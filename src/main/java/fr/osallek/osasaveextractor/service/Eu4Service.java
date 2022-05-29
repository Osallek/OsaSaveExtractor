package fr.osallek.osasaveextractor.service;

import fr.osallek.clausewitzparser.model.ClausewitzItem;
import fr.osallek.eu4parser.Eu4Parser;
import fr.osallek.eu4parser.model.LauncherSettings;
import fr.osallek.eu4parser.model.game.Game;
import fr.osallek.eu4parser.model.save.Save;
import fr.osallek.osasaveextractor.service.object.ProgressState;
import fr.osallek.osasaveextractor.service.object.ProgressStep;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class Eu4Service {

    private LauncherSettings launcherSettings;

    private ProgressState state;

    public void prepare() throws IOException {
        Optional<Path> installationFolder = Eu4Parser.detectInstallationFolder();

        if (installationFolder.isEmpty()) {
            throw new RuntimeException(); //Todo modal to ask ?
        }

        this.launcherSettings = Eu4Parser.loadSettings(installationFolder.get());
        this.state = new ProgressState(ProgressStep.NONE);
    }

    public Map<String, Path> getSaves() throws IOException {
        if (Files.exists(this.launcherSettings.getSavesFolder()) && Files.isDirectory(this.launcherSettings.getSavesFolder())) {
            try (Stream<Path> stream = Files.walk(this.launcherSettings.getSavesFolder())) {
                return stream.filter(path -> path.getFileName().toString().endsWith(".eu4"))
                             .sorted(Comparator.comparing(t -> t.toFile().lastModified(), Comparator.reverseOrder()))
                             .map(path -> this.launcherSettings.getSavesFolder().relativize(path))
                             .collect(Collectors.toMap(path -> {
                                 List<String> strings = new ArrayList<>();

                                 for (int i = 0; i < path.getNameCount(); i++) {
                                     strings.add(path.getName(i).toString());
                                 }

                                 return String.join(" > ", strings);
                             }, Function.identity(), (a, b) -> a, LinkedHashMap::new));
            }
        }

        return null;
    }

    @Async
    public void parseSave(String toAnalyse, String previousSave) {
        try {
            this.state.setStep(ProgressStep.PARSING_GAME);
            AtomicInteger count = new AtomicInteger(0);
            Path savePath = this.launcherSettings.getSavesFolder().resolve(toAnalyse);
            this.state.setStep(ProgressStep.PARSING_GAME);
            Game game = Eu4Parser.parseGame(Eu4Parser.detectInstallationFolder().get(), Eu4Parser.getMods(savePath), this.launcherSettings,
                                            () -> {
                                                count.incrementAndGet();
                                                int progress = ProgressStep.PARSING_GAME.progress;
                                                progress += (ProgressStep.PARSING_GAME.next().progress - ProgressStep.PARSING_GAME.progress) * count.get()
                                                            / Game.NB_PARTS;

                                                this.state.setProgress(progress);
                                            });
            this.state.setStep(ProgressStep.PARSING_SAVE);
            this.state.setSubStep(ProgressStep.PARSING_SAVE_INFO);
            Save save = Eu4Parser.loadSave(savePath, game, Map.of(item -> ClausewitzItem.DEFAULT_NAME.equals(item.getParent().getName()), s -> {
                if ("provinces".equals(s)) {
                    this.state.setSubStep(ProgressStep.PARSING_SAVE_PROVINCES);
                } else if ("countries".equals(s)) {
                    this.state.setSubStep(ProgressStep.PARSING_SAVE_COUNTRIES);
                } else if ("active_advisors".equals(s)) {
                    this.state.setSubStep(ProgressStep.PARSING_SAVE_WARS);
                }
            }));
            this.state.setSubStep(null);
            this.state.setStep(ProgressStep.SENDING_DATA);
            Thread.sleep(2000);
            this.state.setStep(ProgressStep.FINISHED);
            this.state.setLink("http://localhost:8080/saves/" + UUID.randomUUID());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            this.state.setError(true);
            throw new RuntimeException(e);
        }
    }

    public LauncherSettings getLauncherSettings() {
        return launcherSettings;
    }

    public ProgressState getState() {
        return state;
    }
}

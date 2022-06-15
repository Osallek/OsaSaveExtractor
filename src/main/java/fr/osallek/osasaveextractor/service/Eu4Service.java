package fr.osallek.osasaveextractor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.osallek.clausewitzparser.model.ClausewitzItem;
import fr.osallek.eu4parser.Eu4Parser;
import fr.osallek.eu4parser.model.LauncherSettings;
import fr.osallek.eu4parser.model.game.Game;
import fr.osallek.eu4parser.model.game.Province;
import fr.osallek.eu4parser.model.save.Save;
import fr.osallek.osasaveextractor.service.object.ProgressState;
import fr.osallek.osasaveextractor.service.object.ProgressStep;
import fr.osallek.osasaveextractor.service.object.ServerSave;
import fr.osallek.osasaveextractor.service.object.save.SaveDTO;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class Eu4Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(Eu4Service.class);
    private final LauncherSettings launcherSettings;

    private final MessageSource messageSource;

    private final ThreadPoolTaskExecutor executor;

    private final ServerService serverService;

    private final ObjectMapper objectMapper;

    private ProgressState state;

    public Eu4Service(MessageSource messageSource, ThreadPoolTaskExecutor executor, ServerService serverService, ObjectMapper objectMapper) throws IOException {
        this.messageSource = messageSource;
        this.executor = executor;
        this.serverService = serverService;
        this.objectMapper = objectMapper;

        Optional<Path> installationFolder = Eu4Parser.detectInstallationFolder();

        if (installationFolder.isEmpty()) {
            throw new RuntimeException(); //Todo modal to ask ?
        }

        this.launcherSettings = Eu4Parser.loadSettings(installationFolder.get());
    }

    public List<Path> getSaves() throws IOException {
        if (Files.exists(this.launcherSettings.getSavesFolder()) && Files.isDirectory(this.launcherSettings.getSavesFolder())) {
            try (Stream<Path> stream = Files.walk(this.launcherSettings.getSavesFolder())) {
                //Todo filter ironman
                return stream.filter(path -> path.getFileName().toString().endsWith(".eu4"))
                             .sorted(Comparator.comparing(t -> t.toFile().lastModified(), Comparator.reverseOrder()))
                             .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }

    public CompletableFuture<Void> parseSave(Path toAnalyse, ServerSave previousSave) {
        this.state = new ProgressState(ProgressStep.NONE, this.messageSource, Locale.getDefault());

        return this.executor.submitListenable(() -> {
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
                                                                / (Game.NB_PARTS + 1);

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
                this.state.setStep(ProgressStep.SENDING_DATA);
                this.state.setSubStep(null);

                Path tmpFolder = Path.of(FileUtils.getTempDirectoryPath(), UUID.randomUUID().toString());
                FileUtils.forceMkdir(tmpFolder.toFile());
                Path provinceMapFile = tmpFolder.resolve("provinces.png");
                ImageIO.write(ImageIO.read(new File(game.getProvincesImage().getAbsolutePath())), "PNG", provinceMapFile.toFile());

                Path colorsFile = tmpFolder.resolve("colors.png");
                BufferedImage colorsImage = new BufferedImage(game.getProvinces().size(), 1, BufferedImage.TYPE_INT_ARGB);
                Graphics2D colorsImageGraphics = colorsImage.createGraphics();
                int i = 0;
                for (Province province : game.getProvinces().values()) {
                    colorsImageGraphics.setColor(new Color(province.getColor()));
                    colorsImageGraphics.drawLine(i, 0, i, 0);
                    i++;
                }
                ImageIO.write(colorsImage, "PNG", colorsFile.toFile());

                Path dataFile = tmpFolder.resolve("data.json");
                this.objectMapper.writeValue(dataFile.toFile(), new SaveDTO(save));

                return this.serverService.uploadData(dataFile, colorsFile, provinceMapFile)
                                         .whenComplete((s, throwable) -> {
                                             if (throwable != null) {
                                                 this.state.setError(true);
                                                 LOGGER.error(throwable.getMessage(), throwable);

                                                 //FileUtils.deleteQuietly(tmpFolder.toFile());
                                             }
                                         })
                                         .thenAccept(s -> {
                                             this.state.setStep(ProgressStep.FINISHED);
                                             this.state.setLink(s);
                                         });
            } catch (Exception e) {
                this.state.setError(true);
                LOGGER.error("{}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }).completable().thenCompose(unused -> unused);
    }

    public LauncherSettings getLauncherSettings() {
        return launcherSettings;
    }

    public ProgressState getState() {
        return state;
    }
}

package fr.osallek.osasaveextractor.service;

import fr.osallek.eu4parser.Eu4Parser;
import fr.osallek.eu4parser.model.LauncherSettings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class Eu4Service {

    private LauncherSettings launcherSettings;

    public void prepare() throws IOException {
        Optional<Path> installationFolder = Eu4Parser.detectInstallationFolder();

        if (installationFolder.isEmpty()) {
            throw new RuntimeException(); //Todo modal to ask ?
        }

        this.launcherSettings = Eu4Parser.loadSettings(installationFolder.get());
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

    public LauncherSettings getLauncherSettings() {
        return launcherSettings;
    }
}

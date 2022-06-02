package fr.osallek.osasaveextractor.controller.object;

import fr.osallek.osasaveextractor.service.Eu4Service;
import javafx.scene.control.ListCell;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LocalSaveListCell extends ListCell<Path> {

    private final Eu4Service eu4Service;

    public LocalSaveListCell(Eu4Service eu4Service) {
        this.eu4Service = eu4Service;
    }

    @Override
    protected void updateItem(Path path, boolean empty) {
        super.updateItem(path, empty);
        if (path == null || empty) {
            setGraphic(null);
        } else {
            Path relativize = this.eu4Service.getLauncherSettings().getSavesFolder().relativize(path);

            List<String> strings = new ArrayList<>();

            for (int i = 0; i < relativize.getNameCount(); i++) {
                strings.add(relativize.getName(i).toString());
            }

            setText(String.join(" > ", strings));
        }
    }

}

package fr.osallek.osasaveextractor;

import fr.osallek.osasaveextractor.config.StageReadyEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class OsaSaveExtractorUiApplication extends Application {

    @Override
    public void start(Stage stage) {
        OsaSaveExtractorListener.applicationContext.publishEvent(new StageReadyEvent(stage));
    }

    @Override
    public void stop() {
        Platform.exit();
        OsaSaveExtractorListener.applicationContext.close();
        System.exit(0);
    }
}

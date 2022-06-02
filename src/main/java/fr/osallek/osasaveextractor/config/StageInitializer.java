package fr.osallek.osasaveextractor.config;

import fr.osallek.osasaveextractor.controller.MainController;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageInitializer.class);

    private final MainController mainController;

    public StageInitializer(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        try {
            Scene scene = new Scene(this.mainController.getScene());
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

            Stage stage = event.getStage();
            stage.setMaximized(true);
            stage.setScene(scene);
            stage.setTitle("Osa Save Extractor");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/public/favicon.ico")));
            stage.show();
        } catch (Exception e) {
            LOGGER.error("{}", e.getMessage(), e);
        }
    }
}

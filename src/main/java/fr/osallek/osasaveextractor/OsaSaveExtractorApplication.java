package fr.osallek.osasaveextractor;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OsaSaveExtractorApplication {

    public static void main(String[] args) {
        Application.launch(OsaSaveExtractorUiApplication.class, args);
    }

}

package fr.osallek.osasaveextractor;

import fr.osallek.eu4parser.common.Eu4Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OsaSaveExtractorApplication {

    public static void main(String[] args) {
        System.setProperty("OSALLEK_DOCUMENTS", Eu4Utils.OSALLEK_DOCUMENTS_FOLDER.toString());
        SpringApplication.run(OsaSaveExtractorApplication.class, args);
    }

}

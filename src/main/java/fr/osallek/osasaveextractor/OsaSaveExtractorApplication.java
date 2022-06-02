package fr.osallek.osasaveextractor;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

@SpringBootApplication
public class OsaSaveExtractorApplication {

    public static final String ID;

    static {
        //Todo change to save in properties to allow change
        String id1;

        try {
            id1 = UUID.nameUUIDFromBytes(InetAddress.getLocalHost().getHostName().getBytes()).toString();
        } catch (UnknownHostException e) {
            id1 = UUID.randomUUID().toString();
        }

        ID = id1;
    }

    public static void main(String[] args) {
        Application.launch(OsaSaveExtractorUiApplication.class, args);
    }

}

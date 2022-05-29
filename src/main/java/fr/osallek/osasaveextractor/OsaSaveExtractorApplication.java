package fr.osallek.osasaveextractor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OsaSaveExtractorApplication {

    public static final String ID;

    static {
        String id1;

        try {
            id1 = UUID.nameUUIDFromBytes(InetAddress.getLocalHost().getHostName().getBytes()).toString();
        } catch (UnknownHostException e) {
            id1 = UUID.randomUUID().toString();
        }

        ID = id1;
    }

    public static void main(String[] args) {
        SpringApplication.run(OsaSaveExtractorApplication.class, args);
    }

}

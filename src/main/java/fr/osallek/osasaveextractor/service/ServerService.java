package fr.osallek.osasaveextractor.service;

import fr.osallek.osasaveextractor.service.object.ServerSave;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ServerService {

    private static final Random RANDOM = new Random();

    public List<ServerSave> getSaves() {
        //Todo
        List<ServerSave> saves = new ArrayList<>();

        int nb = RANDOM.nextInt(1, 10);

        for (int i = 0; i < nb; i++) {
            saves.add(new ServerSave(RANDOM.ints(97, 122 + 1)
                                           .limit(RANDOM.nextInt(10, 20))
                                           .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                                           .toString() + ".eu4",
                                     getRandomDateTime(),
                                     getRandomDate(),
                                     UUID.randomUUID().toString()));
        }

        saves.sort(Comparator.comparing(ServerSave::creationDate).reversed());

        return saves;
    }

    public CompletableFuture<String> uploadData(Path saveFile, Path colorsFile, Path provinceMapFile) {
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return CompletableFuture.completedFuture("http://localhost:8080/saves/" + UUID.randomUUID()); //Todo
    }

    private LocalDateTime getRandomDateTime() {
        long startSeconds = Instant.now().minusSeconds(24 * 60 * 60 * 30).getEpochSecond();
        long endSeconds = Instant.now().getEpochSecond();

        return LocalDateTime.ofInstant(Instant.ofEpochSecond(RANDOM.nextLong(startSeconds, endSeconds)), ZoneId.systemDefault());
    }

    private LocalDate getRandomDate() {
        long startSeconds = LocalDate.of(1444, 11, 11).atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
        long endSeconds = LocalDate.of(1821, 12, 1).atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();

        return LocalDate.ofInstant(Instant.ofEpochSecond(RANDOM.nextLong(startSeconds, endSeconds)), ZoneId.systemDefault());
    }
}

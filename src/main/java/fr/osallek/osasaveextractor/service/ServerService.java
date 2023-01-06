package fr.osallek.osasaveextractor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.osallek.eu4parser.Eu4Parser;
import fr.osallek.eu4parser.common.Eu4Utils;
import fr.osallek.eu4parser.common.ZipUtils;
import fr.osallek.osasaveextractor.common.CustomGZIPOutputStream;
import fr.osallek.osasaveextractor.common.exception.ServerException;
import fr.osallek.osasaveextractor.config.ApplicationProperties;
import fr.osallek.osasaveextractor.controller.object.DataAssetDTO;
import fr.osallek.osasaveextractor.controller.object.ErrorCode;
import fr.osallek.osasaveextractor.controller.object.ErrorObject;
import fr.osallek.osasaveextractor.service.object.save.SaveDTO;
import fr.osallek.osasaveextractor.service.object.server.ServerSave;
import fr.osallek.osasaveextractor.service.object.server.UploadResponseDTO;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

@Service
public class ServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerService.class);

    private final ApplicationProperties properties;

    private final ObjectMapper objectMapper;

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    public ServerService(ApplicationProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public boolean needUpdate() throws IOException {
        String response = this.httpClient.execute(new HttpGet(this.properties.getServerUrl() + "/api/version"), new BasicHttpClientResponseHandler());
        return this.properties.getVersion().compareTo(new DefaultArtifactVersion(response)) < 0;
    }

    public Map<Integer, String> getTokens() throws IOException {
        return this.httpClient.execute(new HttpGet(this.properties.getServerUrl() + "/data/tokens.txt"), response -> {
            try (ObjectInputStream tokensStream = new ObjectInputStream(response.getEntity().getContent())) {
                return (Map<Integer, String>) tokensStream.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public SortedSet<ServerSave> getSaves(String id) {
        SortedSet<ServerSave> saves = new TreeSet<>(Comparator.comparing(ServerSave::creationDate).reversed());

        try {
            return this.httpClient.execute(new HttpGet(this.properties.getServerUrl() + "/api/saves/user/" + id), response -> {
                if (200 != response.getCode()) {
                    LOGGER.error("An error occurred while retrieving saves from server: {}", response.getCode());
                    return saves;
                }

                saves.addAll(List.of(this.objectMapper.readValue(response.getEntity().getContent(), ServerSave[].class)));

                return saves;
            });
        } catch (IOException e) {
            LOGGER.error("An error occurred while retrieving saves from server: {}", e.getMessage(), e);
            return saves;
        }
    }

    public CompletableFuture<UploadResponseDTO> uploadData(SaveDTO save) throws IOException {
        HttpPost post = new HttpPost(this.properties.getServerUrl() + "/api/saves");
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");
        post.setEntity(new ByteArrayEntity(this.objectMapper.writeValueAsBytes(save), ContentType.APPLICATION_JSON));

        return this.httpClient.execute(post, response -> {
            try {
                String s = EntityUtils.toString(response.getEntity());

                if (response.getCode() != 200) {
                    return CompletableFuture.failedFuture(new ServerException(this.objectMapper.readValue(s, ErrorObject.class).getError()));
                }

                return CompletableFuture.completedFuture(this.objectMapper.readValue(s, UploadResponseDTO.class));
            } catch (Exception e) {
                return CompletableFuture.failedFuture(new ServerException(ErrorCode.DEFAULT_ERROR));
            }
        });
    }

    public CompletableFuture<Boolean> uploadAssets(Collection<Path> assets, Path root, String id, String userId) throws IOException {
        Path zip = root.resolve("assets.zip");

        ZipUtils.zipFolder(root, zip, assets::contains);

        MultipartEntityBuilder body = MultipartEntityBuilder.create();
        body.addBinaryBody("assets", zip.toFile(), ContentType.APPLICATION_OCTET_STREAM, zip.getFileName().toString());
        body.addBinaryBody("data", this.objectMapper.writeValueAsBytes(new DataAssetDTO(userId, id)), ContentType.APPLICATION_JSON, null);

        HttpPost post = new HttpPost(this.properties.getServerUrl() + "/api/data");
        post.setEntity(body.build());

        return this.httpClient.execute(post, response -> {
            try {
                if (response.getCode() != 204) {
                    if (response.getEntity() != null) {
                        String s = EntityUtils.toString(response.getEntity());
                        return CompletableFuture.failedFuture(new ServerException(this.objectMapper.readValue(s, ErrorObject.class).getError()));
                    } else {
                        return CompletableFuture.failedFuture(new ServerException(ErrorCode.DEFAULT_ERROR));
                    }
                }

                return CompletableFuture.completedFuture(true);
            } catch (Exception e) {
                return CompletableFuture.failedFuture(new ServerException(ErrorCode.DEFAULT_ERROR));
            }
        });
    }

    public CompletableFuture<Boolean> uploadSave(Path save, Path tmpFolder, String id, String userId) throws IOException {
        Path gz = tmpFolder.resolve(id + ".eu4.gz");

        if (Eu4Parser.isValidCompressed(save)) {
            Path zipFolder = tmpFolder.resolve("zip");
            ZipUtils.unzip(save, zipFolder);
            save = tmpFolder.resolve(id + ".zip");
            ZipUtils.zipFolder(zipFolder, save,
                               path -> Eu4Utils.GAMESTATE_FILE.equals(path.getFileName().toString()) || Eu4Utils.META_FILE.equals(
                                       path.getFileName().toString()) || Eu4Utils.AI_FILE.equals(path.getFileName().toString()));
        }

        try (CustomGZIPOutputStream outputStream = new CustomGZIPOutputStream(new FileOutputStream(gz.toFile()));
             InputStream inputStream = Files.newInputStream(save)) {
            IOUtils.copyLarge(inputStream, outputStream, new byte[1_000_000]);
        }

        MultipartEntityBuilder body = MultipartEntityBuilder.create();
        body.addBinaryBody("save", gz.toFile(), ContentType.APPLICATION_OCTET_STREAM, gz.getFileName().toString());
        body.addBinaryBody("data", this.objectMapper.writeValueAsBytes(new DataAssetDTO(userId, id)), ContentType.APPLICATION_JSON, null);

        HttpPost post = new HttpPost(this.properties.getServerUrl() + "/api/data/save");
        post.setEntity(body.build());

        return this.httpClient.execute(post, response -> {
            try {
                if (response.getCode() != 204) {
                    if (response.getEntity() != null) {
                        String s = EntityUtils.toString(response.getEntity());
                        return CompletableFuture.failedFuture(new ServerException(this.objectMapper.readValue(s, ErrorObject.class).getError()));
                    } else {
                        return CompletableFuture.failedFuture(new ServerException(ErrorCode.DEFAULT_ERROR));
                    }
                }

                return CompletableFuture.completedFuture(true);
            } catch (Exception e) {
                return CompletableFuture.failedFuture(new ServerException(ErrorCode.DEFAULT_ERROR));
            }
        });
    }
}

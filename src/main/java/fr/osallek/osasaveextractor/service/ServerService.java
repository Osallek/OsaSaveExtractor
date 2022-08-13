package fr.osallek.osasaveextractor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.osallek.eu4parser.Eu4Parser;
import fr.osallek.eu4parser.common.Eu4Utils;
import fr.osallek.eu4parser.common.ZipUtils;
import fr.osallek.osasaveextractor.common.CustomGZIPOutputStream;
import fr.osallek.osasaveextractor.common.exception.ServerException;
import fr.osallek.osasaveextractor.config.ApplicationProperties;
import fr.osallek.osasaveextractor.controller.object.DataAssetDTO;
import fr.osallek.osasaveextractor.controller.object.ErrorObject;
import fr.osallek.osasaveextractor.service.object.save.SaveDTO;
import fr.osallek.osasaveextractor.service.object.server.ServerSave;
import fr.osallek.osasaveextractor.service.object.server.UploadResponseDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

@Service
public class ServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerService.class);

    private final ApplicationProperties properties;

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    public ServerService(ApplicationProperties properties, ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    public boolean needUpdate() {
        String minVersion = this.restTemplate.getForObject(this.properties.getServerUrl() + "/api/version", String.class);

        return this.properties.getVersion().compareTo(new DefaultArtifactVersion(minVersion)) != 0;
    }

    public SortedSet<ServerSave> getSaves(String id) {
        ResponseEntity<List<ServerSave>> response = this.restTemplate.exchange(this.properties.getServerUrl() + "/api/saves/user/" + id, HttpMethod.GET, null,
                                                                               new ParameterizedTypeReference<>() {});

        if (!HttpStatus.OK.equals(response.getStatusCode())) {
            LOGGER.error("An error occurred while retrieving saves from server: {}", response.getStatusCode());
        }

        SortedSet<ServerSave> saves = new TreeSet<>(Comparator.comparing(ServerSave::creationDate).reversed());

        if (CollectionUtils.isNotEmpty(response.getBody())) {
            saves.addAll(response.getBody());
        }

        return saves;
    }

    public CompletableFuture<UploadResponseDTO> uploadData(SaveDTO save) throws JsonProcessingException {
        try {
            ResponseEntity<String> response = new RestTemplate().postForEntity(this.properties.getServerUrl() + "/api/saves", save, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return CompletableFuture.failedFuture(new ServerException(this.objectMapper.readValue(response.getBody(), ErrorObject.class).getError()));
            }

            return CompletableFuture.completedFuture(this.objectMapper.readValue(response.getBody(), UploadResponseDTO.class));
        } catch (HttpClientErrorException e) {
            return CompletableFuture.failedFuture(new ServerException(this.objectMapper.readValue(e.getResponseBodyAsString(), ErrorObject.class).getError()));
        }
    }

    public CompletableFuture<Boolean> uploadAssets(Collection<Path> assets, Path root, String id, String userId) throws IOException {
        Path zip = root.resolve("assets.zip");

        ZipUtils.zipFolder(root, zip, assets::contains);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("assets", new FileSystemResource(zip));
        body.add("data", new DataAssetDTO(userId, id));

        try {
            ResponseEntity<String> response = this.restTemplate.postForEntity(this.properties.getServerUrl() + "/api/data",
                                                                              new HttpEntity<>(body, headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return CompletableFuture.failedFuture(new ServerException(this.objectMapper.readValue(response.getBody(), ErrorObject.class).getError()));
            }

            return CompletableFuture.completedFuture(true);
        } catch (HttpClientErrorException e) {
            return CompletableFuture.failedFuture(new ServerException(this.objectMapper.readValue(e.getResponseBodyAsString(), ErrorObject.class).getError()));
        }
    }

    public CompletableFuture<Boolean> uploadSave(Path save, Path root, String id, String userId) throws IOException {
        Path gz = root.resolve(id + ".eu4.gz");

        if (Eu4Parser.isValidCompressed(save)) {
            Path zipFolder = root.resolve("zip");
            ZipUtils.unzip(save, zipFolder);
            save = root.resolve(id + ".zip");
            ZipUtils.zipFolder(zipFolder, save,
                               path -> Eu4Utils.GAMESTATE_FILE.equals(path.getFileName().toString()) || Eu4Utils.META_FILE.equals(
                                       path.getFileName().toString()) || Eu4Utils.AI_FILE.equals(path.getFileName().toString()));
        }

        try (CustomGZIPOutputStream outputStream = new CustomGZIPOutputStream(new FileOutputStream(gz.toFile()));
             InputStream inputStream = Files.newInputStream(save)) {
            IOUtils.copyLarge(inputStream, outputStream, new byte[1_000_000]);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("save", new FileSystemResource(gz));
        body.add("data", new DataAssetDTO(userId, id));

        try {
            ResponseEntity<String> response = this.restTemplate.postForEntity(this.properties.getServerUrl() + "/api/data/save",
                                                                              new HttpEntity<>(body, headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return CompletableFuture.failedFuture(new ServerException(this.objectMapper.readValue(response.getBody(), ErrorObject.class).getError()));
            }

            return CompletableFuture.completedFuture(true);
        } catch (HttpClientErrorException e) {
            return CompletableFuture.failedFuture(new ServerException(this.objectMapper.readValue(e.getResponseBodyAsString(), ErrorObject.class).getError()));
        }
    }
}

package fr.osallek.osasaveextractor.service.object.server;

import fr.osallek.osasaveextractor.service.object.save.SaveDTO;

public record UploadResponseDTO(String link, AssetsDTO assetsDTO) {

    public UploadResponseDTO(SaveDTO save) {
        this("http://localhost:8080/saves/g", new AssetsDTO(save));
    }
}

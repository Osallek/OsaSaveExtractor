package fr.osallek.osasaveextractor.service.object.server;

import fr.osallek.osasaveextractor.service.object.save.SaveDTO;

import java.util.Set;

public record AssetsDTO(boolean provinces, boolean colors, Set<String> countries, Set<String> advisors, Set<String> institutions, Set<String> buildings,
                        Set<String> religions, Set<String> tradeGoods) {

    public AssetsDTO(SaveDTO save) {
        this(false,
             false,
             Set.of(),
             Set.of(),
             Set.of(),
             Set.of(),
             Set.of(),
             Set.of());
    }
}

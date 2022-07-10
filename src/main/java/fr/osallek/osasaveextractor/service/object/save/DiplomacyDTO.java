package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.diplomacy.Diplomacy;

import java.util.List;
import java.util.stream.Collectors;

public class DiplomacyDTO {

    private final List<DependencyDTO> dependencies;

    private final List<RelationDTO> alliances;

    public DiplomacyDTO(Diplomacy diplomacy) {
        this.dependencies = diplomacy.getDependencies().stream().map(DependencyDTO::new).toList();
        this.alliances = diplomacy.getAlliances().stream().map(RelationDTO::new).toList();
    }

    public List<DependencyDTO> getDependencies() {
        return dependencies;
    }

    public List<RelationDTO> getAlliances() {
        return alliances;
    }
}

package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.diplomacy.Dependency;

public class DependencyDTO extends RelationDTO {

    private final String type;

    public DependencyDTO(Dependency dependency) {
        super(dependency);
        this.type = dependency.getSubjectType().getName();
    }

    public String getType() {
        return type;
    }
}

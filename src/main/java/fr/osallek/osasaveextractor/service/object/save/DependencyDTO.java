package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.diplomacy.DatableRelation;
import fr.osallek.eu4parser.model.save.diplomacy.Dependency;

import java.time.LocalDate;

public class DependencyDTO extends RelationDTO {

    private final String type;

    private final LocalDate endDate;

    public DependencyDTO(Dependency dependency) {
        super(dependency);
        this.type = dependency.getSubjectType().getName();
        this.endDate = null;
    }

    public DependencyDTO(String first, String second, LocalDate date, String type, LocalDate endDate) {
        super(first, second, date);
        this.type = type;
        this.endDate = endDate;
    }

    public String getType() {
        return type;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}

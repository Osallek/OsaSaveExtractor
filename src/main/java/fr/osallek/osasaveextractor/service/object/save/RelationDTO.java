package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.diplomacy.DatableRelation;

import java.time.LocalDate;

public class RelationDTO {

    private final String first;

    private final String second;

    private final LocalDate date;

    public RelationDTO(DatableRelation relation) {
        this.first = relation.getFirst().getTag();
        this.second = relation.getSecond().getTag();
        this.date = relation.getStartDate();
    }

    public String getFirst() {
        return first;
    }

    public String getSecond() {
        return second;
    }

    public LocalDate getDate() {
        return date;
    }
}

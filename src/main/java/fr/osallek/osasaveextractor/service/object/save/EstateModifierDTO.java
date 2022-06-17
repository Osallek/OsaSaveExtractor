package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.clausewitzparser.common.ClausewitzUtils;
import fr.osallek.eu4parser.model.save.country.SaveEstateModifier;

import java.time.LocalDate;

public class EstateModifierDTO {

    private final Double value;

    private final String name;

    private final LocalDate date;

    public EstateModifierDTO(SaveEstateModifier modifier) {
        this.value = modifier.getValue();
        this.name = ClausewitzUtils.removeQuotes(modifier.getDesc());
        this.date = modifier.getDate();
    }

    public Double getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }
}

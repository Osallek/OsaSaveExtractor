package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.country.Queen;
import org.apache.commons.lang3.BooleanUtils;

public class QueenDTO extends MonarchDTO {

    private final boolean consort;

    private final boolean queenRegent;

    private final String countryOfOrigin;

    public QueenDTO(Queen queen) {
        super(queen);
        this.consort = BooleanUtils.toBoolean(queen.getConsort());
        this.queenRegent = BooleanUtils.toBoolean(queen.getQueenRegent());
        this.countryOfOrigin = queen.getCountryOfOrigin() == null ? null : queen.getCountryOfOrigin().getTag();
    }

    public boolean isConsort() {
        return consort;
    }

    public boolean isQueenRegent() {
        return queenRegent;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }
}

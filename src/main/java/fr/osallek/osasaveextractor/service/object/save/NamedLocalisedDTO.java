package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.localisation.Eu4Language;
import fr.osallek.eu4parser.model.game.localisation.Localisation;
import java.util.Map;

public class NamedLocalisedDTO extends Localised {

    protected String name;

    public NamedLocalisedDTO(String name) {
        this.name = name;
    }

    public NamedLocalisedDTO(Map<Eu4Language, Localisation> localisations, String name) {
        super(localisations);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

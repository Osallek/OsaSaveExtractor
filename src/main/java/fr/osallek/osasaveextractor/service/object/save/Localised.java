package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.localisation.Eu4Language;
import fr.osallek.eu4parser.model.game.localisation.Localisation;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;
import java.util.stream.Collectors;

public class Localised {

    protected Map<Eu4Language, String> localisations;

    public Localised() {
    }

    public Localised(Map<Eu4Language, Localisation> localisations) {
        if (MapUtils.isNotEmpty(localisations)) {
            this.localisations = localisations.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getValue()));
        }
    }

    public Map<Eu4Language, String> getLocalisations() {
        return localisations;
    }

    public void setLocalisations(Map<Eu4Language, String> localisations) {
        this.localisations = localisations;
    }
}

package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.localisation.Eu4Language;
import fr.osallek.eu4parser.model.game.localisation.Localisation;
import fr.osallek.eu4parser.model.save.Save;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Localised {

    private static final Pattern ICONS_PATTERN = Pattern.compile("£.*?£");

    protected Map<Eu4Language, String> localisations;

    public Localised() {
    }

    public Localised(Map<Eu4Language, Localisation> localisations) {
        if (MapUtils.isNotEmpty(localisations)) {
            this.localisations = localisations.entrySet()
                                              .stream()
                                              .collect(Collectors.toMap(Map.Entry::getKey, e -> ICONS_PATTERN.matcher(e.getValue().getValue()).replaceAll("")));
        }
    }

    public Localised(Save save, Object root, String key) {
        Map<Eu4Language, Localisation> l = save.getGame().getLocalisation(key);

        if (MapUtils.isNotEmpty(l)) {
            this.localisations = l.keySet()
                                  .stream()
                                  .collect(
                                          Collectors.toMap(Function.identity(), language -> save.getGame().getComputedLocalisation(save, root, key, language)));
        }
    }

    public Localised(Localised other) {
        this.localisations = other.localisations;
    }

    public Map<Eu4Language, String> getLocalisations() {
        return localisations;
    }

    public void setLocalisations(Map<Eu4Language, String> localisations) {
        this.localisations = localisations;
    }
}

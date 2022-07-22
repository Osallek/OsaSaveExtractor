package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.localisation.Eu4Language;
import fr.osallek.eu4parser.model.game.localisation.Localisation;
import java.nio.file.Path;
import java.util.Map;

public class ColorNamedImageLocalisedDTO extends NamedImageLocalisedDTO {

    private ColorDTO color;

    public ColorNamedImageLocalisedDTO(Map<Eu4Language, Localisation> localisations, Path image, String name, ColorDTO color) {
        super(localisations, image, name);
        this.color = color;
    }

    public ColorDTO getColor() {
        return color;
    }

    public void setColor(ColorDTO color) {
        this.color = color;
    }
}

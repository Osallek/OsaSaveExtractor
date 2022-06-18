package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.Culture;
import fr.osallek.osasaveextractor.common.Constants;

public class CultureDTO {

    private final String group;

    private final String name;

    private final ColorDTO color;

    public CultureDTO(Culture culture) {
        this.group = culture.getCultureGroup().getName();
        this.name = culture.getName();
        this.color = Constants.stringToColor(this.name);
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public ColorDTO getColor() {
        return color;
    }
}

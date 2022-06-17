package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.country.CustomColors;

public class CustomColorsDTO {

    private final ColorDTO flagColors;

    private final Integer flag;

    private final Integer color;

    public CustomColorsDTO(CustomColors customColors) {
        this.flagColors = customColors.getFlagColors() == null ? null : new ColorDTO(customColors.getFlagColors());
        this.flag = customColors.getFlag();
        this.color = customColors.getColor();
    }

    public ColorDTO getFlagColors() {
        return flagColors;
    }

    public Integer getFlag() {
        return flag;
    }

    public Integer getColor() {
        return color;
    }
}

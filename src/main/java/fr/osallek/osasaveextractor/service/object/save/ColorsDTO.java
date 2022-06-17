package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.country.Colors;

public class ColorsDTO {

    private final CustomColorsDTO customColors;

    private final ColorDTO revolutionaryColors;

    private final ColorDTO mapColor;

    private final ColorDTO countryColor;

    public ColorsDTO(Colors colors) {
        this.customColors = colors.getCustomColors() == null ? null : new CustomColorsDTO(colors.getCustomColors());
        this.revolutionaryColors = colors.getRevolutionaryColors() == null ? null : new ColorDTO(colors.getRevolutionaryColors());
        this.mapColor = colors.getMapColor() == null ? null : new ColorDTO(colors.getMapColor());
        this.countryColor = colors.getCountryColor() == null ? null : new ColorDTO(colors.getCountryColor());
    }

    public CustomColorsDTO getCustomColors() {
        return customColors;
    }

    public ColorDTO getRevolutionaryColors() {
        return revolutionaryColors;
    }

    public ColorDTO getMapColor() {
        return mapColor;
    }

    public ColorDTO getCountryColor() {
        return countryColor;
    }
}

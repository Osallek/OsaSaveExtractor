package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.Area;
import fr.osallek.eu4parser.model.game.Region;
import fr.osallek.eu4parser.model.save.Save;
import fr.osallek.osasaveextractor.common.Constants;

import java.util.List;

public class RegionDTO extends NamedLocalisedDTO {

    private final ColorDTO color;

    private final List<String> areas;

    public RegionDTO(Save save, Region region) {
        super(save.getGame().getLocalisation(region.getName()), region.getName());
        this.color = Constants.stringToColor(region.getName());
        this.areas = region.getAreas().stream().map(Area::getName).toList();
    }

    public ColorDTO getColor() {
        return color;
    }

    public List<String> getAreas() {
        return areas;
    }
}

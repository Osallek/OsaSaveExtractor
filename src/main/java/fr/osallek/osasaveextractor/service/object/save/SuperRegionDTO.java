package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.Region;
import fr.osallek.eu4parser.model.game.SuperRegion;
import fr.osallek.eu4parser.model.save.Save;
import fr.osallek.osasaveextractor.common.Constants;

import java.util.List;

public class SuperRegionDTO extends NamedLocalisedDTO {

    private final ColorDTO color;

    private final List<String> regions;

    public SuperRegionDTO(Save save, SuperRegion region) {
        super(save.getGame().getLocalisation(region.getName()), region.getName());
        this.color = Constants.stringToColor(region.getName());
        this.regions = region.getRegions().stream().map(Region::getName).toList();
    }

    public ColorDTO getColor() {
        return color;
    }

    public List<String> getRegions() {
        return regions;
    }
}

package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.ProvinceList;
import fr.osallek.eu4parser.model.save.Save;
import fr.osallek.osasaveextractor.common.Constants;

import java.util.List;

public class ProvinceListDTO extends NamedLocalisedDTO {

    private final ColorDTO color;

    private final List<Integer> provinces;

    public ProvinceListDTO(Save save, ProvinceList continent) {
        super(save.getGame().getLocalisation(continent.getName()), continent.getName());
        this.color = Constants.stringToColor(continent.getName());
        this.provinces = continent.getProvinces();
    }

    public ColorDTO getColor() {
        return color;
    }

    public List<Integer> getProvinces() {
        return provinces;
    }
}

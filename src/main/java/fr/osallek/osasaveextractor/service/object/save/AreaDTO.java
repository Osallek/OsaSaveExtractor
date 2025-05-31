package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.Area;
import fr.osallek.eu4parser.model.game.Investment;
import fr.osallek.eu4parser.model.save.Save;
import fr.osallek.eu4parser.model.save.country.SaveArea;
import fr.osallek.osasaveextractor.common.Constants;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AreaDTO extends NamedLocalisedDTO {

    private final ColorDTO color;

    private final List<Integer> provinces;

    private final Map<String, List<String>> investments;

    private final Map<String, CountryStateDTO> states;

    public AreaDTO(Save save, Area area) {
        super(save.getGame().getLocalisation(area.getName()), area.getName());
        SaveArea saveArea = save.getAreas().get(area.getName());

        this.color = Optional.ofNullable(area.getColor()).map(ColorDTO::new).orElse(Constants.stringToColor(area.getName()));
        this.provinces = area.getProvinces();

        if (saveArea != null) {
            this.investments = MapUtils.isEmpty(saveArea.getInvestments()) ? null :
                               saveArea.getInvestments()
                                       .entrySet()
                                       .stream()
                                       .collect(Collectors.toMap(entry -> entry.getKey().getTag(),
                                                                 entry -> entry.getValue()
                                                                               .getInvestments()
                                                                               .stream()
                                                                               .map(Investment::getName)
                                                                               .toList()));
            this.states = MapUtils.isEmpty(saveArea.getCountriesStates()) ? null :
                          saveArea.getCountriesStates()
                                  .entrySet()
                                  .stream()
                                  .collect(Collectors.toMap(entry -> entry.getKey().getTag(),
                                                            entry -> new CountryStateDTO(entry.getValue())));
        } else {
            this.investments = null;
            this.states = null;
        }
    }

    public ColorDTO getColor() {
        return color;
    }

    public List<Integer> getProvinces() {
        return provinces;
    }

    public Map<String, List<String>> getInvestments() {
        return investments;
    }

    public Map<String, CountryStateDTO> getStates() {
        return states;
    }
}

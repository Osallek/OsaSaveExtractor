package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.Investment;
import fr.osallek.eu4parser.model.save.Save;
import fr.osallek.eu4parser.model.save.country.SaveArea;
import fr.osallek.eu4parser.model.save.province.SaveProvince;
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

    public AreaDTO(Save save, SaveArea area) {
        super(save.getGame().getLocalisation(area.getName()), area.getName());
        this.color = Optional.ofNullable(save.getGame().getArea(area.getName()))
                             .flatMap(a -> Optional.ofNullable(a.getColor()))
                             .map(ColorDTO::new)
                             .orElse(Constants.stringToColor(area.getName()));
        this.provinces = area.getProvinces().stream().map(SaveProvince::getId).toList();
        this.investments = MapUtils.isEmpty(area.getInvestments()) ? null :
                           area.getInvestments()
                               .entrySet()
                               .stream()
                               .collect(Collectors.toMap(entry -> entry.getKey().getTag(), entry -> entry.getValue()
                                                                                                         .getInvestments()
                                                                                                         .stream()
                                                                                                         .map(Investment::getName)
                                                                                                         .toList()));
        this.states = MapUtils.isEmpty(area.getCountriesStates()) ? null :
                      area.getCountriesStates()
                          .entrySet()
                          .stream()
                          .collect(Collectors.toMap(entry -> entry.getKey().getTag(), entry -> new CountryStateDTO(entry.getValue())));
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

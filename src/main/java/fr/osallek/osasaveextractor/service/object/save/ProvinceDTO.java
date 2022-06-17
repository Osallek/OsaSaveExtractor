package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.province.ProvinceBuilding;
import fr.osallek.eu4parser.model.save.province.SaveProvince;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ProvinceDTO extends SimpleProvinceDTO {

    private final Double baseTax;

    private final Double baseProduction;

    private final Double baseManpower;

    private final Double devastation;

    private final List<Double> institutions;

    private final boolean isCity;

    private final Map<String, Integer> improvements;

    private final List<String> buildings;

    private final List<ProvinceHistoryDTO> history = new ArrayList<>();

    public ProvinceDTO(SaveProvince province) {
        super(province);
        this.baseManpower = province.getBaseManpower();
        this.baseProduction = province.getBaseProduction();
        this.baseTax = province.getBaseTax();
        this.devastation = province.getDevastation();
        this.institutions = province.getInstitutionsProgress();
        this.isCity = province.isCity();
        this.improvements = province.getImproveCount();
        this.buildings = province.getBuildings().stream().map(ProvinceBuilding::getName).collect(Collectors.toList());

        if (province.getHistory() != null) {
            this.history.addAll(province.getHistory().getEvents().stream().map(ProvinceHistoryDTO::new).toList());
            this.history.add(new ProvinceHistoryDTO(province.getHistory()));
            this.history.sort(Comparator.comparing(ProvinceHistoryDTO::getDate));
        }
    }

    public Double getBaseTax() {
        return baseTax;
    }

    public Double getBaseProduction() {
        return baseProduction;
    }

    public Double getBaseManpower() {
        return baseManpower;
    }

    public Double getDevastation() {
        return devastation;
    }

    public List<Double> getInstitutions() {
        return institutions;
    }

    public boolean isCity() {
        return isCity;
    }

    public Map<String, Integer> getImprovements() {
        return improvements;
    }

    public List<ProvinceHistoryDTO> getHistory() {
        return history;
    }

    public List<String> getBuildings() {
        return buildings;
    }
}

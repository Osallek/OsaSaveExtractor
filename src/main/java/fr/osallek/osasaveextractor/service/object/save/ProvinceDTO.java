package fr.osallek.osasaveextractor.service.object.save;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.osallek.eu4parser.common.Eu4Utils;
import fr.osallek.eu4parser.model.game.Building;
import fr.osallek.eu4parser.model.save.province.ProvinceBuilding;
import fr.osallek.eu4parser.model.save.province.SaveProvince;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


public class ProvinceDTO extends SimpleProvinceDTO {

    private final Double baseTax;

    private final Double baseProduction;

    private final Double baseManpower;

    private final Double devastation;

    private final Double autonomy;

    private final List<Double> institutions;

    private final boolean isCity;

    private final String node;

    private final Map<String, Integer> improvements;

    private final List<String> buildings;

    private final Double colonySize;

    private final List<ProvinceHistoryDTO> history = new ArrayList<>();

    public ProvinceDTO(SaveProvince province) {
        super(province);
        this.baseManpower = province.getBaseManpower();
        this.baseProduction = province.getBaseProduction();
        this.baseTax = province.getBaseTax();
        this.devastation = province.getDevastation();
        this.autonomy = province.getLocalAutonomy();
        this.institutions = province.getInstitutionsProgress();
        this.isCity = province.isCity();
        this.node = province.getTradeNode().getName();
        this.improvements = province.getImproveCount();
        this.buildings = province.getBuildings().stream().map(ProvinceBuilding::getName).toList();
        this.colonySize = province.getColonySize();

        if (province.getHistory() != null) {
            this.history.addAll(province.getHistory().getEvents().stream().map(ProvinceHistoryDTO::new).toList());
            this.history.add(new ProvinceHistoryDTO(province.getHistory()));
            this.history.removeIf(Predicate.not(ProvinceHistoryDTO::notEmpty));
            this.history.sort(ProvinceHistoryDTO.COMPARATOR);

            Iterator<ProvinceHistoryDTO> iterator = this.history.iterator();
            ProvinceHistoryDTO previousValue = iterator.next();

            while (iterator.hasNext()) {
                ProvinceHistoryDTO h = iterator.next();

                if (h.isSame(previousValue)) {
                    iterator.remove();
                } else {
                    previousValue = h;
                }
            }

            for (int i = 0; i < this.history.size(); i++) { //Force remove buildings if empty owner (native american moved province)
                ProvinceHistoryDTO h = this.history.get(i);
                if (Eu4Utils.DEFAULT_TAG.equals(h.getOwner())) {
                    h.getBuildings().clear();

                    if (i > 0) {
                        for (int j = 0; j < i; j++) {
                            ProvinceHistoryDTO g = this.history.get(j);
                            g.getBuildings().forEach((s, aBoolean) -> {
                                if (BooleanUtils.toBoolean(aBoolean)) {
                                    h.getBuildings().put(s, false);
                                }
                            });
                        }
                    }
                }
            }

            this.history.stream()
                        .filter(h -> MapUtils.isNotEmpty(h.getBuildings()))
                        .forEach(h -> {
                            Map<String, Boolean> toAdd = new HashMap<>();

                            h.getBuildings().forEach((s, value) -> {
                                if (BooleanUtils.toBoolean(value)) {
                                    Building building = province.getSave().getGame().getBuilding(s);

                                    if (building != null && building.makeObsolete()) {
                                        toAdd.put(building.getMakeObsolete(), false);
                                    }
                                }
                            });

                            h.getBuildings().putAll(toAdd);
                        }); //Force remove old building when upgrading
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

    public Double getAutonomy() {
        return autonomy;
    }

    public List<Double> getInstitutions() {
        return institutions;
    }

    public boolean isCity() {
        return isCity;
    }

    public String getNode() {
        return node;
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

    public Double getColonySize() {
        return colonySize;
    }

    @JsonIgnore
    public boolean isOwnerAt(LocalDate date, String tag) {
        String owner = this.history.get(0).getOwner();
        for (ProvinceHistoryDTO h : this.history) {
            if (h.getDate().isAfter(date)) {
                break;
            }

            if (StringUtils.isNotBlank(h.getOwner())) {
                owner = h.getOwner();
            }
        }

        return tag.equals(owner);
    }

    @JsonIgnore
    public synchronized void addOwner(LocalDate date, String tag) {
        this.history.add(new ProvinceHistoryDTO(date, tag, tag));
        this.history.sort(ProvinceHistoryDTO.COMPARATOR);
    }
}

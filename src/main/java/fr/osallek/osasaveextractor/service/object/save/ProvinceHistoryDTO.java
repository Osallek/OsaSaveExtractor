package fr.osallek.osasaveextractor.service.object.save;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.osallek.clausewitzparser.common.ClausewitzUtils;
import fr.osallek.eu4parser.model.save.province.SaveProvinceHistoryEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ProvinceHistoryDTO {

    public static final Comparator<ProvinceHistoryDTO> COMPARATOR = (o1, o2) -> {
        int compare = Comparator.comparing(ProvinceHistoryDTO::getDate).compare(o1, o2);

        return compare != 0 ? compare
                            : Comparator.comparing(ProvinceHistoryDTO::getFakeOwner, Comparator.nullsFirst(Comparator.naturalOrder())).compare(o1, o2);
    };

    private final LocalDate date;

    private final String capital;

    private final List<String> addCores;

    private final List<String> addClaims;

    private final List<String> removeCores;

    private final List<String> removeClaims;

    private final Boolean hre;

    private final Double baseTax;

    private final Double baseProduction;

    private final Double baseManpower;

    private final String tradeGood;

    private final String name;

    private final String tribalOwner;

    private final Integer advisor;

    private final Integer nativeHostileness;

    private final Integer nativeFerocity;

    private final Integer nativeSize;

    private String owner;

    private final String fakeOwner;

    private String controller;

    private final List<String> discoveredBy;

    private final String culture;

    private final String religion;

    private final Boolean isCity;

    private final Map<String, Boolean> buildings;

    public ProvinceHistoryDTO(SaveProvinceHistoryEvent event) {
        this.date = event.getDate();
        this.capital = event.getCapital();
        this.addCores = event.getAddCores();
        this.addClaims = event.getAddClaims();
        this.removeCores = event.getRemoveCores();
        this.removeClaims = event.getRemoveClaims();
        this.hre = event.getHre();
        this.baseTax = event.getBaseTax();
        this.baseProduction = event.getBaseProduction();
        this.baseManpower = event.getBaseManpower();
        this.tradeGood = event.getTradeGood();
        this.name = event.getName() == null ? null : ClausewitzUtils.removeQuotes(event.getName().getKey());
        this.tribalOwner = ClausewitzUtils.removeQuotes(event.getTribalOwner());
        this.advisor = event.getAdvisor() == null ? null : event.getAdvisor().getId().getId();
        this.nativeHostileness = event.getNativeHostileness();
        this.nativeFerocity = event.getNativeFerocity();
        this.nativeSize = event.getNativeSize();
        this.fakeOwner = event.getFakeOwner();
        this.owner = event.getOwner();
        this.controller = event.getController();
        this.discoveredBy = event.getDiscoveredBy();
        this.culture = event.getCulture();
        this.religion = event.getReligion();
        this.isCity = event.getIsCity();
        this.buildings = event.getBuildings();
    }

    public ProvinceHistoryDTO(LocalDate date, String owner, String controller) {
        this.date = date;
        this.owner = owner;
        this.controller = controller;
        this.fakeOwner = null;
        this.capital = null;
        this.addCores = null;
        this.addClaims = null;
        this.removeCores = null;
        this.removeClaims = null;
        this.hre = null;
        this.baseTax = null;
        this.baseProduction = null;
        this.baseManpower = null;
        this.tradeGood = null;
        this.name = null;
        this.tribalOwner = null;
        this.advisor = null;
        this.nativeHostileness = null;
        this.nativeFerocity = null;
        this.nativeSize = null;
        this.discoveredBy = null;
        this.culture = null;
        this.religion = null;
        this.isCity = null;
        this.buildings = null;
    }

    @JsonIgnore
    public boolean notEmpty() {
        return this.owner != null ||
               this.controller != null ||
               this.capital != null ||
               CollectionUtils.isNotEmpty(this.addCores) ||
               CollectionUtils.isNotEmpty(this.addClaims) ||
               CollectionUtils.isNotEmpty(this.removeCores) ||
               CollectionUtils.isNotEmpty(this.removeClaims) ||
               this.hre != null ||
               this.baseTax != null ||
               this.baseProduction != null ||
               this.baseManpower != null ||
               this.tradeGood != null ||
               this.name != null ||
               this.tribalOwner != null ||
               this.advisor != null ||
               this.nativeHostileness != null ||
               this.nativeFerocity != null ||
               this.nativeSize != null ||
               CollectionUtils.isNotEmpty(this.discoveredBy) ||
               this.culture != null ||
               this.religion != null ||
               this.isCity != null ||
               MapUtils.isNotEmpty(this.buildings);
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCapital() {
        return capital;
    }

    public List<String> getAddCores() {
        return addCores;
    }

    public List<String> getAddClaims() {
        return addClaims;
    }

    public List<String> getRemoveCores() {
        return removeCores;
    }

    public List<String> getRemoveClaims() {
        return removeClaims;
    }

    public Boolean getHre() {
        return hre;
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

    public String getTradeGood() {
        return tradeGood;
    }

    public String getName() {
        return name;
    }

    public String getTribalOwner() {
        return tribalOwner;
    }

    public Integer getAdvisor() {
        return advisor;
    }

    public Integer getNativeHostileness() {
        return nativeHostileness;
    }

    public Integer getNativeFerocity() {
        return nativeFerocity;
    }

    public Integer getNativeSize() {
        return nativeSize;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @JsonIgnore
    public String getFakeOwner() {
        return fakeOwner;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public List<String> getDiscoveredBy() {
        return discoveredBy;
    }

    public String getCulture() {
        return culture;
    }

    public String getReligion() {
        return religion;
    }

    public Boolean getCity() {
        return isCity;
    }

    public Map<String, Boolean> getBuildings() {
        return buildings;
    }
}

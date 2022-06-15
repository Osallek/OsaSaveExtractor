package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.ProvinceRevolt;
import fr.osallek.eu4parser.model.save.province.SaveAdvisor;
import fr.osallek.eu4parser.model.save.province.SaveProvinceHistoryEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.List;

public class ProvinceHistoryDTO {

    private final LocalDate date;

    private final String capital;

    private final Double colonySize;

    private final Double unrest;

    private final List<String> addCores;

    private final List<String> addClaims;

    private final List<String> removeCores;

    private final List<String> removeClaims;

    private final Boolean hre;

    private final Integer baseTax;

    private final Integer baseProduction;

    private final Integer baseManpower;

    private final String tradeGood;

    private final NameDTO name;

    private final String tribalOwner;

    private final AdvisorDTO advisor;

    private final Integer nativeHostileness;

    private final Integer nativeFerocity;

    private final Integer nativeSize;

    private final String owner;

    private final String controller;

    private final String removeClaim;

    private final List<String> discoveredBy;

    private final String culture;

    private final String religion;

    private final Boolean isCity;

    private final List<String> buildings;

    public ProvinceHistoryDTO(SaveProvinceHistoryEvent event) {
        this.date = event.getDate();
        this.capital = event.getCapital();
        this.colonySize = event.getColonySize();
        this.unrest = event.getUnrest();
        this.addCores = event.getAddCores();
        this.addClaims = event.getAddClaims();
        this.removeCores = event.getRemoveCores();
        this.removeClaims = event.getRemoveClaims();
        this.hre = event.getHre();
        this.baseTax = event.getBaseTax();
        this.baseProduction = event.getBaseProduction();
        this.baseManpower = event.getBaseManpower();
        this.tradeGood = event.getTradeGood();
        this.name = event.getName() == null ? null : new NameDTO(event.getName().getKey(), event.getName().getValue());
        this.tribalOwner = event.getTribalOwner();
        this.advisor = event.getAdvisor() == null ? null : new AdvisorDTO(event.getAdvisor());
        this.nativeHostileness = event.getNativeHostileness();
        this.nativeFerocity = event.getNativeFerocity();
        this.nativeSize = event.getNativeSize();
        this.owner = event.getOwner();
        this.controller = event.getController();
        this.removeClaim = event.getRemoveClaim();
        this.discoveredBy = event.getDiscoveredBy();
        this.culture = event.getCulture();
        this.religion = event.getReligion();
        this.isCity = event.getIsCity();
        this.buildings = event.getBuildings();
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCapital() {
        return capital;
    }

    public Double getColonySize() {
        return colonySize;
    }

    public Double getUnrest() {
        return unrest;
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

    public Integer getBaseTax() {
        return baseTax;
    }

    public Integer getBaseProduction() {
        return baseProduction;
    }

    public Integer getBaseManpower() {
        return baseManpower;
    }

    public String getTradeGood() {
        return tradeGood;
    }

    public NameDTO getName() {
        return name;
    }

    public String getTribalOwner() {
        return tribalOwner;
    }

    public AdvisorDTO getAdvisor() {
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

    public String getController() {
        return controller;
    }

    public String getRemoveClaim() {
        return removeClaim;
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

    public List<String> getBuildings() {
        return buildings;
    }
}

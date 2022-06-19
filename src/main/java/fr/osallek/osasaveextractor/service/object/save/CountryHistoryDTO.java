package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.clausewitzparser.common.ClausewitzUtils;
import fr.osallek.eu4parser.model.Color;
import fr.osallek.eu4parser.model.Power;
import fr.osallek.eu4parser.model.save.country.Heir;
import fr.osallek.eu4parser.model.save.country.Monarch;
import fr.osallek.eu4parser.model.save.country.Queen;
import fr.osallek.eu4parser.model.save.country.SaveCountryHistoryEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class CountryHistoryDTO {

    private final LocalDate date;

    private final Boolean abolishedSerfdom;

    private final LeaderDTO leader;

    private final Map<String, Boolean> ideasLevel;

    private final List<String> addAcceptedCultures;

    private final List<String> removeAcceptedCultures;

    private final Integer governmentRank;

    private final Integer capital;

    private final String changedCountryNameFrom;

    private final String changedCountryAdjectiveFrom;

    private final ColorDTO changedCountryMapcolorFrom;

    private final Integer milTech;

    private final Integer admTech;

    private final Integer dipTech;

    private final String addGovernmentReform;

    private final String primaryCulture;

    private final String government;

    private final String religion;

    private final String secondaryReligion;

    private final String technologyGroup;

    private final String unitType;

    private final String changedTagFrom;

    private final String religiousSchool;

    private final String setCountryFlag;

    private final String decision;

    private final QueenDTO queen;

    private final QueenDTO monarchConsort;

    private final MonarchDTO monarch;

    private final HeirDTO monarchHeir;

    private final HeirDTO heir;

    private final HeirDTO monarchForeignHeir;

    private final Integer union;

    private final Integer tradePort;

    private final Boolean elector;

    public CountryHistoryDTO(SaveCountryHistoryEvent event) {
        this.date = event.getDate();
        this.abolishedSerfdom = event.getAbolishedSerfdom();
        this.leader = event.getLeader() == null ? null : new LeaderDTO(event.getLeader());
        this.ideasLevel = event.getIdeasLevel();
        this.addAcceptedCultures = event.getAddAcceptedCultures();
        this.removeAcceptedCultures = event.getRemoveAcceptedCultures();
        this.governmentRank = event.getGovernmentRank();
        this.capital = event.getCapital();
        this.changedCountryNameFrom = event.getChangedCountryNameFrom();
        this.changedCountryAdjectiveFrom = event.getChangedCountryAdjectiveFrom();
        this.changedCountryMapcolorFrom = event.getChangedCountryMapcolorFrom() == null ? null : new ColorDTO(event.getChangedCountryMapcolorFrom());
        this.milTech = event.getMilTech();
        this.admTech = event.getAdmTech();
        this.dipTech = event.getDipTech();
        this.addGovernmentReform = ClausewitzUtils.removeQuotes(event.getAddGovernmentReform());
        this.primaryCulture = event.getPrimaryCulture();
        this.government = event.getGovernment();
        this.religion = event.getReligion();
        this.secondaryReligion = event.getSecondaryReligion();
        this.technologyGroup = event.getTechnologyGroup();
        this.unitType = event.getUnitType();
        this.changedTagFrom = event.getChangedTagFrom();
        this.religiousSchool = event.getReligiousSchool();
        this.setCountryFlag = event.getSetCountryFlag();
        this.decision = event.getDecision();
        this.queen = event.getQueen() == null ? null : new QueenDTO(event.getQueen());
        this.monarchConsort = event.getMonarchConsort() == null ? null : new QueenDTO(event.getMonarchConsort());
        this.monarch = event.getMonarch() == null ? null : new MonarchDTO(event.getMonarch());
        this.monarchHeir = event.getMonarchHeir() == null ? null : new HeirDTO(event.getMonarchHeir());
        this.heir = event.getHeir() == null ? null : new HeirDTO(event.getHeir());
        this.monarchForeignHeir = event.getMonarchForeignHeir() == null ? null : new HeirDTO(event.getMonarchForeignHeir());
        this.union = event.getUnion();
        this.tradePort = event.getTradePort();
        this.elector = event.getElector();
    }

    public LocalDate getDate() {
        return date;
    }

    public Boolean getAbolishedSerfdom() {
        return abolishedSerfdom;
    }

    public LeaderDTO getLeader() {
        return leader;
    }

    public Map<String, Boolean> getIdeasLevel() {
        return ideasLevel;
    }

    public List<String> getAddAcceptedCultures() {
        return addAcceptedCultures;
    }

    public List<String> getRemoveAcceptedCultures() {
        return removeAcceptedCultures;
    }

    public Integer getGovernmentRank() {
        return governmentRank;
    }

    public Integer getCapital() {
        return capital;
    }

    public String getChangedCountryNameFrom() {
        return changedCountryNameFrom;
    }

    public String getChangedCountryAdjectiveFrom() {
        return changedCountryAdjectiveFrom;
    }

    public ColorDTO getChangedCountryMapcolorFrom() {
        return changedCountryMapcolorFrom;
    }

    public Integer getMilTech() {
        return milTech;
    }

    public Integer getAdmTech() {
        return admTech;
    }

    public Integer getDipTech() {
        return dipTech;
    }

    public String getAddGovernmentReform() {
        return addGovernmentReform;
    }

    public String getPrimaryCulture() {
        return primaryCulture;
    }

    public String getGovernment() {
        return government;
    }

    public String getReligion() {
        return religion;
    }

    public String getSecondaryReligion() {
        return secondaryReligion;
    }

    public String getTechnologyGroup() {
        return technologyGroup;
    }

    public String getUnitType() {
        return unitType;
    }

    public String getChangedTagFrom() {
        return changedTagFrom;
    }

    public String getReligiousSchool() {
        return religiousSchool;
    }

    public String getSetCountryFlag() {
        return setCountryFlag;
    }

    public String getDecision() {
        return decision;
    }

    public QueenDTO getQueen() {
        return queen;
    }

    public QueenDTO getMonarchConsort() {
        return monarchConsort;
    }

    public MonarchDTO getMonarch() {
        return monarch;
    }

    public HeirDTO getMonarchHeir() {
        return monarchHeir;
    }

    public HeirDTO getHeir() {
        return heir;
    }

    public HeirDTO getMonarchForeignHeir() {
        return monarchForeignHeir;
    }

    public Integer getUnion() {
        return union;
    }

    public Integer getTradePort() {
        return tradePort;
    }

    public Boolean getElector() {
        return elector;
    }
}

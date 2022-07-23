package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.clausewitzparser.common.ClausewitzUtils;
import fr.osallek.eu4parser.model.game.RulerPersonality;
import fr.osallek.eu4parser.model.save.country.Monarch;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.BooleanUtils;

public class MonarchDTO {

    private final LocalDate monarchDate;

    private final int id;

    private final String country;

    private final List<String> personalities;

    private final String name;

    private final int adm;

    private final int dip;

    private final int mil;

    private final boolean female;

    private final boolean regent;

    private final String culture;

    private final String religion;

    private final String dynasty;

    private final LocalDate birthDate;

    private LocalDate deathDate;

    private final String monarchName;

    private final LeaderDTO leader;

    public MonarchDTO(Monarch monarch) {
        this.monarchDate = monarch.getMonarchDate();
        this.id = monarch.getAdm();
        this.country = monarch.getCountry().getTag();
        this.personalities = monarch.getPersonalities() == null ? null :
                             monarch.getPersonalities().getPersonalities().stream().filter(Objects::nonNull).map(RulerPersonality::getName).toList();
        this.name = ClausewitzUtils.removeQuotes(monarch.getName());
        this.adm = monarch.getAdm();
        this.dip = monarch.getDip();
        this.mil = monarch.getMil();
        this.female = BooleanUtils.toBoolean(monarch.getFemale());
        this.regent = BooleanUtils.toBoolean(monarch.getRegent());
        this.culture = monarch.getCultureName();
        this.religion = monarch.getReligionName();
        this.dynasty = ClausewitzUtils.removeQuotes(monarch.getDynasty());
        this.birthDate = monarch.getBirthDate();
        this.deathDate = monarch.getDeathDate();
        this.monarchName = ClausewitzUtils.removeQuotes(monarch.getMonarchName());
        this.leader = monarch.getLeader() == null ? null : new LeaderDTO(monarch.getLeader());
    }

    public LocalDate getMonarchDate() {
        return monarchDate;
    }

    public int getId() {
        return id;
    }

    public String getCountry() {
        return country;
    }

    public List<String> getPersonalities() {
        return personalities;
    }

    public String getName() {
        return name;
    }

    public int getAdm() {
        return adm;
    }

    public int getDip() {
        return dip;
    }

    public int getMil() {
        return mil;
    }

    public boolean isFemale() {
        return female;
    }

    public boolean isRegent() {
        return regent;
    }

    public String getCulture() {
        return culture;
    }

    public String getReligion() {
        return religion;
    }

    public String getDynasty() {
        return dynasty;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public LocalDate getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(LocalDate deathDate) {
        this.deathDate = deathDate;
    }

    public String getMonarchName() {
        return monarchName;
    }

    public LeaderDTO getLeader() {
        return leader;
    }
}

package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.clausewitzparser.common.ClausewitzUtils;
import fr.osallek.eu4parser.model.save.province.SaveAdvisor;
import org.apache.commons.lang3.BooleanUtils;

import java.time.LocalDate;

public class AdvisorDTO {

    private final int id;

    private final String name;

    private final String type;

    private final LocalDate birth;

    private final LocalDate hire;

    private final LocalDate death;

    private final int skill;

    private final int location;

    private final boolean female;

    private final String culture;

    private final String religion;

    public AdvisorDTO(SaveAdvisor advisor) {
        this.id = advisor.getId().getId();
        this.name = ClausewitzUtils.removeQuotes(advisor.getName());
        this.type = advisor.getType();
        this.birth = advisor.getDate();
        this.hire = advisor.getHireDate();
        this.death = advisor.getDeathDate();
        this.skill = advisor.getSkill();
        this.location = advisor.getLocation().getId();
        this.female = BooleanUtils.isTrue(advisor.getFemale());
        this.culture = advisor.getCultureName();
        this.religion = advisor.getReligionName();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public LocalDate getBirth() {
        return birth;
    }

    public LocalDate getHire() {
        return hire;
    }

    public LocalDate getDeath() {
        return death;
    }

    public int getSkill() {
        return skill;
    }

    public int getLocation() {
        return location;
    }

    public boolean isFemale() {
        return female;
    }

    public String getCulture() {
        return culture;
    }

    public String getReligion() {
        return religion;
    }
}

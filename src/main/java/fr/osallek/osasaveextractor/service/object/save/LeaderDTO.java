package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.clausewitzparser.common.ClausewitzUtils;
import fr.osallek.eu4parser.model.save.country.Leader;
import fr.osallek.eu4parser.model.save.country.LeaderType;
import java.time.temporal.ChronoUnit;
import org.apache.commons.lang3.BooleanUtils;

import java.time.LocalDate;

public class LeaderDTO {

    private final int id;

    private final String name;

    private final LeaderType type;

    private final boolean female;

    private final int manuever;

    private final int fire;

    private final int shock;

    private final int siege;

    private final String personality;

    private final LocalDate activation;

    private LocalDate deathDate;

    private Integer duration;

    public LeaderDTO(Leader leader) {
        this.id = leader.getId().getId();
        this.name = ClausewitzUtils.removeQuotes(leader.getName());
        this.type = leader.getType();
        this.female = BooleanUtils.toBoolean(leader.getFemale());
        this.manuever = leader.getManuever();
        this.fire = leader.getFire();
        this.shock = leader.getShock();
        this.siege = leader.getSiege();
        this.personality = leader.getPersonality() == null ? null : leader.getPersonality().getName();
        this.activation = leader.getActivation();
        this.deathDate = leader.getDeathDate();
        this.duration = (this.deathDate == null || this.activation == null) ? null : (int) ChronoUnit.MONTHS.between(this.activation, this.deathDate);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LeaderType getType() {
        return type;
    }

    public boolean isFemale() {
        return female;
    }

    public int getManuever() {
        return manuever;
    }

    public int getFire() {
        return fire;
    }

    public int getShock() {
        return shock;
    }

    public int getSiege() {
        return siege;
    }

    public String getPersonality() {
        return personality;
    }

    public LocalDate getActivation() {
        return activation;
    }

    public LocalDate getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(LocalDate deathDate) {
        if (deathDate != null) {
            this.deathDate = deathDate;

            if (this.activation != null) {
                this.duration = (int) ChronoUnit.MONTHS.between(this.activation, this.deathDate);
            }
        }
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}

package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.SaveReligion;
import fr.osallek.osasaveextractor.common.Constants;
import java.time.LocalDate;
import org.apache.commons.lang3.BooleanUtils;

public class ReligionDTO {

    private final String group;

    private final String name;

    private final ColorDTO color;

    private final Integer icon;

    private final boolean hreReligion;

    private final boolean hreHereticReligion;

    private final LocalDate enable;

    private final int nbProvinces;

    private final String defender;

    public ReligionDTO(SaveReligion religion) {
        this.group = religion.getReligionGroup().getName();
        this.name = religion.getName();
        this.color = religion.getGameReligion().getColor() == null ? Constants.stringToColor(this.name) : new ColorDTO(religion.getGameReligion().getColor());
        this.icon = religion.getGameReligion().getIcon();
        this.hreReligion = BooleanUtils.toBoolean(religion.isHreReligion());
        this.hreHereticReligion = BooleanUtils.toBoolean(religion.isHreHereticReligion());
        this.enable = religion.getEnable();
        this.nbProvinces = religion.getAmountOfProvinces();
        this.defender = religion.getDefender() == null ? null : religion.getDefender().getTag();
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public ColorDTO getColor() {
        return color;
    }

    public Integer getIcon() {
        return icon;
    }

    public boolean isHreReligion() {
        return hreReligion;
    }

    public boolean isHreHereticReligion() {
        return hreHereticReligion;
    }

    public LocalDate getEnable() {
        return enable;
    }

    public int getNbProvinces() {
        return nbProvinces;
    }

    public String getDefender() {
        return defender;
    }
}

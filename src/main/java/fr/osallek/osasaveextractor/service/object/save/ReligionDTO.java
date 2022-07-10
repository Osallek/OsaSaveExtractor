package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.Religion;
import fr.osallek.eu4parser.model.save.Save;
import fr.osallek.eu4parser.model.save.SaveReligion;
import fr.osallek.osasaveextractor.common.Constants;
import org.apache.commons.lang3.BooleanUtils;

import java.time.LocalDate;

public class ReligionDTO extends NamedImageLocalisedDTO {

    private final String group;

    private final ColorDTO color;

    private final Integer icon;

    private final boolean hreReligion;

    private final boolean hreHereticReligion;

    private final LocalDate enable;

    private final int nbProvinces;

    private final String defender;

    public ReligionDTO(Save save, SaveReligion saveReligion, Religion religion) {
        super(save.getGame().getLocalisation(saveReligion.getName()), religion.getWritenTo(), saveReligion.getName());
        this.group = saveReligion.getReligionGroup().getName();
        this.color = religion.getColor() == null ? Constants.stringToColor(this.name) : new ColorDTO(religion.getColor());
        this.icon = religion.getIcon();
        this.hreReligion = BooleanUtils.toBoolean(saveReligion.isHreReligion());
        this.hreHereticReligion = BooleanUtils.toBoolean(saveReligion.isHreHereticReligion());
        this.enable = saveReligion.getEnable();
        this.nbProvinces = saveReligion.getAmountOfProvinces();
        this.defender = saveReligion.getDefender() == null ? null : saveReligion.getDefender().getTag();
    }

    public String getGroup() {
        return group;
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

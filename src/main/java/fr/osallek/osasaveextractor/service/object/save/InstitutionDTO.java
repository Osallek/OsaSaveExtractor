package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.Institution;
import fr.osallek.eu4parser.model.save.Save;

public class InstitutionDTO extends NamedImageLocalisedDTO {

    private Integer origin;

    public InstitutionDTO(Save save, Institution institution, Integer origin) {
        super(save.getGame().getLocalisation(institution.getName()), institution.getImage(), institution.getName());
        this.origin = origin;
    }

    public Integer getOrigin() {
        return origin;
    }

    public void setOrigin(Integer origin) {
        this.origin = origin;
    }
}

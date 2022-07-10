package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.Institution;
import fr.osallek.eu4parser.model.save.Save;

public class InstitutionDTO extends NamedImageLocalisedDTO {

    private int origin;

    public InstitutionDTO(Save save, Institution institution, int origin) {
        super(save.getGame().getLocalisation(institution.getName()), institution.getImage(), institution.getName());
        this.origin = origin;
    }

    public int getOrigin() {
        return origin;
    }

    public void setOrigin(int origin) {
        this.origin = origin;
    }
}

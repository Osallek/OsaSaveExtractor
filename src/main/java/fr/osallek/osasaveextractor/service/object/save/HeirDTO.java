package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.common.NumbersUtils;
import fr.osallek.eu4parser.model.game.RulerPersonality;
import fr.osallek.eu4parser.model.save.country.Heir;
import fr.osallek.eu4parser.model.save.country.Monarch;
import java.time.LocalDate;
import java.util.List;

public class HeirDTO extends MonarchDTO {

    private final double claim;

    public HeirDTO(Heir heir) {
        super(heir);
        this.claim = NumbersUtils.doubleOrDefault(heir.getClaim());
    }

    public double getClaim() {
        return claim;
    }
}

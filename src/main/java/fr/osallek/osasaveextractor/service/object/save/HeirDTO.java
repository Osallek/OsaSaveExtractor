package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.common.NumbersUtils;
import fr.osallek.eu4parser.model.save.country.Heir;
import java.time.LocalDate;

public class HeirDTO extends MonarchDTO {

    private final double claim;

    public HeirDTO(Heir heir, LocalDate date) {
        super(heir, date);
        this.claim = NumbersUtils.doubleOrDefault(heir.getClaim());
    }

    public double getClaim() {
        return claim;
    }
}

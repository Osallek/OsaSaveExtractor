package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.clausewitzparser.common.ClausewitzUtils;
import fr.osallek.eu4parser.model.save.empire.OldEmperor;
import java.time.LocalDate;
import java.util.Comparator;

public class OldEmperorDTO implements Comparable<OldEmperorDTO> {

    private final String country;

    private final LocalDate date;

    private final int id;

    public OldEmperorDTO(OldEmperor oldEmperor) {
        this.country = ClausewitzUtils.removeQuotes(oldEmperor.getCountry());
        this.date = oldEmperor.getDate();
        this.id = oldEmperor.getId();
    }

    public String getCountry() {
        return country;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(OldEmperorDTO o) {
        return Comparator.comparing(OldEmperorDTO::getDate).compare(this, o);
    }
}

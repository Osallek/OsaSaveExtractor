package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.clausewitzparser.common.ClausewitzUtils;
import fr.osallek.eu4parser.model.save.province.SaveProvince;

import java.util.Comparator;
import java.util.Objects;


public class SimpleProvinceDTO implements Comparable<SimpleProvinceDTO> {

    protected final int id;

    protected final String name;

    public SimpleProvinceDTO(SaveProvince province) {
        this.id = province.getId();
        this.name = ClausewitzUtils.removeQuotes(province.getName());
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(SimpleProvinceDTO o) {
        return Comparator.comparingInt(SimpleProvinceDTO::getId).compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof SimpleProvinceDTO that)) {
            return false;
        }

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

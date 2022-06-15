package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.clausewitzparser.common.ClausewitzUtils;
import fr.osallek.eu4parser.common.Eu4MapUtils;
import fr.osallek.eu4parser.model.save.province.SaveProvince;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class ProvinceDTO implements Comparable<ProvinceDTO> {

    private final int id;

    private final String name;

    private final ColorDTO color;

    private final List<ProvinceHistoryDTO> history = new ArrayList<>();

    public ProvinceDTO(SaveProvince province) {
        this.id = province.getId();
        this.name = ClausewitzUtils.removeQuotes(province.getName());

        if (!province.isImpassable() && !province.isOcean() && !province.isLake()) {
            if (province.getOwner() != null) {
                this.color = new ColorDTO(province.getOwner().getColor());
            } else {
                this.color = new ColorDTO(Eu4MapUtils.EMPTY_COLOR);
            }
        } else {
            this.color = null;
        }

        if (province.getHistory() != null) {
            this.history.addAll(province.getHistory().getEvents().stream().map(ProvinceHistoryDTO::new).toList());
            this.history.add(new ProvinceHistoryDTO(province.getHistory()));
            this.history.sort(Comparator.comparing(ProvinceHistoryDTO::getDate));
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ColorDTO getColor() {
        return color;
    }

    public List<ProvinceHistoryDTO> getHistory() {
        return history;
    }

    @Override
    public int compareTo(ProvinceDTO o) {
        return Comparator.comparingInt(ProvinceDTO::getId).compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ProvinceDTO that)) {
            return false;
        }

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

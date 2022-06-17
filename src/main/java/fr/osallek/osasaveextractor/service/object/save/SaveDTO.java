package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.Save;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

public class SaveDTO {

    private final String id;

    private final String name;

    private final LocalDate date;

    private final int nbProvinces;

    private final List<TeamDTO> teams;

    private final SortedSet<ProvinceDTO> provinces;

    private final SortedSet<SimpleProvinceDTO> oceansProvinces;

    private final SortedSet<SimpleProvinceDTO> lakesProvinces;

    private final SortedSet<SimpleProvinceDTO> impassableProvinces;

    private final List<CountryDTO> countries;

    private final List<AreaDTO> areas;

    private final List<AdvisorDTO> advisors;

    public SaveDTO(Save save) {
        this.id = UUID.randomUUID().toString();
        this.name = save.getName();
        this.date = save.getDate();
        this.nbProvinces = Collections.max(save.getGame().getProvinces().keySet()); //Get the greatest id

        this.teams = CollectionUtils.isNotEmpty(save.getTeams()) ? save.getTeams().stream().map(TeamDTO::new).toList() : null;

        this.provinces = new TreeSet<>();
        this.oceansProvinces = new TreeSet<>();
        this.lakesProvinces = new TreeSet<>();
        this.impassableProvinces = new TreeSet<>();
        save.getProvinces().values().forEach(province -> {
            if (province.isImpassable()) {
                this.impassableProvinces.add(new SimpleProvinceDTO(province));
            } else if (province.isOcean()) {
                this.oceansProvinces.add(new SimpleProvinceDTO(province));
            } else if (province.isLake()) {
                this.lakesProvinces.add(new SimpleProvinceDTO(province));
            } else {
                this.provinces.add(new ProvinceDTO(province));
            }
        });

        this.areas = save.getAreas().values().stream().map(AreaDTO::new).collect(Collectors.toList());
        this.advisors = save.getAdvisors().values().stream().map(AdvisorDTO::new).collect(Collectors.toList());
        this.countries = save.getCountries().values().stream().map(country -> new CountryDTO(country, save.getDiplomacy())).toList();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getNbProvinces() {
        return nbProvinces;
    }

    public List<TeamDTO> getTeams() {
        return teams;
    }

    public SortedSet<ProvinceDTO> getProvinces() {
        return provinces;
    }

    public SortedSet<SimpleProvinceDTO> getOceansProvinces() {
        return oceansProvinces;
    }

    public SortedSet<SimpleProvinceDTO> getLakesProvinces() {
        return lakesProvinces;
    }

    public SortedSet<SimpleProvinceDTO> getImpassableProvinces() {
        return impassableProvinces;
    }

    public List<CountryDTO> getCountries() {
        return countries;
    }

    public List<AreaDTO> getAreas() {
        return areas;
    }

    public List<AdvisorDTO> getAdvisors() {
        return advisors;
    }
}

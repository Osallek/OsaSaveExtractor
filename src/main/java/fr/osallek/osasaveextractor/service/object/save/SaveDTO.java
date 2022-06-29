package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.Save;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class SaveDTO {

    private final String id;

    private final String name;

    private final LocalDate date;

    private final int nbProvinces;

    private final List<TeamDTO> teams;

    private final SortedSet<ProvinceDTO> provinces = new ConcurrentSkipListSet<>();

    private final SortedSet<SimpleProvinceDTO> oceansProvinces = new ConcurrentSkipListSet<>();

    private final SortedSet<SimpleProvinceDTO> lakesProvinces = new ConcurrentSkipListSet<>();

    private final SortedSet<SimpleProvinceDTO> impassableProvinces = new ConcurrentSkipListSet<>();

    private final List<CountryDTO> countries;

    private final List<AreaDTO> areas;

    private final List<AdvisorDTO> advisors;

    private final List<CultureDTO> cultures;

    private final List<ReligionDTO> religions;

    private final HreDTO hre;

    private final CelestialEmpireDTO celestialEmpire;

    private final List<Integer> institutions;

    private final DiplomacyDTO diplomacy;

    public SaveDTO(Save save) {
        this.id = UUID.randomUUID().toString();
        this.name = save.getName();
        this.date = save.getDate();
        this.nbProvinces = Collections.max(save.getGame().getProvinces().keySet()); //Get the greatest id
        this.teams = CollectionUtils.isNotEmpty(save.getTeams()) ? save.getTeams().stream().map(TeamDTO::new).toList() : null;

        save.getProvinces().values().parallelStream().forEach(province -> {
            if (province.isImpassable()) {
                this.impassableProvinces.add(new SimpleProvinceDTO(province));
            } else if (province.isOcean()) {
                this.oceansProvinces.add(new SimpleProvinceDTO(province));
            } else if (province.isLake()) {
                this.lakesProvinces.add(new SimpleProvinceDTO(province));
            } else if (province.getHistory() != null) {
                this.provinces.add(new ProvinceDTO(province));
            }
        });

        this.areas = save.getAreas().values().stream().map(AreaDTO::new).collect(Collectors.toList());
        this.advisors = save.getAdvisors().values().stream().map(AdvisorDTO::new).collect(Collectors.toList());
        this.countries = save.getCountries().values().parallelStream().map(country -> {
            CountryDTO countryDTO = new CountryDTO(country, save.getDiplomacy());

            countryDTO.getHistory().stream().filter(history -> StringUtils.isNotBlank(history.getChangedTagFrom())).forEach(history -> {
                this.provinces.stream()
                              .filter(province -> province.isOwnerAt(history.getDate(), history.getChangedTagFrom()))
                              .forEach(province -> province.addOwner(history.getDate(), countryDTO.getTag()));
                this.provinces.stream() //Add owner when inheriting from decision
                              .filter(province -> CollectionUtils.isNotEmpty(province.getHistory()) && province.getHistory()
                                                                                                               .stream()
                                                                                                               .anyMatch(h -> h.getDate()
                                                                                                                               .equals(history.getDate())
                                                                                                                              && country.getTag()
                                                                                                                                        .equals(h.getFakeOwner())))
                              .forEach(province -> province.addOwner(history.getDate(), countryDTO.getTag()));
            });
            return countryDTO;
        }).toList();
        this.cultures = save.getGame().getCultures().stream().map(CultureDTO::new).toList();
        this.religions = save.getReligions().getReligions().values().stream().filter(r -> r.getGameReligion() != null).map(ReligionDTO::new).toList();
        this.hre = new HreDTO(save.getHre());
        this.celestialEmpire = new CelestialEmpireDTO(save.getCelestialEmpire());
        this.institutions = save.getInstitutions()
                                .getOrigins()
                                .stream()
                                .map(saveProvince -> saveProvince == null ? 0 : saveProvince.getId())
                                .toList()
                                .subList(0, (int) save.getInstitutions().getNbAvailable());
        this.diplomacy = new DiplomacyDTO(save.getDiplomacy());
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

    public List<CultureDTO> getCultures() {
        return cultures;
    }

    public List<ReligionDTO> getReligions() {
        return religions;
    }

    public HreDTO getHre() {
        return hre;
    }

    public CelestialEmpireDTO getCelestialEmpire() {
        return celestialEmpire;
    }

    public List<Integer> getInstitutions() {
        return institutions;
    }

    public DiplomacyDTO getDiplomacy() {
        return diplomacy;
    }
}

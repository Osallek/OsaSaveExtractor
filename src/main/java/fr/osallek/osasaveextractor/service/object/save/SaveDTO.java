package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.clausewitzparser.common.ClausewitzUtils;
import fr.osallek.eu4parser.common.NumbersUtils;
import fr.osallek.eu4parser.model.game.Religion;
import fr.osallek.eu4parser.model.save.Save;
import fr.osallek.eu4parser.model.save.country.SaveCountry;
import fr.osallek.eu4parser.model.save.province.SaveProvince;
import fr.osallek.osasaveextractor.common.Constants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SaveDTO {

    private final LocalDate startDate;

    private final boolean hideAll;

    private final String owner;

    private final String country;

    private final Localised countryName;

    private final String version;

    private final String previousSave;

    private final String name;

    private final String provinceImage;

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

    private final List<InstitutionDTO> institutions;

    private final DiplomacyDTO diplomacy;

    private final List<NamedImageLocalisedDTO> buildings;

    private final List<NamedImageLocalisedDTO> advisorTypes;

    private final List<ColorNamedImageLocalisedDTO> tradeGoods;

    private final List<ColorNamedImageLocalisedDTO> estates;

    private final List<NamedImageLocalisedDTO> estatePrivileges;

    private final List<NamedLocalisedDTO> subjectTypes;

    private final List<IdeaGroupDTO> ideaGroups;

    private final List<NamedImageLocalisedDTO> personalities;

    private final List<NamedImageLocalisedDTO> leaderPersonalities;

    private final List<WarDTO> wars;

    private final List<TradeNodeDTO> tradeNodes;

    public SaveDTO(String userId, String name, String previousSave, Save save, boolean hideAll, String provinceImage, Map<String, Religion> religions,
                   DoubleConsumer percentCountriesConsumer) {
        this.startDate = save.getStartDate();
        this.owner = userId;
        this.country = save.getPlayedCountry().getTag();
        this.version = ClausewitzUtils.removeQuotes(save.getSavegameVersions().get(0));
        this.previousSave = previousSave;
        this.hideAll = hideAll;
        this.provinceImage = provinceImage;
        this.name = name;
        this.date = save.getDate();
        this.nbProvinces = Collections.max(save.getGame().getProvinces().keySet()); //Get the greatest id
        this.teams = CollectionUtils.isNotEmpty(save.getTeams()) ? save.getTeams().stream().map(TeamDTO::new).toList() : null;

        save.getProvinces().values().forEach(province -> {
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

        this.areas = save.getAreas().values().stream().map(AreaDTO::new).toList();
        this.advisors = save.getAdvisors().values().stream().map(AdvisorDTO::new).toList();

        AtomicInteger i = new AtomicInteger();
        List<SaveCountry> list = save.getCountries()
                                     .values()
                                     .stream()
                                     .filter(Predicate.not(SaveCountry::isObserver))
                                     .filter(c -> !"REB".equals(c.getTag()))
                                     .filter(c -> c.getHistory() != null)
                                     .filter(c -> c.getHistory().hasEvents())
                                     .toList();
        this.countries = list.parallelStream()
                             .map(c -> {
                                 CountryDTO countryDTO = new CountryDTO(save, c, save.getDiplomacy(), this.provinces);
                                 i.getAndIncrement();
                                 percentCountriesConsumer.accept((double) i.get() / list.size());

                                 return countryDTO;
                             })
                             .toList();

        for (CountryDTO countryDTO : this.countries) { //Not in stream to prevent Concurrent modification
            List<CountryHistoryDTO> changes = countryDTO.getHistory().stream().filter(history -> StringUtils.isNotBlank(history.getChangedTagFrom())).toList();

            if (CollectionUtils.isNotEmpty(changes)) {
                for (int j = 0; j < changes.size(); j++) {
                    CountryHistoryDTO history = changes.get(j);

                    String tag = changes.size() == (j + 1) ? countryDTO.getTag() : changes.get(j + 1).getChangedTagFrom(); //If multiple changes use right tag

                    this.provinces.stream()
                                  .filter(province -> province.isOwnerAt(history.getDate(), history.getChangedTagFrom()))
                                  .forEach(province -> province.addOwner(history.getDate(), tag));
                    this.provinces.stream() //Add owner when inheriting from decision
                                  .filter(province -> CollectionUtils.isNotEmpty(province.getHistory())
                                                      && province.getHistory().stream().anyMatch(h -> h.getDate().equals(history.getDate())
                                                                                                      && countryDTO.getTag().equals(h.getFakeOwner())))
                                  .forEach(province -> province.addOwner(history.getDate(), tag));

                    countryDTO.setDev(this.provinces.stream()
                                                    .filter(p -> p.isOwnerAt(save.getDate(), countryDTO.getTag()))
                                                    .mapToDouble(p -> NumbersUtils.doubleOrDefault(p.getBaseTax())
                                                                      + NumbersUtils.doubleOrDefault(p.getBaseProduction())
                                                                      + NumbersUtils.doubleOrDefault(p.getBaseManpower()))
                                                    .sum());
                    countryDTO.setNbProvince((int) provinces.stream().filter(p -> p.isOwnerAt(save.getDate(), countryDTO.getTag())).count());

                    if (CollectionUtils.isNotEmpty(countryDTO.getPlayers())) {
                        this.countries.stream()
                                      .filter(c -> c.getTag().equals(history.getChangedTagFrom()))
                                      .findFirst()
                                      .ifPresent(c -> c.setPlayers(
                                              new LinkedHashSet<>(CollectionUtils.collate(ObjectUtils.firstNonNull(c.getPlayers(), new ArrayList<>()),
                                                                                          countryDTO.getPlayers()))));
                    }
                }
            }
        }

        this.cultures = save.getGame().getCultures().stream().map(culture -> new CultureDTO(save, culture)).toList();
        this.religions = save.getReligions()
                             .getReligions()
                             .values()
                             .stream()
                             .filter(r -> religions.containsKey(r.getName()))
                             .map(r -> new ReligionDTO(save, r, religions.get(r.getName())))
                             .toList();
        this.hre = new HreDTO(save.getHre());
        this.celestialEmpire = new CelestialEmpireDTO(save.getCelestialEmpire());
        this.institutions = save.getGame()
                                .getInstitutions()
                                .stream()
                                .filter(institution -> save.getInstitutions().isAvailable(institution))
                                .map(institution -> {
                                    SaveProvince origin = save.getInstitutions().getOrigin(institution);
                                    return origin != null ? new InstitutionDTO(save, institution, save.getInstitutions().getOrigin(institution).getId())
                                                          : new InstitutionDTO(save, institution, 0);
                                })
                                .toList();
        this.diplomacy = new DiplomacyDTO(save, list);
        this.buildings = save.getGame()
                             .getBuildings()
                             .stream()
                             .map(building -> new NamedImageLocalisedDTO(save.getGame().getLocalisation("building_" + building.getName()),
                                                                         building.getImage(), building.getName()))
                             .toList();
        this.advisorTypes = save.getGame()
                                .getAdvisors()
                                .stream()
                                .map(advisor -> new NamedImageLocalisedDTO(save.getGame().getLocalisation(advisor.getName()), advisor.getDefaultImage(),
                                                                           advisor.getName()))
                                .toList();
        this.tradeGoods = save.getGame()
                              .getTradeGoods()
                              .stream()
                              .map(tradeGood -> new ColorNamedImageLocalisedDTO(save.getGame().getLocalisation(tradeGood.getName()),
                                                                                tradeGood.getWritenTo(), tradeGood.getName(),
                                                                                tradeGood.getColor() == null ? Constants.stringToColor(this.name) :
                                                                                new ColorDTO(tradeGood.getColor())))
                              .toList();
        this.estates = this.countries.stream()
                                     .map(CountryDTO::getEstates)
                                     .filter(CollectionUtils::isNotEmpty)
                                     .flatMap(Collection::stream)
                                     .map(EstateDTO::getType)
                                     .distinct()
                                     .map(s -> save.getGame().getEstate(s))
                                     .filter(Objects::nonNull)
                                     .map(estate -> new ColorNamedImageLocalisedDTO(save.getGame().getLocalisation(estate.getName()),
                                                                                    estate.getWritenTo(), estate.getName(),
                                                                                    estate.getColor() == null ? Constants.stringToColor(this.name) :
                                                                                    new ColorDTO(estate.getColor())))
                                     .toList();
        this.estatePrivileges = this.countries.stream()
                                              .map(CountryDTO::getEstates)
                                              .filter(CollectionUtils::isNotEmpty)
                                              .flatMap(Collection::stream)
                                              .map(EstateDTO::getGrantedPrivileges)
                                              .flatMap(Collection::stream)
                                              .distinct()
                                              .map(s -> save.getGame().getEstatePrivilege(s))
                                              .filter(Objects::nonNull)
                                              .map(privilege -> new NamedImageLocalisedDTO(save.getGame().getLocalisation(privilege.getName()),
                                                                                           privilege.getImage(), privilege.getName()))
                                              .toList();
        this.subjectTypes = this.diplomacy.getDependencies()
                                          .stream()
                                          .map(DependencyDTO::getType)
                                          .distinct()
                                          .map(s -> save.getGame().getSubjectType(s))
                                          .filter(Objects::nonNull)
                                          .map(type -> new NamedLocalisedDTO(save.getGame().getLocalisation(type.getName() + "_title"), type.getName()))
                                          .toList();
        this.ideaGroups = this.countries.stream()
                                        .map(CountryDTO::getIdeaGroups)
                                        .filter(MapUtils::isNotEmpty)
                                        .map(Map::keySet)
                                        .flatMap(Collection::stream)
                                        .distinct()
                                        .map(s -> save.getGame().getIdeaGroup(s))
                                        .filter(Objects::nonNull)
                                        .map(group -> new IdeaGroupDTO(save, group))
                                        .toList();
        this.personalities = this.countries.stream()
                                           .map(CountryDTO::getHistory)
                                           .flatMap(Collection::stream)
                                           .map(CountryHistoryDTO::getMonarch)
                                           .filter(Objects::nonNull)
                                           .map(MonarchDTO::getPersonalities)
                                           .filter(CollectionUtils::isNotEmpty)
                                           .flatMap(Collection::stream)
                                           .distinct()
                                           .map(s -> save.getGame().getRulerPersonality(s))
                                           .filter(Objects::nonNull)
                                           .map(personality -> new NamedImageLocalisedDTO(save.getGame().getLocalisation(personality.getName()),
                                                                                          personality.getImage(), personality.getName()))
                                           .toList();
        this.leaderPersonalities = this.countries.stream()
                                                 .map(CountryDTO::getHistory)
                                                 .flatMap(Collection::stream)
                                                 .map(CountryHistoryDTO::getLeader)
                                                 .filter(Objects::nonNull)
                                                 .map(LeaderDTO::getPersonality)
                                                 .filter(Objects::nonNull)
                                                 .distinct()
                                                 .map(s -> save.getGame().getLeaderPersonality(s))
                                                 .filter(Objects::nonNull)
                                                 .map(personality -> new NamedImageLocalisedDTO(save.getGame().getLocalisation(personality.getName()),
                                                                                                personality.getModifiers().getImage(save.getGame()),
                                                                                                personality.getName()))
                                                 .toList();

        this.countryName = this.countries.stream().filter(c -> this.country.equals(c.getTag())).findFirst().map(Localised::new).orElse(null);
        AtomicInteger warId = new AtomicInteger(1);
        this.wars = Stream.concat(save.getActiveWars().stream(), save.getPreviousWars().stream())
                          .filter(war -> MapUtils.isNotEmpty(war.getActionsHistory()))
                          .filter(war -> MapUtils.isNotEmpty(war.getPersistentAttackers()) && MapUtils.isNotEmpty(war.getPersistentDefenders()))
                          .map(war -> new WarDTO(warId.getAndIncrement(), war, save.getDate()))
                          .filter(war -> war.getEndDate() == null || war.getEndDate().isAfter(this.startDate))
                          .sorted(Comparator.comparing(WarDTO::getStartDate))
                          .collect(Collectors.toList());
        this.tradeNodes = save.getTradeNodes().values().stream().map(node -> new TradeNodeDTO(save, node)).toList();
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public boolean isHideAll() {
        return hideAll;
    }

    public String getOwner() {
        return owner;
    }

    public String getCountry() {
        return country;
    }

    public Localised getCountryName() {
        return countryName;
    }

    public String getVersion() {
        return version;
    }

    public String getPreviousSave() {
        return previousSave;
    }

    public String getName() {
        return name;
    }

    public String getProvinceImage() {
        return provinceImage;
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

    public List<InstitutionDTO> getInstitutions() {
        return institutions;
    }

    public DiplomacyDTO getDiplomacy() {
        return diplomacy;
    }

    public List<NamedImageLocalisedDTO> getBuildings() {
        return buildings;
    }

    public List<NamedImageLocalisedDTO> getAdvisorTypes() {
        return advisorTypes;
    }

    public List<ColorNamedImageLocalisedDTO> getTradeGoods() {
        return tradeGoods;
    }

    public List<ColorNamedImageLocalisedDTO> getEstates() {
        return estates;
    }

    public List<NamedImageLocalisedDTO> getEstatePrivileges() {
        return estatePrivileges;
    }

    public List<NamedLocalisedDTO> getSubjectTypes() {
        return subjectTypes;
    }

    public List<IdeaGroupDTO> getIdeaGroups() {
        return ideaGroups;
    }

    public List<NamedImageLocalisedDTO> getPersonalities() {
        return personalities;
    }

    public List<NamedImageLocalisedDTO> getLeaderPersonalities() {
        return leaderPersonalities;
    }

    public List<WarDTO> getWars() {
        return wars;
    }

    public List<TradeNodeDTO> getTradeNodes() {
        return tradeNodes;
    }
}

package fr.osallek.osasaveextractor.service.object.server;

import fr.osallek.osasaveextractor.service.object.save.AdvisorDTO;
import fr.osallek.osasaveextractor.service.object.save.CountryDTO;
import fr.osallek.osasaveextractor.service.object.save.InstitutionDTO;
import fr.osallek.osasaveextractor.service.object.save.NamedImageLocalisedDTO;
import fr.osallek.osasaveextractor.service.object.save.ReligionDTO;
import fr.osallek.osasaveextractor.service.object.save.SaveDTO;
import fr.osallek.osasaveextractor.service.object.save.TradeGoodDTO;
import java.util.Set;
import java.util.stream.Collectors;

public record AssetsDTO(boolean provinces, boolean colors, Set<String> countries, Set<String> advisors, Set<String> institutions, Set<String> buildings,
                        Set<String> religions, Set<String> tradeGoods) {

    public AssetsDTO(SaveDTO save) {
        this(true,
             true,
             save.getCountries().stream().map(CountryDTO::getTag).collect(Collectors.toSet()),
             save.getAdvisors().stream().map(AdvisorDTO::getType).collect(Collectors.toSet()),
             save.getInstitutions().stream().map(InstitutionDTO::getName).collect(Collectors.toSet()),
             save.getBuildings().stream().map(NamedImageLocalisedDTO::getName).collect(Collectors.toSet()),
             save.getReligions().stream().map(ReligionDTO::getName).collect(Collectors.toSet()),
             save.getTradeGoods().stream().map(TradeGoodDTO::getName).collect(Collectors.toSet()));
    }
}

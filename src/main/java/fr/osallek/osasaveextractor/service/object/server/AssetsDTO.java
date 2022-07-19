package fr.osallek.osasaveextractor.service.object.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.osallek.osasaveextractor.service.object.save.CountryDTO;
import fr.osallek.osasaveextractor.service.object.save.InstitutionDTO;
import fr.osallek.osasaveextractor.service.object.save.NamedImageLocalisedDTO;
import fr.osallek.osasaveextractor.service.object.save.ReligionDTO;
import fr.osallek.osasaveextractor.service.object.save.SaveDTO;
import fr.osallek.osasaveextractor.service.object.save.TradeGoodDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;

public record AssetsDTO(boolean provinces, boolean colors, Set<String> countries, Set<String> advisors, Set<String> institutions, Set<String> buildings,
                        Set<String> religions, Set<String> tradeGoods) {

    public AssetsDTO(SaveDTO save) {
        this(true,
             true,
             save.getCountries().stream().map(CountryDTO::getTag).collect(Collectors.toSet()),
             save.getAdvisorTypes().stream().map(NamedImageLocalisedDTO::getName).collect(Collectors.toSet()),
             save.getInstitutions().stream().map(InstitutionDTO::getName).collect(Collectors.toSet()),
             save.getBuildings().stream().map(NamedImageLocalisedDTO::getName).collect(Collectors.toSet()),
             save.getReligions().stream().map(ReligionDTO::getName).collect(Collectors.toSet()),
             save.getTradeGoods().stream().map(TradeGoodDTO::getName).collect(Collectors.toSet()));
    }

    @JsonIgnore
    public boolean isEmpty() {
        return !this.provinces && !this.colors && CollectionUtils.isEmpty(this.countries) && CollectionUtils.isEmpty(this.advisors)
               && CollectionUtils.isEmpty(this.institutions) && CollectionUtils.isEmpty(this.buildings) && CollectionUtils.isEmpty(this.religions)
               && CollectionUtils.isEmpty(this.tradeGoods);
    }
}

package fr.osallek.osasaveextractor.service.object.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;

public record AssetsDTO(boolean provinces, boolean colors, Set<String> countries, Set<String> advisors, Set<String> institutions, Set<String> buildings,
                        Set<String> religions, Set<String> tradeGoods, Set<String> estates, Set<String> privileges, Set<String> ideaGroups, Set<String> modifiers) {

    @JsonIgnore
    public boolean isEmpty() {
        return !this.provinces && !this.colors && CollectionUtils.isEmpty(this.countries) && CollectionUtils.isEmpty(this.advisors)
               && CollectionUtils.isEmpty(this.institutions) && CollectionUtils.isEmpty(this.buildings) && CollectionUtils.isEmpty(this.religions)
               && CollectionUtils.isEmpty(this.tradeGoods) && CollectionUtils.isEmpty(this.estates) && CollectionUtils.isEmpty(this.privileges)
               && CollectionUtils.isEmpty(this.ideaGroups) && CollectionUtils.isEmpty(this.modifiers);
    }
}

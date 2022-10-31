package fr.osallek.osasaveextractor.service.object.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Set;

public record AssetsDTO(boolean provinces, Set<String> countries, Set<String> advisors, Set<String> institutions, Set<String> buildings,
                        Set<String> religions, Set<String> tradeGoods, Set<String> estates, Set<String> privileges, Set<String> ideaGroups,
                        Set<String> ideas, Set<String> personalities, Set<String> leaderPersonalities, Set<String> missions) {

    @JsonIgnore
    public boolean isEmpty() {
        return !this.provinces && CollectionUtils.isEmpty(this.countries) && CollectionUtils.isEmpty(this.advisors)
               && CollectionUtils.isEmpty(this.institutions) && CollectionUtils.isEmpty(this.buildings) && CollectionUtils.isEmpty(this.religions)
               && CollectionUtils.isEmpty(this.tradeGoods) && CollectionUtils.isEmpty(this.estates) && CollectionUtils.isEmpty(this.privileges)
               && CollectionUtils.isEmpty(this.ideaGroups) && CollectionUtils.isEmpty(this.ideas) && CollectionUtils.isEmpty(this.personalities)
               && CollectionUtils.isEmpty(this.leaderPersonalities) && CollectionUtils.isEmpty(this.missions);
    }
}

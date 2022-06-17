package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.clausewitzparser.common.ClausewitzUtils;
import fr.osallek.eu4parser.model.save.country.EstateInteraction;
import fr.osallek.eu4parser.model.save.country.SaveEstate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EstateDTO {

    private final String type;

    private final double loyalty;

    private final double influence;

    private final double influenceFromTerritory;

    private final double territory;

    private final List<String> grantedPrivileges;

    private final List<EstateModifierDTO> influenceModifiers;

    private final List<EstateModifierDTO> loyaltyModifiers;

    private final List<Integer> activeInfluences;

    private final List<Integer> activeLoyalties;

    public EstateDTO(SaveEstate estate) {
        this.type = ClausewitzUtils.removeQuotes(estate.getType());
        this.loyalty = estate.getLoyalty();
        this.influence = estate.getInfluence();
        this.influenceFromTerritory = estate.getInfluenceFromTerritory();
        this.territory = estate.getTerritory();
        this.grantedPrivileges = estate.getGrantedPrivileges().stream().map(i -> i.getPrivilege().getName()).toList();
        this.influenceModifiers = estate.getInfluenceModifiers().stream().map(EstateModifierDTO::new).toList();
        this.loyaltyModifiers = estate.getLoyaltyModifiers().stream().map(EstateModifierDTO::new).toList();
        this.activeInfluences = estate.getActiveInfluences();
        this.activeLoyalties = estate.getActiveLoyalties();
    }

    public String getType() {
        return type;
    }

    public double getLoyalty() {
        return loyalty;
    }

    public double getInfluence() {
        return influence;
    }

    public double getInfluenceFromTerritory() {
        return influenceFromTerritory;
    }

    public double getTerritory() {
        return territory;
    }

    public List<String> getGrantedPrivileges() {
        return grantedPrivileges;
    }

    public List<EstateModifierDTO> getInfluenceModifiers() {
        return influenceModifiers;
    }

    public List<EstateModifierDTO> getLoyaltyModifiers() {
        return loyaltyModifiers;
    }

    public List<Integer> getActiveInfluences() {
        return activeInfluences;
    }

    public List<Integer> getActiveLoyalties() {
        return activeLoyalties;
    }
}

package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.country.Losses;
import fr.osallek.eu4parser.model.save.war.WarParticipant;

import java.util.Map;

public record WarParticipantDTO(Double value, String tag, boolean promisedLand, Map<Losses, Integer> losses) {

    public WarParticipantDTO(WarParticipant warParticipant) {
        this(warParticipant.getValue(), warParticipant.getTag(), warParticipant.getPromisedLand(), warParticipant.getLosses());
    }
}

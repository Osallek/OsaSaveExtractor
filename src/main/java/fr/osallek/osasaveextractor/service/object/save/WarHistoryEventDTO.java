package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.war.WarHistoryEvent;

import java.time.LocalDate;
import java.util.List;

public record WarHistoryEventDTO(LocalDate date, List<String> addAttacker, List<String> addDefender, List<String> remAttacker, List<String> remDefender,
                                 List<BattleDTO> battles) {

    public WarHistoryEventDTO(WarHistoryEvent event) {
        this(event.getDate(), event.getAddAttacker(), event.getAddDefender(), event.getRemAttacker(), event.getRemDefender(),
             event.getBattles().stream().map(BattleDTO::new).toList());
    }
}

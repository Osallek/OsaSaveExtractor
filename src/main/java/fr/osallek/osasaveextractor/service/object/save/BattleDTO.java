package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.war.Battle;

import java.time.LocalDate;

public record BattleDTO(LocalDate date, String name, Integer location, boolean result, CombatantDTO attacker, CombatantDTO defender) {

    public BattleDTO(Battle battle) {
        this(battle.getDate(), battle.getName(), battle.getLocation(), battle.getResult(), new CombatantDTO(battle.getAttacker()),
             new CombatantDTO(battle.getDefender()));
    }
}

package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.war.Combatant;

public record CombatantDTO(Integer cavalry, Integer artillery, Integer infantry, Integer galley, Integer lightShip, Integer heavyShip, Integer transport,
                           Integer losses, String country, String commander) {

    public CombatantDTO(Combatant combatant) {
        this(combatant.getCavalry(), combatant.getArtillery(), combatant.getInfantry(), combatant.getGalley(), combatant.getLightShip(),
             combatant.getHeavyShip(), combatant.getTransport(), combatant.getLosses(), combatant.getCountry(), combatant.getCommander());
    }
}

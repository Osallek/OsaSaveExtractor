package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.common.NumbersUtils;
import fr.osallek.eu4parser.model.save.empire.CelestialEmpire;
import fr.osallek.eu4parser.model.save.empire.Empire;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class CelestialEmpireDTO extends EmpireDTO {

    private final String decree;

    public CelestialEmpireDTO(CelestialEmpire empire) {
        super(empire);
        this.decree = (empire.getDecree() == null || empire.getDecree().getDecree() == null) ? null : empire.getDecree().getDecree().getName();
    }

    public String getDecree() {
        return decree;
    }
}

package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.common.NumbersUtils;
import fr.osallek.eu4parser.model.save.Save;
import fr.osallek.eu4parser.model.save.SaveGreatProject;

public class GreatProjectDTO extends Localised {

    private final String name;

    private final int level;

    private final int maxLevel;

    public GreatProjectDTO(Save save, SaveGreatProject greatProject) {
        super(save.getGame().getLocalisation(greatProject.getName()));
        this.name = greatProject.getName();
        this.level = NumbersUtils.intOrDefault(greatProject.getDevelopmentTier());
        this.maxLevel = greatProject.getGreatProject().getMaxLevel();
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}

package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.country.CustomNationalIdea;

public class CustomNationalIdeaDTO {

    private final int level;

    private final int index;

    private final String name;

    public CustomNationalIdeaDTO(CustomNationalIdea idea) {
        this.level = idea.getLevel();
        this.index = idea.getIndex();
        this.name = idea.getName();
    }

    public int getLevel() {
        return level;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }
}

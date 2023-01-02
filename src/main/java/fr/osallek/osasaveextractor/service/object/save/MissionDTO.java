package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.Mission;
import fr.osallek.eu4parser.model.game.localisation.Localisation;
import fr.osallek.eu4parser.model.save.country.SaveCountry;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

public class MissionDTO extends NamedImageLocalisedDTO {

    private List<String> required;

    private boolean completed;

    private Localised description;

    public MissionDTO(SaveCountry country, Mission mission, boolean completed) {
        super(country.getSave(), country, mission.getName() + "_title", mission.getIconFile(), mission.getName());
        this.required = CollectionUtils.isEmpty(mission.getRequiredMissions()) ? null : mission.getRequiredMissions().stream().map(Mission::getName).toList();
        this.completed = completed;
        this.description = new Localised(country.getSave(), country, mission.getName() + "_desc");
    }

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Localised getDescription() {
        return description;
    }

    public void setDescription(Localised description) {
        this.description = description;
    }
}

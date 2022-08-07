package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.Mission;
import fr.osallek.eu4parser.model.save.Save;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class MissionDTO extends NamedImageLocalisedDTO {

    private List<String> required;

    public MissionDTO(Save save, Mission mission) {
        super(save.getGame().getLocalisation(mission.getName() + "_title"), mission.getIconFile(), mission.getName());
        this.required = CollectionUtils.isEmpty(mission.getRequiredMissions()) ? null : mission.getRequiredMissions().stream().map(Mission::getName).toList();
    }

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }
}

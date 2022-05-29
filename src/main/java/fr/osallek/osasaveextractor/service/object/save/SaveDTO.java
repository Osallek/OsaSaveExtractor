package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.Save;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;

public class SaveDTO {

    private List<TeamDTO> teams;

    public SaveDTO(Save save) {
        this.teams = CollectionUtils.isNotEmpty(save.getTeams()) ? save.getTeams().stream().map(TeamDTO::new).toList() : null;
    }

    public List<TeamDTO> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamDTO> teams) {
        this.teams = teams;
    }
}

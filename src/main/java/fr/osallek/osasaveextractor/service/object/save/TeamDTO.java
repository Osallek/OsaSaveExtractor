package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.SaveTeam;
import java.util.List;

public class TeamDTO {

    private final String name;

    private final List<String> members;

    public TeamDTO(SaveTeam saveTeam) {
        this.name = saveTeam.name();
        this.members = saveTeam.countries();
    }

    public String getName() {
        return name;
    }

    public List<String> getMembers() {
        return members;
    }
}

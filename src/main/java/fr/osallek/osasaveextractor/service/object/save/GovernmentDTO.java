package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.todo.GovernmentReform;
import fr.osallek.eu4parser.model.save.country.SaveGovernment;

import java.util.List;

public class GovernmentDTO {

    private final String type;

    private final List<String> reforms;

    public GovernmentDTO(SaveGovernment government) {
        this.type = government.getType().getName();
        this.reforms = government.getReforms().stream().map(GovernmentReform::getName).toList();
    }

    public String getType() {
        return type;
    }

    public List<String> getReforms() {
        return reforms;
    }
}

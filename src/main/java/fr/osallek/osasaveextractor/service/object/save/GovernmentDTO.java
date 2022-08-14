package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.save.country.SaveGovernment;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class GovernmentDTO {

    private final String type;

    private final List<String> reforms;

    public GovernmentDTO(SaveGovernment government) {
        this.type = government.getTypeName();
        this.reforms = government.getReformsNames().stream().filter(StringUtils::isNotBlank).toList();
    }

    public String getType() {
        return type;
    }

    public List<String> getReforms() {
        return reforms;
    }
}

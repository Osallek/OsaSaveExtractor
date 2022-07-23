package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.IdeaGroup;
import fr.osallek.eu4parser.model.save.Save;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.collections4.MapUtils;

public class IdeaGroupDTO extends NamedImageLocalisedDTO {

    private List<NamedImageLocalisedDTO> ideas;

    public IdeaGroupDTO(Save save, IdeaGroup ideaGroup) {
        super(save.getGame().getLocalisation(ideaGroup.getName()), ideaGroup.getImage(), ideaGroup.getName());
        this.ideas = ideaGroup.getIdeas().entrySet().stream().map(entry -> {
            AtomicReference<File> image = new AtomicReference<>(null);

            if (MapUtils.isNotEmpty(entry.getValue().getModifiers())) {
                entry.getValue()
                     .getModifiers()
                     .keySet()
                     .stream()
                     .map(m -> m.getImage(save.getGame()))
                     .filter(Objects::nonNull)
                     .findFirst()
                     .ifPresent(image::set);
            }

            return new NamedImageLocalisedDTO(save.getGame().getLocalisation(entry.getKey()), image.get(), entry.getKey());
        }).toList();
    }

    public List<NamedImageLocalisedDTO> getIdeas() {
        return ideas;
    }

    public void setIdeas(List<NamedImageLocalisedDTO> ideas) {
        this.ideas = ideas;
    }
}

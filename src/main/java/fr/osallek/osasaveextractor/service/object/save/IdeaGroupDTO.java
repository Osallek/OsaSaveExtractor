package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.IdeaGroup;
import fr.osallek.eu4parser.model.save.Save;
import java.util.List;

public class IdeaGroupDTO extends NamedImageLocalisedDTO {

    private List<NamedImageLocalisedDTO> ideas;

    public IdeaGroupDTO(Save save, IdeaGroup ideaGroup) {
        super(save.getGame().getLocalisation(ideaGroup.getName()), ideaGroup.getImage(), ideaGroup.getName());
        this.ideas = ideaGroup.getIdeas()
                              .entrySet()
                              .stream()
                              .map(entry -> new NamedImageLocalisedDTO(save.getGame().getLocalisation(entry.getKey()),
                                                                       entry.getValue().getImage(save.getGame()), entry.getKey()))
                              .toList();
    }

    public List<NamedImageLocalisedDTO> getIdeas() {
        return ideas;
    }

    public void setIdeas(List<NamedImageLocalisedDTO> ideas) {
        this.ideas = ideas;
    }
}

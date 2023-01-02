package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.localisation.Eu4Language;
import fr.osallek.eu4parser.model.game.localisation.Localisation;
import fr.osallek.eu4parser.model.save.Save;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public class NamedImageLocalisedDTO extends ImageLocalised {

    protected String name;

    public NamedImageLocalisedDTO(String name) {
        this.name = name;
    }

    public NamedImageLocalisedDTO(Map<Eu4Language, Localisation> localisations, String name) {
        super(localisations);
        this.name = name;
    }

    public NamedImageLocalisedDTO(Map<Eu4Language, Localisation> localisations, File image) {
        super(localisations, image);
    }

    public NamedImageLocalisedDTO(Map<Eu4Language, Localisation> localisations, Path image) {
        super(localisations, image);
    }

    public NamedImageLocalisedDTO(File image) {
        super(image);
    }

    public NamedImageLocalisedDTO(Path image) {
        super(image);
    }

    public NamedImageLocalisedDTO(String image, String name) {
        super(image);
        this.name = name;
    }

    public NamedImageLocalisedDTO(Map<Eu4Language, Localisation> localisations, String image, String name) {
        super(localisations, image);
        this.name = name;
    }

    public NamedImageLocalisedDTO(Map<Eu4Language, Localisation> localisations, File image, String name) {
        super(localisations, image);
        this.name = name;
    }

    public NamedImageLocalisedDTO(Save save, Object root, String key, File image, String name) {
        super(save, root, key, image);
        this.name = name;
    }

    public NamedImageLocalisedDTO(Map<Eu4Language, Localisation> localisations, Path image, String name) {
        super(localisations, image);
        this.name = name;
    }

    public NamedImageLocalisedDTO(File image, String name) {
        super(image);
        this.name = name;
    }

    public NamedImageLocalisedDTO(Path image, String name) {
        super(image);
        this.name = name;
    }

    public NamedImageLocalisedDTO() {
    }

    public NamedImageLocalisedDTO(Map<Eu4Language, Localisation> localisations) {
        super(localisations);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package fr.osallek.osasaveextractor.service.object.save;

import fr.osallek.eu4parser.model.game.localisation.Eu4Language;
import fr.osallek.eu4parser.model.game.localisation.Localisation;
import fr.osallek.eu4parser.model.save.Save;
import fr.osallek.osasaveextractor.common.Constants;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public class ImageLocalised extends Localised {

    protected String image;

    public ImageLocalised() {
    }

    public ImageLocalised(Map<Eu4Language, Localisation> localisations) {
        super(localisations);
    }

    public ImageLocalised(String image) {
        this.image = image;
    }

    public ImageLocalised(Map<Eu4Language, Localisation> localisations, String image) {
        super(localisations);
        this.image = image;
    }

    public ImageLocalised(Map<Eu4Language, Localisation> localisations, File image) {
        super(localisations);
        Constants.getFileChecksum(image).ifPresent(this::setImage);
    }

    public ImageLocalised(Save save, Object root, String key, File image) {
        super(save, root, key);
        Constants.getFileChecksum(image).ifPresent(this::setImage);
    }

    public ImageLocalised(Map<Eu4Language, Localisation> localisations, Path image) {
        super(localisations);
        Constants.getFileChecksum(image).ifPresent(this::setImage);
    }

    public ImageLocalised(File image) {
        Constants.getFileChecksum(image).ifPresent(this::setImage);
    }

    public ImageLocalised(Path image) {
        Constants.getFileChecksum(image).ifPresent(this::setImage);
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

package fr.osallek.osasaveextractor.controller.object;

import fr.osallek.osasaveextractor.service.Eu4Service;
import fr.osallek.osasaveextractor.service.object.ServerSave;
import javafx.scene.control.ListCell;
import org.springframework.context.MessageSource;

import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class ServerSaveListCell extends ListCell<ServerSave> {

    private final MessageSource messageSource;

    private final Eu4Service eu4Service;

    public ServerSaveListCell(MessageSource messageSource, Eu4Service eu4Service) {
        this.messageSource = messageSource;
        this.eu4Service = eu4Service;
    }

    @Override
    protected void updateItem(ServerSave save, boolean empty) {
        super.updateItem(save, empty);

        Locale locale = Locale.getDefault();

        if (save == null || empty) {
            setGraphic(null);
        } else {
            setText(save.name()
                    + " ["
                    + save.date().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale).withChronology(Chronology.ofLocale(locale)))
                    + "] ("
                    + this.messageSource.getMessage("ose.creation-date", null, locale)
                    + " "
                    + save.creationDate().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale).withChronology(Chronology.ofLocale(locale)))
                    + ')');
        }
    }

}

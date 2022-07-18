package fr.osallek.osasaveextractor.service.object.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import org.springframework.context.MessageSource;

public record ServerSave(@JsonProperty("name") String name, @JsonProperty("creation_date") LocalDateTime creationDate, @JsonProperty("date") LocalDate date,
                         @JsonProperty("id") String id) {

    @JsonIgnore
    public String toString(MessageSource messageSource) {
        Locale locale = Locale.getDefault();
        return name()
               + " ["
               + date().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale).withChronology(Chronology.ofLocale(locale)))
               + "] ("
               + messageSource.getMessage("ose.creation-date", null, locale)
               + " "
               + creationDate().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale).withChronology(Chronology.ofLocale(locale)))
               + ')';
    }
}

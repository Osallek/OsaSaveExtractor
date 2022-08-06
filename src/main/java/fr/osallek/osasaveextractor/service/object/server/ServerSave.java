package fr.osallek.osasaveextractor.service.object.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.osallek.osasaveextractor.common.Constants;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ServerSave(@JsonProperty("name") String name, @JsonProperty("creationDate") LocalDateTime creationDate, @JsonProperty("date") LocalDate date,
                         @JsonProperty("id") String id) {

    @JsonIgnore
    public String toString(MessageSource messageSource) {
        return this.name
               + " ["
               + this.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                                                   .withLocale(Constants.LOCALE)
                                                   .withChronology(Chronology.ofLocale(Constants.LOCALE)))
               + "] ("
               + messageSource.getMessage("ose.creation-date", null, Constants.LOCALE)
               + " "
               + this.creationDate.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                                                           .withLocale(Constants.LOCALE)
                                                           .withChronology(Chronology.ofLocale(Constants.LOCALE)))
               + ") ("
               + this.id
               + ')';
    }
}

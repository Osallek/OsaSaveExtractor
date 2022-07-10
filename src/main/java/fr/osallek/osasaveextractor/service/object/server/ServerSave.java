package fr.osallek.osasaveextractor.service.object.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ServerSave(@JsonProperty("name") String name, @JsonProperty("creation_date") LocalDateTime creationDate, @JsonProperty("date") LocalDate date,
                         @JsonProperty("id") String id) {
}

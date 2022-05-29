package fr.osallek.osasaveextractor.controller.object;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ParseDTO(@JsonProperty("save") String save, @JsonProperty("previous_save") String previousSave) {
}

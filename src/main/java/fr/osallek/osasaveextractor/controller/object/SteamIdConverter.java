package fr.osallek.osasaveextractor.controller.object;

import javafx.util.StringConverter;

import java.util.Map;

public class SteamIdConverter extends StringConverter<Map.Entry<String, String>> {

    @Override
    public String toString(Map.Entry<String, String> object) {
        return object.getValue();
    }

    @Override
    public Map.Entry<String, String> fromString(String string) {
        return null;
    }
}

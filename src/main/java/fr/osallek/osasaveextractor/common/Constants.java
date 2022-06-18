package fr.osallek.osasaveextractor.common;

import fr.osallek.osasaveextractor.service.object.save.ColorDTO;

public final class Constants {

    private Constants() {
    }

    public static ColorDTO stringToColor(String s) {
        return new ColorDTO(new java.awt.Color(s.toUpperCase().hashCode() % 0xFFFFFF));
    }
}

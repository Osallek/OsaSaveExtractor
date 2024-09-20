package fr.osallek.osasaveextractor.common;

import fr.osallek.osasaveextractor.service.object.save.ColorDTO;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public final class Constants {

    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.class);

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    public static final Pattern UUID_PATTERN = Pattern.compile("^[\\da-f]{8}-[\\da-f]{4}-[0-5][\\da-f]{3}-[089ab][\\da-f]{3}-[\\da-f]{12}$",
                                                               Pattern.CASE_INSENSITIVE);

    private Constants() {
    }

    public static final Locale LOCALE = Locale.getDefault();

    public static ColorDTO stringToColor(String s) {
        return new ColorDTO(new java.awt.Color(s.toUpperCase().hashCode() % 0xFFFFFF));
    }

    public static Optional<String> getFileChecksum(File file) {
        if (file == null) {
            return Optional.empty();
        } else {
            return getFileChecksum(file.toPath());
        }
    }

    public static Optional<String> getFileChecksum(Path path) {
        if (path == null) {
            return Optional.empty();
        }

        if (!path.toFile().exists() || !path.toFile().canRead()) {
            LOGGER.warn("Could not get hash of {}: File does not exists or is not readable", path.getFileName());
            return Optional.empty();
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path.toFile()))) {
                byte[] buffer = new byte[8192];
                int count;

                while ((count = bis.read(buffer)) > 0) {
                    md.update(buffer, 0, count);
                }
            }

            return Optional.of(bytesToHex(md.digest()));
        } catch (Exception e) {
            LOGGER.error("Could not get hash of {}: {}", path.getFileName(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Thanks to <a href="https://stackoverflow.com/a/9855338">...</a>
     */
    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    public static void openLink(String url) {
        Runtime runtime = Runtime.getRuntime();

        try {
            if (SystemUtils.IS_OS_WINDOWS) {
                runtime.exec(new String[] {"explorer", url});
            } else if (SystemUtils.IS_OS_MAC) {
                runtime.exec(new String[] {"open", url});
            } else if (SystemUtils.IS_OS_LINUX) {
                runtime.exec(new String[] {"xdg-open", url});
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while trying to open browser: {}", e.getMessage(), e);
        }
    }
}

package fr.osallek.osasaveextractor.common;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

public class CustomGZIPOutputStream extends GZIPOutputStream {

    public CustomGZIPOutputStream(OutputStream out) throws IOException {
        super(out);
        this.def.setLevel(Deflater.BEST_COMPRESSION);
    }
}

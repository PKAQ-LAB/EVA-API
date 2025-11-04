package org.pkaq.core.util;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author PKAQ
 */
public class IOUtils {
    public static final int DEFAULT_BUFFER_SIZE = 2 << 12;

    public static long copy(InputStream in, OutputStream out, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        long total = 0;
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
            total += read;
        }
        return total;
    }
}

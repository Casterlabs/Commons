package co.casterlabs.commons.platform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import lombok.NonNull;

class _PlatformUtil {

    static void writeInputStreamToOutputStream(@NonNull InputStream source, @NonNull OutputStream dest) throws IOException {
        byte[] buffer = new byte[1024];
        int read = 0;

        while ((read = source.read(buffer)) != -1) {
            dest.write(buffer, 0, read);
        }

        dest.flush();

        source.close();
        dest.close();
    }

    static byte[] readInputStreamBytes(@NonNull InputStream source) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writeInputStreamToOutputStream(source, out);

        source.close();

        return out.toByteArray();
    }

    static String readInputStreamString(@NonNull InputStream source, @NonNull Charset sourceCharset) throws IOException {
        byte[] bytes = readInputStreamBytes(source);

        return new String(bytes, sourceCharset);
    }

}

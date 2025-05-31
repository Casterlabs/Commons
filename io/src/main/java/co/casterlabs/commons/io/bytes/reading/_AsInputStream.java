package co.casterlabs.commons.io.bytes.reading;

import java.io.IOException;
import java.io.InputStream;

import co.casterlabs.commons.io.bytes.EndOfStreamException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class _AsInputStream extends InputStream {
    private final ByteReader reader;

    @Override
    public int read() throws IOException {
        try {
            return this.reader.read();
        } catch (EndOfStreamException e) {
            return -1;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            this.reader.read(b, off, len);
            return len;
        } catch (EndOfStreamException e) {
            return -1;
        }
    }

}

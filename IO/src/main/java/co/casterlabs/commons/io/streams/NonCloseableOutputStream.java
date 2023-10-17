package co.casterlabs.commons.io.streams;

import java.io.IOException;
import java.io.OutputStream;

import lombok.RequiredArgsConstructor;

/**
 * This wrapper prevents the underlying OutputStream from being closed.
 */
@RequiredArgsConstructor
public class NonCloseableOutputStream extends OutputStream {
    private final OutputStream out;
    private boolean isClosed;

    @Override
    public void close() throws IOException {
        // Don't actually close the outputstream.
        this.isClosed = true;
    }

    @Override
    public void write(int b) throws IOException {
        if (this.isClosed) throw new IOException("NCOutputStream is closed.");
        this.out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (this.isClosed) throw new IOException("NCOutputStream is closed.");
        if (len == 0) return;
        this.out.write(b, off, len);
    }

}

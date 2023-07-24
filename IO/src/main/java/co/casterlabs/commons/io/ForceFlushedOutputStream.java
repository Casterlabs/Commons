package co.casterlabs.commons.io;

import java.io.IOException;
import java.io.OutputStream;

import lombok.RequiredArgsConstructor;

/**
 * An OutputStream wrapper that forcibly flushes after every write().
 */
@RequiredArgsConstructor
public class ForceFlushedOutputStream extends OutputStream {
    private final OutputStream wrap;

    /**
     * See: {@link OutputStream#write(int)}
     */
    @Override
    public void write(int b) throws IOException {
        this.wrap.write(b);
        this.wrap.flush();
    }

    /**
     * See: {@link OutputStream#write(byte[])}
     */
    @Override
    public void write(byte[] b) throws IOException {
        this.wrap.write(b);
        this.wrap.flush();
    }

    /**
     * See: {@link OutputStream#write(byte[], int, int)}
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.wrap.write(b, off, len);
        this.wrap.flush();
    }

    /**
     * See: {@link OutputStream#flush()}
     */
    @Override
    public void flush() throws IOException {
        this.wrap.flush();
    }

    /**
     * See: {@link OutputStream#close()}
     */
    @Override
    public void close() throws IOException {
        this.wrap.close();
    }

}

package co.casterlabs.commons.io.bytes.writing;

import java.io.IOException;
import java.io.OutputStream;

import lombok.AllArgsConstructor;

@AllArgsConstructor
class _AsOutputStream extends OutputStream {
    private final ByteWriter delegate;

    @Override
    public void write(int b) throws IOException {
        this.delegate.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.delegate.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.delegate.write(b, off, len);
    }

}

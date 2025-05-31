package co.casterlabs.commons.io.bytes.writing;

import java.io.IOException;
import java.io.OutputStream;

import lombok.AllArgsConstructor;

@AllArgsConstructor
class _AsOutputStream extends OutputStream {
    private final ByteWriter writer;

    @Override
    public void write(int b) throws IOException {
        this.writer.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.writer.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.writer.write(b, off, len);
    }

}

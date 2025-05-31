/* 
Copyright 2025 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.io.bytes.reading;

import java.io.IOException;

import co.casterlabs.commons.io.bytes.EndOfStreamException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LimitedByteReader extends ByteReader {
    private final ByteReader reader;
    private int limit;

    private void ensureReadable(int len) throws IOException {
        if (this.limit < len) {
            throw new EndOfStreamException("End of stream");
        }
    }

    @Override
    public void skip(int len) throws IOException {
        ensureReadable(len);

        if (len < 1) {
            return;
        }

        this.limit -= len;
        this.reader.skip(len);
    }

    @Override
    public byte[] read(int len) throws IOException {
        ensureReadable(len);

        if (len < 1) {
            return new byte[0];
        }

        byte[] read = this.reader.read(len);
        this.limit -= len;
        return read;
    }

    @Override
    public void read(byte[] b, int off, int len) throws IOException {
        ensureReadable(len);

        this.reader.read(b, off, len);
        this.limit -= len;
    }

    @Override
    protected int read() throws IOException {
        ensureReadable(1);
        this.limit -= 1;
        return this.reader.read();
    }

    /**
     * Consumes all remaining bytes from the stream.
     */
    @Override
    public void close() throws Exception {
        this.skip(this.limit);
    }

}

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
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ArrayByteReader extends ByteReader {
    private final byte[] bytes;
    private int index = 0;

    private void ensureReadable(int len) throws IOException {
        if (this.index + len > this.bytes.length) {
            throw new EndOfStreamException("End of stream");
        }
    }

    @Override
    public void skip(int len) throws IOException {
        ensureReadable(len);
        this.index += len;
    }

    @Override
    public byte[] read(int len) throws IOException {
        ensureReadable(len);

        byte[] buf = new byte[len];
        System.arraycopy(this.bytes, this.index, buf, 0, len);
        this.index += len;
        return buf;
    }

    @Override
    public void read(byte[] b, int off, int len) throws IOException {
        ensureReadable(len);

        System.arraycopy(this.bytes, this.index, b, off, len);
        this.index += len;
    }

    @Override
    protected int read() throws IOException {
        ensureReadable(1);
        return this.bytes[this.index++];
    }

    @Override
    public void close() throws Exception {
        this.index = this.bytes.length;
    }

}

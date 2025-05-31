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
import java.io.InputStream;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StreamByteReader extends ByteReader {
    private final InputStream stream;

    @Override
    public void skip(int len) throws IOException {
        if (len < 1) {
            return;
        }

        long skipped = 0;

        while (skipped < len) {
            long round = this.stream.skip(len);
            if (round == -1) throw new IOException("End of stream");
            skipped += round;
        }
    }

    @Override
    public byte[] read(int len) throws IOException {
        if (len < 1) {
            return new byte[0];
        }

        byte[] buf = new byte[len];
        int total = 0;
        while (total < len) {
            int read = this.stream.read(buf, total, len - total);
            if (read == -1) throw new IOException("End of stream");
            total += read;
        }
        return buf;
    }

    @Override
    public void read(byte[] b, int off, int len) throws IOException {
        int remaining = len;
        while (remaining > 0) {
            int read = this.stream.read(b, off + len - remaining, remaining);
            if (read == -1) throw new IOException("End of stream");
            remaining -= read;
        }
    }

    @Override
    protected int read() throws IOException {
        int read = this.stream.read();
        if (read == -1) throw new IOException("End of stream");
        return read;
    }

    @Override
    public void close() throws Exception {
        this.stream.close();
    }

}

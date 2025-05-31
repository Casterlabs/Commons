/* 
Copyright 2025 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.io.streams;

import java.io.IOException;
import java.io.InputStream;

import lombok.AllArgsConstructor;

/**
 * This wrapper allows you to place a read limit on an input stream to prevent
 * you from unintentionally reading too many bytes.
 * 
 * @apiNote This class is not thread-safe.
 */
@AllArgsConstructor
public class LimitedInputStream extends InputStream {
    private final InputStream in;
    private long remaining;

    /**
     * Consumes all remaining bytes from the stream.
     */
    @Override
    public void close() throws IOException {
        this.skip(this.remaining);
    }

    @Override
    public int read() throws IOException {
        if (this.remaining == 0) {
            return -1;
        }
        this.remaining--;
        return this.in.read();
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        if (this.remaining == 0) {
            return -1;
        }

        if (len < 1) {
            return 0;
        }

        if (len > this.remaining) { // Clamp.
            len = (int) this.remaining;
        }

        int read = this.in.read(b, off, len);
        this.remaining -= read;
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        if (this.remaining == 0) {
            return -1;
        }

        if (n < 1) {
            return 0;
        }

        if (n > this.remaining) { // Clamp.
            n = this.remaining;
        }

        long skipped = this.in.skip(n);
        this.remaining -= skipped;
        return skipped;
    }

    @Override
    public int available() throws IOException {
        if (this.remaining == 0) {
            return -1;
        }

        int available = this.in.available();
        if (available > this.remaining) {
            return (int) this.remaining;
        }

        return available;
    }

}

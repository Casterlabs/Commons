/* 
Copyright 2023 Casterlabs

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
 */
@AllArgsConstructor
public class LimitedInputStream extends InputStream {
    private final InputStream in;
    private long limit;

    @Override
    public void close() throws IOException {} // NOOP

    @Override
    public synchronized int read() throws IOException {
        if (this.limit == 0) {
            return -1;
        }
        this.limit--;
        return this.in.read();
    }

    @Override
    public synchronized int read(byte b[], int off, int len) throws IOException {
        if (this.limit == 0) {
            return -1;
        }

        if (len > this.limit) { // Clamp.
            len = (int) this.limit;
        }

        int read = this.in.read(b, off, len);
        this.limit -= read;
        return read;
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        if (this.limit == 0) {
            return -1;
        }

        if (n > this.limit) { // Clamp.
            n = this.limit;
        }

        long skipped = this.in.skip(n);
        this.limit -= skipped;
        return skipped;
    }

    @Override
    public synchronized int available() throws IOException {
        if (this.limit == 0) {
            return -1;
        }

        int available = this.in.available();
        if (available > this.limit) {
            return (int) this.limit;
        }

        return available;
    }

}

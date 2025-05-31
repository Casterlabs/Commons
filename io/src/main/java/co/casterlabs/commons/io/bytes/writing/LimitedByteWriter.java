/* 
Copyright 2025 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.io.bytes.writing;

import java.io.IOException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LimitedByteWriter extends ByteWriter {
    private final ByteWriter destination;
    private int remaining = 0;

    private void ensureCapacity(int len) throws IOException {
        if (this.remaining < len) {
            throw new IOException("Capacity reached.");
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        ensureCapacity(len);

        this.destination.write(b, off, len);
        this.remaining -= len;
    }

    @Override
    protected void write(int value) throws IOException {
        ensureCapacity(1);
        this.destination.write(value);
        this.remaining--;
    }

    /**
     * Writes 0s in place of the remaining bytes.
     */
    @Override
    public void close() throws Exception {
        if (this.remaining > 0) {
            this.destination.write(new byte[this.remaining], 0, this.remaining);
            this.remaining = 0;
        }
    }

}

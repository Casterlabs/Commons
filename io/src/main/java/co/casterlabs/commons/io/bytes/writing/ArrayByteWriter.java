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

public class ArrayByteWriter extends ByteWriter {
    private byte[] buffer;
    private int index = 0;

    public ArrayByteWriter() {
        this(1024);
    }

    public ArrayByteWriter(int initialCapacity) {
        this.buffer = new byte[initialCapacity];
    }

    public byte[] buffer() {
        if (this.index == this.buffer.length) return this.buffer; // No need to copy.

        byte[] slice = new byte[this.index];
        System.arraycopy(this.buffer, 0, slice, 0, this.index);
        return slice;
    }

    private void ensureCapacity(int len) {
        if (this.index + len <= this.buffer.length) {
            return;
        }

        int newCapacity = Math.max(this.buffer.length * 2, this.index + len);
        byte[] newBuffer = new byte[newCapacity];
        System.arraycopy(this.buffer, 0, newBuffer, 0, this.index);
        this.buffer = newBuffer;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.ensureCapacity(len);
        System.arraycopy(b, off, this.buffer, this.index, len);
        this.index += len;
    }

    @Override
    public void write(int value) throws IOException {
        this.ensureCapacity(1);
        this.buffer[this.index++] = (byte) value;
    }

    @Override
    public void close() throws Exception {
        // NOOP
    }

}

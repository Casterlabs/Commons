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
import java.io.OutputStream;

import lombok.RequiredArgsConstructor;

/**
 * This wrapper prevents the underlying OutputStream from being closed.
 */
@RequiredArgsConstructor
public class NonCloseableOutputStream extends OutputStream {
    private final OutputStream out;
    private boolean isClosed;

    @Override
    public void close() throws IOException {
        // Don't actually close the outputstream.
        this.isClosed = true;
    }

    @Override
    public void write(int b) throws IOException {
        if (this.isClosed) throw new IOException("NCOutputStream is closed.");
        this.out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (this.isClosed) throw new IOException("NCOutputStream is closed.");
        if (len == 0) return;
        this.out.write(b, off, len);
    }

}

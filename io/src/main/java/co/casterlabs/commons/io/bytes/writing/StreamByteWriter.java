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
import java.io.OutputStream;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StreamByteWriter extends ByteWriter {
    private final OutputStream stream;

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.stream.write(b, off, len);
    }

    @Override
    public void write(int value) throws IOException {
        this.stream.write(value);
    }

    @Override
    public void close() throws Exception {
        this.stream.close();
    }

}

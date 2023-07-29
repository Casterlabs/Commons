/* 
Copyright 2023 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.io;

import java.io.IOException;
import java.io.InputStream;

import co.casterlabs.commons.io.sink.ExtractionStrategy;
import co.casterlabs.commons.io.sink.InsertionStrategy;
import co.casterlabs.commons.io.sink.SinkBuffer;
import co.casterlabs.commons.io.sink.SinkUtil;
import lombok.NonNull;

/**
 * This InputStream continually reads from the provided stream until the
 * internal buffer is full, allowing you to take large chunks of data without
 * needing to potentially wait on a blocking read.
 */
public class BufferedAvailabilityInputStream extends InputStream {
    private SinkBuffer buffer;

    public BufferedAvailabilityInputStream(@NonNull InputStream in, int maxBuffer) {
        this.buffer = new SinkBuffer(
            maxBuffer,
            InsertionStrategy.BLOCK_ON_OVERRUN,
            ExtractionStrategy.SHRINK_ON_UNDERRUN
        );

        SinkUtil.drainInputStreamToSink(in, this.buffer);
    }

    /**
     * See: {@link InputStream#read()}
     */
    @Override
    public synchronized int read() throws IOException {
        byte[] tmp = new byte[1];
        while (this.read(tmp) == 0); // Keep reading...
        return tmp[0];
    }

    /**
     * See: {@link InputStream#read(byte[], int, int)}
     */
    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        try {
            return this.buffer.extract(b, off, len);
        } catch (InterruptedException e) {
            throw new IOException(e); // Rethrow.
        }
    }

    /**
     * See: {@link InputStream#available()}
     */
    @Override
    public synchronized int available() throws IOException {
        return this.buffer.getAmountBuffered();
    }

    /**
     * Closes the BufferedAvailabilityInputStream, discarding any buffered bytes and
     * also closes the underlying wrapped InputStream.
     * 
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        this.buffer.close();
    }

}

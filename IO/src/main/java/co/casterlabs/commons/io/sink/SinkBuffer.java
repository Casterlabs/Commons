/* 
Copyright 2023 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.io.sink;

import lombok.Getter;
import lombok.NonNull;

/**
 * A SinkBuffer allows you to insert data at a reliable rate and read back the
 * data at a reliable rate. You can mix and match the strategies and set the
 * internal buffer size to any value. <br />
 * <br />
 * 
 * There are three insertion strategies:
 * <ul>
 * <li>{@link InsertionStrategy#DROP_ON_OVERFLOW}</li>
 * <li>{@link InsertionStrategy#BLOCK_ON_OVERFLOW}</li>
 * <li>{@link InsertionStrategy#THROW_ON_OVERFLOW}</li>
 * </ul>
 * 
 * There are four extraction strategies:
 * <ul>
 * <li>{@link ExtractionStrategy#NULL_ON_UNDERRUN}</li>
 * <li>{@link ExtractionStrategy#BLOCK_ON_UNDERRUN}</li>
 * <li>{@link ExtractionStrategy#THROW_ON_UNDERRUN}</li>
 * </ul>
 */
public class SinkBuffer {

    /**
     * If for some reason you need NULL_ON_UNDERRUN to use a different value than 0,
     * change this. You probably don't need this.
     */
    @Deprecated
    public byte nullValue = 0;

    private final InsertionStrategy insertionStrategy;
    private final ExtractionStrategy extractionStrategy;
    private final byte[] buffer; // lock.

    private @Getter int amountBuffered = 0;
    private int bufferReadPos = 0;
    private int bufferWritePos = 0;

    public SinkBuffer(int bufferSize, @NonNull InsertionStrategy insertionStrategy, ExtractionStrategy extractionStrategy) {
        this.buffer = new byte[bufferSize];
        this.insertionStrategy = insertionStrategy;
        this.extractionStrategy = extractionStrategy;
    }

    public int getBufferSize() {
        return this.buffer.length;
    }

    public void insert(byte[] buf, int bufOffset, int amountToInsert) throws InterruptedException {
        synchronized (this.buffer) {
            // TODO
        }
    }

    public void extract(byte[] buf, int bufOffset, int amountToExtract) throws InterruptedException {
        synchronized (this.buffer) {
            if (this.amountBuffered < amountToExtract) { // The while loop is for
                switch (this.extractionStrategy) {
                    case BLOCK_ON_UNDERRUN: {
                        do {
                            this.buffer.wait(); // Wait for new data to come in.
                        } while (this.amountBuffered < amountToExtract); // Check and see if there's enough.
                        break; // Fall through to the below code.
                    }

                    case THROW_ON_UNDERRUN:
                        throw new BufferUnderrunError();

                    case NULL_ON_UNDERRUN: {
                        int amountAvailable = this.amountBuffered;
                        this.extract(buf, bufOffset, amountAvailable);

                        // Go over the remaining bytes and set them to 0.
                        for (int arrIdx = bufOffset + amountAvailable; arrIdx < amountToExtract; arrIdx++) {
                            buf[arrIdx] = this.nullValue;
                        }
                        return;
                    }

                    case LOOP_ON_UNDERRUN: {
                        int remaining = amountToExtract;
                        while (remaining > 0) {
                            int len = Math.min(this.amountBuffered, remaining);
                            remaining -= len;
                            bufOffset += len;
                            this.extract(buf, bufOffset, len);
                        }
                        return; // We're done!
                    }

                }
            }

            // TODO
        }
    }

}

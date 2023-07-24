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
 * A SinkBuffer allows you to insert data at a predictable rate and read back
 * the data at a predictable rate. You can mix and match strategies and set the
 * internal buffer size to any value. <br />
 * <br />
 * 
 * There are three insertion strategies:
 * <ul>
 * <li>{@link InsertionStrategy#DROP_ON_OVERRUN}</li>
 * <li>{@link InsertionStrategy#BLOCK_ON_OVERRUN}</li>
 * <li>{@link InsertionStrategy#THROW_ON_OVERRUN}</li>
 * </ul>
 * 
 * There are four extraction strategies:
 * <ul>
 * <li>{@link ExtractionStrategy#NULL_ON_UNDERRUN}</li>
 * <li>{@link ExtractionStrategy#BLOCK_ON_UNDERRUN}</li>
 * <li>{@link ExtractionStrategy#THROW_ON_UNDERRUN}</li>
 * <li>{@link ExtractionStrategy#LOOP_ON_UNDERRUN}</li>
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
    private final byte[] buffer;

    private @Getter int amountBuffered = 0;
    private int bufferReadPos = 0;
    private int bufferWritePos = this.bufferReadPos; // MUST START AT THE SAME SPOT!

    /**
     * @param    bufferSize               the size of the internal buffer. It is up
     *                                    to you to properly estimate this value
     *                                    ahead of time.
     * @param    insertionStrategy
     * @param    extractionStrategy
     * 
     * @throws   IllegalArgumentException if bufferSize is not greater than zero
     * 
     * @implNote                          Using both BLOCK_ON_OVERRUN and
     *                                    BLOCK_ON_UNDERRUN <i>may</i> result in
     *                                    deadlocks. You have been warned.
     */
    public SinkBuffer(
        int bufferSize, @NonNull InsertionStrategy insertionStrategy,
        @NonNull ExtractionStrategy extractionStrategy
    ) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size MUST be greater than zero");
        }

        this.buffer = new byte[bufferSize];
        this.insertionStrategy = insertionStrategy;
        this.extractionStrategy = extractionStrategy;
    }

    public int getBufferSize() {
        return this.buffer.length;
    }

    /**
     * @throws SinkBuffereringError if there is not enough space in the buffer AND
     *                              the strategy is
     *                              {InsertionStrategy#THROW_ON_OVERRUN}.
     */
    public synchronized void insert(byte[] buf, int bufOffset, int amountToInsert) throws InterruptedException {
        try {
            if (this.buffer.length - this.amountBuffered < amountToInsert) {
                switch (this.insertionStrategy) {
                    case BLOCK_ON_OVERRUN: {
                        do {
                            this.wait(); // Wait for data to be consumed.
                        } while (this.buffer.length - this.amountBuffered < amountToInsert); // Check and see if there's
                                                                                             // enough room yet.
                        break; // Fall through to the below code.
                    }

                    case DROP_ON_OVERRUN:
                        amountToInsert = this.buffer.length - this.amountBuffered;
                        break; // Fall through to the below code.

                    case THROW_ON_OVERRUN:
                        throw new SinkBuffereringError();
                }
            }

            int remainingSpace = this.buffer.length - this.bufferWritePos;
            if (remainingSpace < amountToInsert) {
                // We have to first split up our write since the buffer is not circular.
                System.arraycopy(buf, bufOffset, this.buffer, this.bufferWritePos, remainingSpace);
                this.bufferWritePos = 0;
                this.amountBuffered += remainingSpace;
                bufOffset += remainingSpace;
                amountToInsert -= remainingSpace; // We set all of these for the code below.
            }

            System.arraycopy(buf, bufOffset, this.buffer, this.bufferWritePos, amountToInsert);
            this.bufferWritePos += amountToInsert;
            this.amountBuffered += amountToInsert;

            if (this.bufferWritePos == this.buffer.length) {
                this.bufferWritePos = 0; // Wrap around.
            }
        } finally {
            this.notifyAll();
        }
    }

    /**
     * @throws SinkBuffereringError if there is not enough data in the buffer AND
     *                              the strategy is
     *                              {@link ExtractionStrategy#THROW_ON_UNDERRUN}.
     */
    public synchronized void extract(byte[] buf, int bufOffset, int amountToExtract) throws InterruptedException {
        try {
            if (this.amountBuffered < amountToExtract) { // The while loop is for
                switch (this.extractionStrategy) {
                    case BLOCK_ON_UNDERRUN: {
                        do {
                            this.wait(); // Wait for new data to come in.
                        } while (this.amountBuffered < amountToExtract);// Check and see if there's enough room yet.
                        break; // Fall through to the below code.
                    }

                    case THROW_ON_UNDERRUN:
                        throw new SinkBuffereringError();

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
        } finally {
            this.notifyAll();
        }
    }

    @Override
    public synchronized String toString() {
        String[] lines = {
                "Buffer:        |", // 0
                "Write Pointer:  ", // 1
                "Read Pointer:   ", // 2
        };

        // Write the buffer data.
        for (byte b : this.buffer) {
            lines[0] += String.format(" %02x", b);
        }
        lines[0] += " |";

        // Add the write pointer.
        for (int i = 0; i < this.bufferWritePos; i++) {
            lines[1] += "   ";
        }
        lines[1] += " ^";

        // Add the read pointer.
        for (int i = 0; i < this.bufferReadPos; i++) {
            lines[2] += "   ";
        }
        lines[2] += " ^";

        return String.join("\n", lines);
    }

}

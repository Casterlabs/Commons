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

import lombok.NonNull;

/**
 * An InputStream wrapper that allows you to "peek" ahead at bytes without
 * consuming them like a read() would.
 * 
 * TODO Implement mark() behavior.
 * 
 * @see {@link PeekableInputStream#peek(int)}
 */
public class PeekableInputStream extends InputStream {
    private final InputStream wrapped;

    private byte[] currentBuffer = null;
    private int bufferPos = 0;

    public PeekableInputStream(@NonNull InputStream toWrap) {
        this.wrapped = toWrap;
    }

    /**
     * @return if the stream is current buffering data as the result of a peek()
     *         operation.
     */
    public synchronized boolean hasDataBuffered() {
        return this.currentBuffer != null;
    }

    /**
     * @return the amount of data buffered as the result of a peek() operation or 0
     *         if there is no data peeked.
     */
    public synchronized int amountBuffered() {
        if (this.hasDataBuffered()) {
            return this.currentBuffer.length - this.bufferPos;
        } else {
            return 0;
        }
    }

    /**
     * Pre-buffers the amount of specified bytes to speed up future peek()s. Use
     * this if you plan on peeking a lot of bytes consecutively.
     * 
     * @param  nbytes                   the amount of bytes to buffer for future
     *                                  peek()s.
     * 
     * @throws IllegalArgumentException if {@code bytes} is negative
     * @throws IOException              if an I/O error occurs
     * @throws OutOfMemoryError         if there is not enough memory to buffer the
     *                                  bytes
     */
    public synchronized void preBuffer(int nbytes) throws IOException {
        if (nbytes < 0) {
            throw new IllegalArgumentException("`nbytes` cannot negative.");
        }

        if (this.amountBuffered() < nbytes) {
            if (this.hasDataBuffered()) {
                // We need to append to our existing buffer.
                final int oldAmountBuffered = this.amountBuffered();
                final byte[] oldBuffer = this.currentBuffer;

                final int newBufferSize = nbytes;

                this.currentBuffer = new byte[newBufferSize];
                System.arraycopy(oldBuffer, this.bufferPos, this.currentBuffer, 0, oldAmountBuffered);

                this.bufferPos = 0;

                int bytesReadIntoBuffer = oldAmountBuffered;
                do {
                    // Keep reading into the buffer until we have the correct amount.
                    bytesReadIntoBuffer += this.wrapped.read(this.currentBuffer, bytesReadIntoBuffer, newBufferSize - bytesReadIntoBuffer);
                } while (bytesReadIntoBuffer < newBufferSize);
            } else {
                // We need to create a buffer.
                final int toRead = nbytes;

                this.currentBuffer = new byte[toRead];
                this.bufferPos = 0;

                int bytesReadIntoBuffer = 0;
                do {
                    // Keep reading into the buffer until we have the correct amount.
                    bytesReadIntoBuffer += this.wrapped.read(this.currentBuffer, bytesReadIntoBuffer, toRead - bytesReadIntoBuffer);
                } while (bytesReadIntoBuffer < toRead);
            }
        } // Otherwise, we already have the data buffered :D
    }

    /**
     * @param  ahead                    the amount of bytes to skip/peek over.
     * 
     * @return                          the value in the stream at that position
     * 
     * @throws IllegalArgumentException if {@code ahead} is negative
     * @throws IOException              if an I/O error occurs
     * @throws OutOfMemoryError         if there is not enough memory to buffer the
     *                                  peeked bytes
     */
    public synchronized int peek(int ahead) throws IOException {
        if (ahead < 0) {
            throw new IllegalArgumentException("`ahead` cannot negative, use a time machine next time.");
        }

        this.preBuffer(ahead); // Ensures the buffer has the data.

        // Gaze upon the buffer for the peeked value.
        return this.currentBuffer[this.bufferPos + ahead - 1];
    }

    /**
     * See: {@link InputStream#read()}
     */
    @Override
    public synchronized int read() throws IOException {
        if (!this.hasDataBuffered()) {
            return this.wrapped.read();
        }

        int result = this.currentBuffer[this.bufferPos++];

        if (this.bufferPos == this.currentBuffer.length) {
            this.currentBuffer = null;
        }

        return result;
    }

    /**
     * See: {@link InputStream#read(byte[], int, int)}
     */
    @Override
    public synchronized int read(byte b[], int off, int len) throws IOException {
        if (!this.hasDataBuffered()) {
            return this.wrapped.read(b, off, len);
        }

        int nread = Math.min(len, this.amountBuffered());
        System.arraycopy(this.currentBuffer, this.bufferPos, b, off, nread);

        this.bufferPos += nread; // Update the buffer pointer and check for reset.

        if (this.bufferPos == this.currentBuffer.length) {
            this.currentBuffer = null;
        }

        return nread;
    }

    /**
     * See: {@link InputStream#skip(long)}
     */
    @Override
    public synchronized long skip(long n) throws IOException {
        if (!this.hasDataBuffered()) {
            return this.wrapped.skip(n);
        }

        long nread = Math.min(n, this.amountBuffered());
        this.bufferPos += nread; // Just move the pointer.

        if (this.bufferPos == this.currentBuffer.length) {
            this.currentBuffer = null;
        }

        return nread;
    }

    /**
     * See: {@link InputStream#available()}
     */
    @Override
    public synchronized int available() throws IOException {
        if (!this.hasDataBuffered()) {
            return this.wrapped.available();
        }

        return this.amountBuffered();
    }

    /**
     * Closes the PeekableInputStream, discarding any buffered bytes and also closes
     * the underlying wrapped InputStream. <br />
     * <br />
     * You may consider using {@link #amountBuffered()} to read out all of the
     * buffered bytes and then dereferencing this instance to avoid closing the
     * wrapped stream.
     * 
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        this.currentBuffer = null;
        this.wrapped.close();
    }

}

package co.casterlabs.commons.io.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Basically we buffered too many bytes, so we need to give them back during a
 * read.
 */
@RequiredArgsConstructor
public class OverzealousInputStream extends InputStream {
    private final ReentrantLock lock = new ReentrantLock();

    private final InputStream underlying;

    private byte[] overage = new byte[1024];
    private int overageEnd = 0;
    private int overageIndex = 0;

    private void ensureCapacity(int additionalSize) {
        int requiredCapacity = this.overageEnd - this.overageIndex + additionalSize;
        if (requiredCapacity > this.overage.length) {
            int newCapacity = Math.max(this.overage.length * 2, requiredCapacity);
            byte[] newBuffer = new byte[newCapacity];
            System.arraycopy(this.overage, this.overageIndex, newBuffer, 0, this.overageEnd - this.overageIndex);
            this.overageEnd -= this.overageIndex;
            this.overageIndex = 0;
            this.overage = newBuffer;
        } else if (this.overageIndex > 0) {
            // Shift data if there’s unused space at the beginning
            System.arraycopy(this.overage, this.overageIndex, this.overage, 0, this.overageEnd - this.overageIndex);
            this.overageEnd -= this.overageIndex;
            this.overageIndex = 0;
        }
    }

    public void append(@NonNull byte[] data, int startAt, int endAt) {
        this.lock.lock();
        try {
            int newRemaining = endAt - startAt;
            ensureCapacity(newRemaining);

            // Copy new data to the buffer
            System.arraycopy(data, startAt, this.overage, this.overageEnd, newRemaining);
            this.overageEnd += newRemaining;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public int read() throws IOException {
        this.lock.lock();
        try {
            if (this.overageIndex < this.overageEnd) {
                return this.overage[this.overageIndex++];
            }

            return this.underlying.read();
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) return 0;
        this.lock.lock();
        try {
            if (this.overageIndex < this.overageEnd) {
                int amount = Math.min(this.overageEnd - this.overageIndex, len);
                System.arraycopy(this.overage, this.overageIndex, b, off, amount);
                this.overageIndex += amount;
                return amount;
            }

            return this.underlying.read(b, off, len);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public int available() throws IOException {
        this.lock.lock();
        try {
            if (this.overageIndex < this.overageEnd) {
                return this.overageEnd - this.overageIndex;
            }
            return this.underlying.available();
        } finally {
            this.lock.unlock();
        }
    }

}

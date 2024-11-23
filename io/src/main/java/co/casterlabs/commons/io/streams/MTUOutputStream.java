package co.casterlabs.commons.io.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MTUOutputStream extends OutputStream {
    private final ReentrantLock lock = new ReentrantLock();

    private final OutputStream underlying;
    public final int mtu;

    @Override
    public void write(int b) throws IOException {
        this.lock.lock();
        try {
            this.underlying.write(b);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.lock.lock();
        try {
            // We need to batch the write into chunks of the MTU size.
            while (len > 0) {
                int chunkSize = Math.min(len, this.mtu);
                this.underlying.flush();
                this.underlying.write(b, off, chunkSize);
                len -= chunkSize;
                off += chunkSize;
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void flush() throws IOException {
        this.underlying.flush();
    }

    public static int guessMtu(Socket socket) {
        InetAddress address = socket.getInetAddress();

        if (address.isLoopbackAddress()) {
            // In practice, loopback MTU is usually ~2^64. Because we use this MTU value to
            // determine our buffer size, we want to keep it small to avoid memory issues.
            return 8192; // Arbitrary.
        }

        if (address instanceof Inet4Address) {
            /*
             * ipv4 min. MTU = 576 (though usually around 1500)
             * ipv4 header size = 20-60
             */
            return 1500 - 60;
        } else {
            /*
             * ipv6 min. MTU = 1280
             * ipv6 header size = 40
             */
            return 1280 - 40;
        }
    }

}

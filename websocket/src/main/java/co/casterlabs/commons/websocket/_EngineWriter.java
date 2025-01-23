package co.casterlabs.commons.websocket;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.commons.io.streams.MTUOutputStream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class _EngineWriter {
    private final ReentrantLock writeLock = new ReentrantLock();

    private final _Engine engine;
    MTUOutputStream outputStream;

    void doPingLoop(long timeout) throws InterruptedException, IOException {
        while (true) {
            byte[] someBytes = PrimitiveMarshall.BIG_ENDIAN.longToBytes(System.currentTimeMillis());
            this.sendFrame(true, _OpCode.PING, someBytes, 0, someBytes.length);
            Thread.sleep(timeout);
        }
    }

    void sendOrFragment(int op, byte[] bytes) throws IOException {
        this.writeLock.lock();
        try {
            if (this.engine.state != _State.CONNECTED) return; // Silent return.

            // We want to avoid fragmentation if we can. We use the "MTU" here as a guide to
            // determine whether or not to fragment via CONTINUATION packets, as it would
            // otherwise get fragmented at the link-layer and there are performance
            // implications when that happens with TCP.
            if (bytes.length + Long.BYTES + 2 <= this.outputStream.mtu) {
                this.sendFrame(true, op, bytes, 0, bytes.length);
                return;
            }

            int remaining = bytes.length;
            for (int written = 0; written < bytes.length;) {
                int chunkLen = Math.min(remaining, this.outputStream.mtu);

                boolean fin = remaining - chunkLen == 0;
                int chunkOp = written == 0 ? op : _OpCode.CONTINUATION;

                this.sendFrame(fin, chunkOp, bytes, written, chunkLen);

                written += chunkLen;
                remaining -= chunkLen;
            }
        } catch (IOException e) {
            this.engine.close();
            throw e;
        } finally {
            this.writeLock.unlock();
        }
    }

    void sendFrame(boolean fin, int op, byte[] buf, int off, int len) throws IOException {
        this.writeLock.lock();
        try {
            if (this.engine.state != _State.CONNECTED) return; // Silent return.

            int len7 = len;
            if (len7 > 125) {
                if (len > 65535) {
                    len7 = 127; // Use 64bit length.
                } else {
                    len7 = 126; // Use 16bit length.
                }
            }

            int header1 = 0;
            header1 |= (fin ? 1 : 0) << 7;
            header1 |= op;

            int header2 = 0;
            header2 |= len7;
//            header2 |= 0b00000000; // Mask.

            // Nagle's algorithm is disabled (aka no delay mode), so we batch writes to be
            // more efficient.
            if (len7 == 126) {
                byte[] headerBytes = PrimitiveMarshall.BIG_ENDIAN.intToBytes(len);
                headerBytes[0] = (byte) header1;
                headerBytes[1] = (byte) header2; // We only need the first 16 bits from length, so we can overwrite 1-2 safely.

                this.outputStream.write(headerBytes);
            } else if (len7 == 127) {
                byte[] lenBytes = PrimitiveMarshall.BIG_ENDIAN.longToBytes(len);

                byte[] headerBytes = new byte[Long.BYTES + 2];
                headerBytes[0] = (byte) header1;
                headerBytes[1] = (byte) header2;

                System.arraycopy(lenBytes, 0, headerBytes, 2, Long.BYTES);

                this.outputStream.write(headerBytes);
            } else {
                this.outputStream.write(new byte[] {
                        (byte) header1,
                        (byte) header2
                });
            }

            // Note we use an MTUOutputStream here so that we batch writes to be more
            // efficient when transmitted over the wire.
            this.outputStream.write(buf, off, len);
        } finally {
            this.writeLock.unlock();
        }
    }

}

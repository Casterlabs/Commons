package co.casterlabs.commons.websocket;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.commons.io.streams.OverzealousInputStream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class _EngineReader {
    private final _Engine engine;
    OverzealousInputStream inputStream;

    void doReadLoop() throws IOException {
        // For continuation frames.
        int fragmentedOpCode = 0;
        int fragmentedLength = 0;
        List<byte[]> fragmentedPackets = new LinkedList<>();

        while (!Thread.interrupted() && this.engine.state == _State.CONNECTED) {
            // @formatter:off
                int header1 = this.throwRead();
                int header2 = this.throwRead();
                    
                boolean isFinished = (header1 & 0b10000000) != 0;
                boolean rsv1       = (header1 & 0b01000000) != 0;
                boolean rsv2       = (header1 & 0b00100000) != 0;
                boolean rsv3       = (header1 & 0b00010000) != 0;
                int     op         =  header1 & 0b00001111;
                    
                boolean isMasked   = (header2 & 0b10000000) != 0;
                int     len7       =  header2 & 0b01111111;
                // @formatter:on

            if (rsv1 || rsv2 || rsv3) {
                throw new IllegalArgumentException(String.format("Reserved bits are set, these are not supported! rsv1=%b rsv2=%b rsv3=%b", rsv1, rsv2, rsv3));
            }

            int length;
            if (len7 == 127) {
                // Unsigned 64bit, possibly negative.
                long length_long = PrimitiveMarshall.BIG_ENDIAN.bytesToLong(readN(8));

                if (Long.compareUnsigned(length_long, this.engine.client.maxPayloadLength) > 0) {
                    throw new IllegalArgumentException(String.format("Payload length too large, max %d bytes got %s bytes.", this.engine.client.maxPayloadLength, Long.toUnsignedString(length_long)));
                }

                length = (int) length_long;
            } else if (len7 == 126) {
                // Unsigned 16bit, never negative. This can never be larger than
                // MAX_PAYLOAD_LENGTH.
                length = PrimitiveMarshall.BIG_ENDIAN.bytesToInt(new byte[] {
                        0,
                        0,
                        (byte) this.throwRead(),
                        (byte) this.throwRead(),
                });
            } else {
                // Unsigned 7bit, never negative. This can never be larger than
                // MAX_PAYLOAD_LENGTH.
                length = len7;
            }

            byte[] maskingKey = null;
            if (isMasked) {
                maskingKey = readN(4);
            }

            // Read in the whole payload.
            byte[] payload = readN(length);

            // XOR decrypt.
            if (isMasked) {
                for (int idx = 0; idx < payload.length; idx++) {
                    payload[idx] ^= maskingKey[idx % 4];
                }
            }

            // We're starting a new fragmented message, store this info for later.
            if (!isFinished && op != _OpCode.CONTINUATION) {
                fragmentedOpCode = op;
            }

            // Handle fragmented messages.
            if (op == _OpCode.CONTINUATION) {
                fragmentedLength += payload.length;
                if (fragmentedLength > this.engine.client.maxPayloadLength) {
                    throw new IllegalArgumentException(String.format("Fragmented payload length too large, max %d bytes got %d bytes.", this.engine.client.maxPayloadLength, fragmentedLength));
                }

                fragmentedPackets.add(payload);

                if (!isFinished) {
                    // Server is not yet finished, next packet pls.
                    continue;
                }

                // Combine all the fragments together.
                payload = new byte[fragmentedLength];
                int off = 0;
                for (byte[] fp : fragmentedPackets) {
                    System.arraycopy(fp, 0, payload, off, fp.length);
                    off += fp.length;
                }

                // We're finished! Parse it!
                op = fragmentedOpCode;
                fragmentedLength = 0;
                fragmentedPackets.clear();
                break;
            }

            // Parse the op code and do behavior tingz.
            switch (op) {
                case _OpCode.TEXT: {
                    try {
                        String text = new String(payload, StandardCharsets.UTF_8);
                        payload = null; // Early free attempt.
                        this.engine.client.listener.onText(this.engine.client, text);
                    } catch (Throwable t) {
                        this.engine.client.listener.onException(t);
                    }
                    break;
                }

                case _OpCode.BINARY: {
                    try {
                        this.engine.client.listener.onBinary(this.engine.client, payload);
                    } catch (Throwable t) {
                        this.engine.client.listener.onException(t);
                    }
                    break;
                }

                case _OpCode.CLOSE: {
                    this.engine.close(); // Send close reply.
                    return;
                }

                case _OpCode.PING: {
                    this.engine.writer.sendFrame(true, _OpCode.PONG, payload, 0, payload.length); // Send pong reply.
                    continue;
                }

                case _OpCode.PONG: {
                    continue;
                }

                default: // Reserved
                    continue;
            }
        }
    }

    private byte[] readN(int length) throws IOException {
        byte[] buf = new byte[length];
        for (int read = 0; read < length;) {
            int nread = this.inputStream.read(buf, read, length - read);
            if (nread == -1) throw new IOException("Socket closed.");
            read += nread;
        }
        return buf;
    }

    private int throwRead() throws IOException {
        int read = this.inputStream.read();
        if (read == -1) throw new IOException("Socket closed.");
        return read;
    }

}

package co.casterlabs.commons.websocket;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import co.casterlabs.commons.io.marshalling.PrimitiveMarshall;
import co.casterlabs.commons.io.streams.MTUOutputStream;
import co.casterlabs.commons.io.streams.OverzealousInputStream;
import co.casterlabs.commons.websocket._ConnectionUtil.ResponseLineInfo;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class WebSocketClient implements Closeable {
    private final ReentrantLock lock = new ReentrantLock();

    private @Setter int maxPayloadLength = 16 /*mb*/ * 1024 * 1024;
    private @Setter @NonNull ThreadFactory threadFactory = Executors.defaultThreadFactory();
    private @Setter @NonNull SocketFactory socketFactory; // This is populated by default in the constructor.
    private @Setter @NonNull WebSocketListener listener = new WebSocketListener() {
    };

    private final Map<String, String> connectionHeaders = new HashMap<>();
    private final InetSocketAddress address;
    private final String path;
    private final @Getter boolean isSSL;

    private State state = State.NEVER_CONNECTED;

    private Socket socket;
    private OverzealousInputStream inputStream;
    private MTUOutputStream outputStream;

    private Thread readThread;
    private Thread pingThread;

    private Object attachment;

    public WebSocketClient(@NonNull URI uri) {
        this(uri, Collections.emptyMap(), Collections.emptyList());
    }

    public WebSocketClient(@NonNull URI uri, @NonNull Map<String, String> additionalHeaders, @NonNull List<String> acceptedProtocols) {
        String httpHost = uri.getHost();
        int port;

        if (uri.getScheme().equalsIgnoreCase("wss")) {
            this.isSSL = true;
            port = 443;
            this.socketFactory = SSLSocketFactory.getDefault();
        } else if (uri.getScheme().equalsIgnoreCase("ws")) {
            this.isSSL = false;
            port = 80;
            this.socketFactory = SocketFactory.getDefault();
        } else {
            throw new IllegalArgumentException("Unknown scheme: " + uri.getScheme());
        }

        if (uri.getPort() != -1) {
            port = uri.getPort();
            httpHost += ':';
            httpHost += uri.getPort();
        }

        this.address = new InetSocketAddress(uri.getHost(), port);

        String httpPath = uri.getRawPath();
        if (httpPath == null || httpPath.isEmpty()) {
            httpPath = "/";
        }
        if (uri.getRawQuery() != null) {
            httpPath += '?' + uri.getQuery();
        }

        this.path = httpPath;

        // These headers can be overwritten
        this.connectionHeaders.put("User-Agent".toLowerCase(), "Casterlabs/Commons-WebSocket");
        this.connectionHeaders.put("Host".toLowerCase(), httpHost);

        this.connectionHeaders.putAll(additionalHeaders); // Override.

        // These headers must NOT be overwritten.
        this.connectionHeaders.put("Connection".toLowerCase(), "Upgrade");
        this.connectionHeaders.put("Upgrade".toLowerCase(), "websocket");
        this.connectionHeaders.put("Sec-WebSocket-Version".toLowerCase(), "13");

        if (acceptedProtocols != null && !acceptedProtocols.isEmpty()) {
            this.connectionHeaders.put("Sec-WebSocket-Protocol".toLowerCase(), String.join(", ", acceptedProtocols));
        }

        byte[] key = new byte[16];
        ThreadLocalRandom.current().nextBytes(key);
        this.connectionHeaders.put("Sec-WebSocket-Key".toLowerCase(), Base64.getEncoder().encodeToString(key));
    }

    public boolean isConnected() {
        return this.state == State.CONNECTED;
    }

    public void attachment(Object attachment) {
        this.attachment = attachment;
    }

    @SuppressWarnings("unchecked")
    public <T> T attachment() {
        return (T) this.attachment;
    }

    private void doPing(long timeout) {
        try {
            while (true) {
                byte[] someBytes = PrimitiveMarshall.BIG_ENDIAN.longToBytes(System.currentTimeMillis());
                this.sendFrame(true, WebsocketOpCode.PING, someBytes);
                Thread.sleep(timeout);
            }
        } catch (Exception ignored) {
            this.readThread.interrupt();
        }
    }

    private void doRead() throws IOException {
        try (Socket _s = this.socket) {
            // For continuation frames.
            int fragmentedOpCode = 0;
            int fragmentedLength = 0;
            List<byte[]> fragmentedPackets = new LinkedList<>();

            while (!Thread.interrupted() && this.state == State.CONNECTED) {
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
                    long length_long = PrimitiveMarshall.BIG_ENDIAN.bytesToLong(
                        this.inputStream.readNBytes(8)
                    );

                    if (Long.compareUnsigned(length_long, this.maxPayloadLength) > 0) {
                        throw new IllegalArgumentException(String.format("Payload length too large, max %d bytes got %s bytes.", this.maxPayloadLength, Long.toUnsignedString(length_long)));
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
                    maskingKey = this.inputStream.readNBytes(4);
                }

                // Read in the whole payload.
                byte[] payload = this.inputStream.readNBytes(length);

                // XOR decrypt.
                if (isMasked) {
                    for (int idx = 0; idx < payload.length; idx++) {
                        payload[idx] ^= maskingKey[idx % 4];
                    }
                }

                // We're starting a new fragmented message, store this info for later.
                if (!isFinished && op != 0) {
                    fragmentedOpCode = op;
                }

                // Handle fragmented messages.
                if (op == 0) {
                    fragmentedLength += payload.length;
                    if (fragmentedLength > this.maxPayloadLength) {
                        throw new IllegalArgumentException(String.format("Fragmented payload length too large, max %d bytes got %d bytes.", this.maxPayloadLength, fragmentedLength));
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
                    case WebsocketOpCode.TEXT: {
                        try {
                            String text = new String(payload, StandardCharsets.UTF_8);
                            payload = null; // Early free attempt.
                            this.listener.onText(this, text);
                        } catch (Throwable t) {
                            this.listener.onException(t);
                        }
                        break;
                    }

                    case WebsocketOpCode.BINARY: {
                        try {
                            this.listener.onBinary(this, payload);
                        } catch (Throwable t) {
                            this.listener.onException(t);
                        }
                        break;
                    }

                    case WebsocketOpCode.CLOSE: {
                        this.close(); // Send close reply.
                        return;
                    }

                    case WebsocketOpCode.PING: {
                        this.sendFrame(true, WebsocketOpCode.PONG, payload); // Send pong reply.
                        continue;
                    }

                    case WebsocketOpCode.PONG: {
                        continue;
                    }

                    default: // Reserved
                        continue;
                }
            }
        } finally {
            this.pingThread.interrupt();
        }
    }

    public void connect(long timeout, long pingInterval) throws IOException {
        this.lock.lock();
        try {
            if (this.state != State.NEVER_CONNECTED) throw new IllegalStateException("Current state is: " + this.state);
            this.state = State.CONNECTING;

            this.socket = this.socketFactory.createSocket();
            this.socket.connect(this.address, (int) timeout);
            this.socket.setSoTimeout((int) timeout);
            this.socket.setTcpNoDelay(true);

            this.inputStream = new OverzealousInputStream(this.socket.getInputStream());
            this.outputStream = new MTUOutputStream(this.socket.getOutputStream(), MTUOutputStream.guessMtu(this.socket));

            StringBuilder handshake = new StringBuilder()
                .append("GET ")
                .append(this.path)
                .append(" HTTP/1.1\r\n");

            for (Map.Entry<String, String> header : this.connectionHeaders.entrySet()) {
                handshake
                    .append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\r\n");
            }

            handshake.append("\r\n");

            this.outputStream.write(handshake.toString().getBytes(_ConnectionUtil.CHARSET));

            ResponseLineInfo responseLine = _ConnectionUtil.readResponseLine(this.inputStream, this.maxPayloadLength);
            if (responseLine.statusCode != 101) {
                throw new IOException("Expected status code 101, got " + responseLine.statusCode + ": " + responseLine.statusMessage);
            }

            Map<String, String> headers = _ConnectionUtil.readHeaders(this.inputStream, this.maxPayloadLength);
            String acceptedProtocol = headers.get("Sec-WebSocket-Protocol".toLowerCase());

            this.state = State.CONNECTED;
            this.listener.onOpen(this, headers, acceptedProtocol);

            this.pingThread = this.threadFactory.newThread(() -> this.doPing(pingInterval));
            this.pingThread.setName("WebSocket Client Ping Thread - " + this.address);
            this.pingThread.start();

            this.readThread = this.threadFactory.newThread(() -> {
                try {
                    this.doRead();
                } catch (Throwable t) {
                    this.listener.onException(t);
                } finally {
                    this.close();
                }
            });
            this.readThread.setName("WebSocket Client Read Thread - " + this.address);
            this.readThread.start();
        } catch (IOException e) {
            this.close();
            throw e;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void close() {
        this.lock.lock();
        try {
            if (this.state != State.CONNECTED) return; // Silent return.

            this.listener.onClosed(this);

            try {
                this.sendFrame(true, WebsocketOpCode.CLOSE, new byte[0]);
            } catch (Throwable ignored) {}

            try {
                this.socket.close();
            } catch (Throwable ignored) {}
        } finally {
            this.state = State.CLOSED;
            this.lock.unlock();
        }
    }

    public void waitFor() throws InterruptedException {
        if (this.state != State.CONNECTED) return; // Silent return.
        this.readThread.join();
    }

    public void send(String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        this.sendOrFragment(WebsocketOpCode.TEXT, bytes);
    }

    public void send(byte[] bytes) throws IOException {
        this.sendOrFragment(WebsocketOpCode.BINARY, bytes);
    }

    private void sendOrFragment(int op, byte[] bytes) throws IOException {
        this.lock.lock();
        try {
            if (this.state != State.CONNECTED) return; // Silent return.

            if (bytes.length <= this.outputStream.mtu) {
                // Don't fragment.
                this.sendFrame(true, op, bytes);
                return;
            }

            int toWrite = bytes.length;
            int written = 0;

            while (toWrite > 0) {
                byte[] chunk = new byte[Math.min(toWrite, this.outputStream.mtu)];
                System.arraycopy(bytes, written, chunk, 0, chunk.length);
                toWrite -= chunk.length;

                boolean fin = toWrite == 0;
                int chunkOp = written == 0 ? op : WebsocketOpCode.CONTINUATION;

                this.sendFrame(fin, chunkOp, chunk);

                written += chunk.length;
            }
        } catch (IOException e) {
            this.close();
            throw e;
        } finally {
            this.lock.unlock();
        }
    }

    private void sendFrame(boolean fin, int op, byte[] bytes) throws IOException {
        this.lock.lock();
        try {
            if (this.state != State.CONNECTED) return; // Silent return.

            int len7 = bytes.length;
            if (len7 > 125) {
                if (bytes.length > 65535) {
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
                byte[] headerBytes = PrimitiveMarshall.BIG_ENDIAN.intToBytes(bytes.length);
                headerBytes[0] = (byte) header1;
                headerBytes[1] = (byte) header2; // We only need the first 16 bits from length, so we can overwrite 1-2 safely.

                this.outputStream.write(headerBytes);
            } else if (len7 == 127) {
                byte[] lenBytes = PrimitiveMarshall.BIG_ENDIAN.longToBytes(bytes.length);

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
            this.outputStream.write(bytes);
        } finally {
            this.lock.unlock();
        }
    }

    private static class WebsocketOpCode {
        private static final int CONTINUATION = 0;
        private static final int TEXT = 1;
        private static final int BINARY = 2;
        private static final int CLOSE = 8;
        private static final int PING = 9;
        private static final int PONG = 10;
    }

    private static enum State {
        NEVER_CONNECTED,
        CONNECTING,
        CONNECTED,
        CLOSED
    }

    private int throwRead() throws IOException {
        int read = this.inputStream.read();
        if (read == -1) throw new IOException("Socket closed.");
        return read;
    }

}

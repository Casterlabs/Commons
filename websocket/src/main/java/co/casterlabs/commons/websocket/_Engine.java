package co.casterlabs.commons.websocket;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import co.casterlabs.commons.io.streams.MTUOutputStream;
import co.casterlabs.commons.io.streams.OverzealousInputStream;
import co.casterlabs.commons.websocket._HttpUtil.ResponseLineInfo;
import lombok.NonNull;

class _Engine implements Closeable {
    final WebSocketClient client;

    _State state = _State.NEVER_CONNECTED;

    private final ReentrantLock stateLock = new ReentrantLock();

    private final Map<String, String> connectionHeaders = new HashMap<>();
    private final InetSocketAddress address;
    private final String path;

    private Socket socket;

    final _EngineReader reader = new _EngineReader(this);
    final _EngineWriter writer = new _EngineWriter(this);

    private Thread readThread;
    private Thread pingThread;

    _Engine(WebSocketClient client, @NonNull URI uri, @NonNull Map<String, String> additionalHeaders, @NonNull List<String> acceptedProtocols) {
        this.client = client;

        String httpHost = uri.getHost();
        int port;

        if (uri.getScheme().equalsIgnoreCase("wss")) {
            port = 443;
            this.client.setSocketFactory(SSLSocketFactory.getDefault());
        } else if (uri.getScheme().equalsIgnoreCase("ws")) {
            port = 80;
            this.client.setSocketFactory(SocketFactory.getDefault());
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

        for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
            this.connectionHeaders.put(entry.getKey().toLowerCase(), entry.getValue()); // Override.
        }

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

    void connect(long timeout, long pingInterval) throws IOException {
        this.stateLock.lock();
        try {
            if (this.state != _State.NEVER_CONNECTED) throw new IllegalStateException("Current state is: " + this.state);
            this.state = _State.CONNECTING;

            this.socket = this.client.socketFactory.createSocket();
            this.socket.connect(this.address, (int) timeout);
            this.socket.setSoTimeout((int) timeout);
            this.socket.setTcpNoDelay(true);

            this.reader.inputStream = new OverzealousInputStream(this.socket.getInputStream());
            this.writer.outputStream = new MTUOutputStream(this.socket.getOutputStream(), MTUOutputStream.guessMtu(this.socket));

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

            this.writer.outputStream.write(handshake.toString().getBytes(_HttpUtil.CHARSET));

            ResponseLineInfo responseLine = _HttpUtil.readResponseLine(this.reader.inputStream, this.client.maxPayloadLength);
            if (responseLine.statusCode != 101) {
                throw new IOException("Expected status code 101, got " + responseLine.statusCode + ": " + responseLine.statusMessage);
            }

            Map<String, String> headers = _HttpUtil.readHeaders(this.reader.inputStream, this.client.maxPayloadLength);
            String acceptedProtocol = headers.get("Sec-WebSocket-Protocol".toLowerCase());

            this.state = _State.CONNECTED;
            this.client.listener.onOpen(this.client, headers, acceptedProtocol);

            this.pingThread = this.client.threadFactory.newThread(() -> {
                try {
                    this.writer.doPingLoop(pingInterval);
                } catch (Throwable ignored) {} finally {
                    this.close();
                }
            });
            this.pingThread.setName("WebSocket Client Ping Thread - " + this.address);
            this.pingThread.start();

            this.readThread = this.client.threadFactory.newThread(() -> {
                try {
                    this.reader.doReadLoop();
                } catch (Throwable ignored) {} finally {
                    this.close();
                }
            });
            this.readThread.setName("WebSocket Client Read Thread - " + this.address);
            this.readThread.start();
        } catch (IOException e) {
            this.close();
            throw e;
        } finally {
            this.stateLock.unlock();
        }
    }

    @Override
    public void close() {
        this.stateLock.lock();
        try {
            if (this.state != _State.CONNECTED) return; // Silent return.
            this.state = _State.CLOSED;

            try {
                this.writer.sendFrame(true, _OpCode.CLOSE, new byte[0], 0, 0);
            } catch (Throwable ignored) {}

            try {
                this.socket.close();
            } catch (Throwable ignored) {}

            this.readThread.interrupt();
            this.pingThread.interrupt();

            this.client.listener.onClosed(this.client);
        } finally {
            this.stateLock.unlock();
        }
    }

    public void waitFor() throws InterruptedException {
        if (this.state != _State.CONNECTED) return; // Silent return.
        this.readThread.join();
    }

}

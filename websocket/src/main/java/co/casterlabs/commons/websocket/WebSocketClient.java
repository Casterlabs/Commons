package co.casterlabs.commons.websocket;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.net.SocketFactory;

import lombok.NonNull;
import lombok.Setter;

public class WebSocketClient implements Closeable {
    @Setter
    int maxPayloadLength = 16 /*mb*/ * 1024 * 1024;

    @Setter
    @NonNull
    ThreadFactory threadFactory = Executors.defaultThreadFactory();

    @Setter
    @NonNull
    SocketFactory socketFactory; // This is populated by default in the constructor.

    @Setter
    @NonNull
    WebSocketListener listener = new WebSocketListener() {
    };

    private _Engine engine;
    private Object attachment;

    public WebSocketClient(@NonNull URI uri) {
        this(uri, Collections.emptyMap(), Collections.emptyList());
    }

    public WebSocketClient(@NonNull URI uri, @NonNull Map<String, String> additionalHeaders) {
        this(uri, additionalHeaders, Collections.emptyList());
    }

    public WebSocketClient(@NonNull URI uri, @NonNull List<String> acceptedProtocols) {
        this(uri, Collections.emptyMap(), acceptedProtocols);
    }

    public WebSocketClient(@NonNull URI uri, @NonNull Map<String, String> additionalHeaders, @NonNull List<String> acceptedProtocols) {
        this.engine = new _Engine(this, uri, additionalHeaders, acceptedProtocols);
    }

    public boolean isConnected() {
        return this.engine.state == _State.CONNECTED;
    }

    public void attachment(Object attachment) {
        this.attachment = attachment;
    }

    @SuppressWarnings("unchecked")
    public <T> T attachment() {
        return (T) this.attachment;
    }

    public void connect(long timeout, long pingInterval) throws IOException {
        this.engine.connect(timeout, pingInterval);
    }

    @Override
    public void close() {
        this.engine.close();
    }

    public void waitFor() throws InterruptedException {
        this.engine.waitFor();
    }

    public void send(String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        this.engine.writer.sendOrFragment(_OpCode.TEXT, bytes);
    }

    public void send(byte[] bytes) throws IOException {
        this.engine.writer.sendOrFragment(_OpCode.BINARY, bytes);
    }

}

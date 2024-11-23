package co.casterlabs.commons.websocket;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

public interface WebSocketListener {

    default void onOpen(WebSocketClient client, Map<String, String> headers, @Nullable String acceptedProtocol) {}

    default void onClosed(WebSocketClient client) {}

    default void onText(WebSocketClient client, String string) {}

    default void onBinary(WebSocketClient client, byte[] bytes) {}

    default void onException(Throwable t) {
        t.printStackTrace();
    }

}

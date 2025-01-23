package co.casterlabs.commons.websocket;

class _OpCode {
    // @formatter:off
    static final int CONTINUATION = 0x0;
    static final int TEXT         = 0x1;
    static final int BINARY       = 0x2;
    static final int CLOSE        = 0x8;
    static final int PING         = 0x9;
    static final int PONG         = 0xA;
    // @formatter:on
}

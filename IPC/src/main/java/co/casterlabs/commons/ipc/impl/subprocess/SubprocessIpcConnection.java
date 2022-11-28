package co.casterlabs.commons.ipc.impl.subprocess;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.async.queue.ThreadExecutionQueue;
import co.casterlabs.commons.ipc.IpcConnection;
import co.casterlabs.commons.ipc.packets.IpcPacket;
import co.casterlabs.commons.ipc.packets.IpcPacket.IpcPacketType;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.SneakyThrows;

public abstract class SubprocessIpcConnection implements Closeable {
    static final long PING_INTERVAL = TimeUnit.SECONDS.toMillis(1);
    static final long PING_TIMEOUT = PING_INTERVAL * 2;
    private static final byte MESSAGE_PACKET_TYPE = -1;

    private final ThreadExecutionQueue dispatchThread = new ThreadExecutionQueue();
    private final WrappedIpcConnection connection = new WrappedIpcConnection();

    private long lastPing = System.currentTimeMillis();

    private OutputStream out;
    private InputStream in;

    void init(OutputStream out, InputStream in) {
        this.out = out;
        this.in = in;

        // Read byte messages from the process.
        AsyncTask.createNonDaemon(() -> {
            try {
                while (true) {
                    // 1) Read the length.
                    // 2) Read the user-provided type.
                    // 3) Read the message.
                    // 4) Finally, read a null, so we can double check the write was good.
                    int len = this.in.read();
                    int type = this.in.read();

                    byte[] buff = new byte[len];
                    if (this.in.read(buff) < len) {
                        throw new IOException("Got less bytes than expected when reading from IPC process byte stream.");
                    }

                    int expectedNull = this.in.read();
                    if (expectedNull != 0) {
                        throw new IOException("Expected a sanity null-termination byte. Got: " + expectedNull);
                    }

                    if (type == MESSAGE_PACKET_TYPE) {
                        debugMessage("Received message.");
                        JsonObject json = Rson.DEFAULT.fromJson(new String(buff), JsonObject.class);

                        IpcPacket packet = IpcPacketType.get(json);
                        this.connection.receive0(packet);
                    } else {
                        debugMessage("Received %d bytes of type %d.", buff.length, type);
                        this.handleByteMessage(type, buff);
                    }
                }
            } catch (Exception e) {
                debugError(e);
                this.close();
                return;
            }
        });

        // Ping thread.
        AsyncTask.createNonDaemon(() -> {
            try {
                while (true) {
                    Thread.sleep(PING_INTERVAL);

                    // Check and make sure we didn't timeout.
                    if (System.currentTimeMillis() > this.lastPing + PING_TIMEOUT) {
                        this.close();
                        return;
                    }

                    // Send a ping.
                    this.connection.sendMessage(SubprocessIpcPingPacket.INSTANCE);
                }
            } catch (Exception e) {
                debugError(e);
                this.close();
                return;
            }
        });
    }

    @Override
    public final void close() {
        try {
            this.close0();
        } catch (Throwable t) {
            debugError(t);
        }
    }

    abstract void close0();

    public abstract void handleMessage(Object message);

    public abstract void handleByteMessage(int type, byte[] message);

    public final void sendMessage(Object message) {
        debugMessage("Sending message.");
        this.connection.sendMessage(message);
    }

    public final void sendByteMessage(byte type, byte[] message) {
        try {
            assert type != MESSAGE_PACKET_TYPE : "Type " + MESSAGE_PACKET_TYPE + " is used internally.";
            debugMessage("Sending %d bytes of type %d.", message.length, type);
            _sendByteMessage(type, message);
        } catch (Exception e) {
            debugError(e);
//            System.exit(1);
        }
    }

    final synchronized void _sendByteMessage(byte type, byte[] message) throws IOException {
        // 1) Write the length.
        // 2) Write the user-provided type.
        // 3) Write the message.
        // 4) Finally, write a null, so we can double check the read was good.
        this.out.write(message.length);
        this.out.write(type);
        this.out.write(message);
        this.out.write(0);
        this.out.flush();
    }

    void debugError(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        t.printStackTrace(pw);

        String out = sw.toString();

        pw.flush();
        pw.close();
        sw.flush();

        debugMessage(
            out
                .substring(0, out.length() - 2)
                .replace("\r", "")
        );
    }

    void debugMessage(String format, Object... args) {
        System.err.printf("[%s] %s\n", this.getClass().getSimpleName(), String.format(format, args));
    }

    class WrappedIpcConnection extends IpcConnection {

        protected void receive0(IpcPacket packet) {
            this.receive(packet);
        }

        @SneakyThrows
        @Override
        protected void handleMessage(Object message) {
            if (message instanceof SubprocessIpcPacket) {
                SubprocessIpcPacket packet = (SubprocessIpcPacket) message;

                switch (packet.getType()) {
                    case PING:
                        lastPing = System.currentTimeMillis();
                        break;

                    case PRINT: {
                        SubprocessIpcPrintPacket printPacket = (SubprocessIpcPrintPacket) packet;
                        byte[] bytes = Base64.getDecoder().decode(printPacket.getBytes());

                        switch (printPacket.getChannel()) {
                            case STDERR:
                                System.err.write(bytes);
                                System.err.flush();
                                break;

                            case STDOUT:
                                System.out.write(bytes);
                                System.out.flush();
                                break;
                        }
                        break;
                    }
                }
            } else {
                dispatchThread.execute(() -> {
                    handleMessage(message);
                });
            }
        }

        @SneakyThrows
        @Override
        protected void send(IpcPacket packet) {
            _sendByteMessage(MESSAGE_PACKET_TYPE, Rson.DEFAULT.toJsonString(packet).getBytes());
        }
    }

}

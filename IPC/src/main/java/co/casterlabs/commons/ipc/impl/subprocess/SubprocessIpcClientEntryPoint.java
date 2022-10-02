package co.casterlabs.commons.ipc.impl.subprocess;

import static co.casterlabs.commons.ipc.impl.subprocess.SubprocessIpc.PING_INTERVAL;
import static co.casterlabs.commons.ipc.impl.subprocess.SubprocessIpc.PING_TIMEOUT;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Base64;
import java.util.Scanner;

import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.async.queue.ThreadQueue;
import co.casterlabs.commons.ipc.IpcConnection;
import co.casterlabs.commons.ipc.impl.subprocess.SubprocessIpcPrintPacket.PrintChannel;
import co.casterlabs.commons.ipc.packets.IpcPacket;
import co.casterlabs.commons.ipc.packets.IpcPacket.IpcPacketType;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AllArgsConstructor;

public class SubprocessIpcClientEntryPoint {
    private static final ThreadQueue dispatchThread = new ThreadQueue();
    private static final InputStream nativeIn = System.in;
    private static final PrintStream nativeOut = System.out;
    static final PrintStream nativeErr = System.err; // We use stderr for raw byte messages.

    private static long lastPing = System.currentTimeMillis();

    private static SubprocessIpcClientHandler handler;

    final static WrappedIpcConnection connection = new WrappedIpcConnection();

    @SuppressWarnings({
            "deprecation",
            "unchecked"
    })
    public static void main(String[] args) throws Exception {
        // Override IO
        System.setOut(new PrintStream(new IpcOutputStream(PrintChannel.STDOUT), true));
        System.setErr(new PrintStream(new IpcOutputStream(PrintChannel.STDERR), true));
        System.setIn(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new UnsupportedOperationException("You cannot read System.in from an IPC child process.");
            }
        }); // NOOP

        String clientHandlerClassName = args[0];
        Class<? extends SubprocessIpcClientHandler> clientHandlerClazz = //
                (Class<? extends SubprocessIpcClientHandler>) Class.forName(clientHandlerClassName);

        handler = clientHandlerClazz.newInstance();

        // Read packets from stdin.
        AsyncTask.createNonDaemon(() -> {
            try (Scanner in = new Scanner(nativeIn)) {
                while (true) {
                    String line = in.nextLine();
                    JsonObject json = Rson.DEFAULT.fromJson(line, JsonObject.class);

                    IpcPacket packet = IpcPacketType.get(json);
                    connection.handlePacket(packet);
                }
            } catch (Exception e) {
                SubprocessIpc.debugError(e);
                System.exit(1);
                return;
            }
        });

        // Ping thread.
        AsyncTask.createNonDaemon(() -> {
            try {
                while (true) {
                    Thread.sleep(PING_INTERVAL);

                    // Check and make sure we didn't timeout.
                    if (System.currentTimeMillis() > lastPing + PING_TIMEOUT) {
                        System.exit(1);
                        return;
                    }

                    // Send a ping.
                    connection.sendMessage(SubprocessIpcPingPacket.INSTANCE);
                }
            } catch (Exception e) {
                SubprocessIpc.debugError(e);
                System.exit(1);
                return;
            }
        });
    }

    static class WrappedIpcConnection extends IpcConnection {

        protected void handlePacket(IpcPacket packet) {
            this.receive(packet);
        }

        @Override
        protected void handleMessage(Object message) {
            if (message instanceof SubprocessIpcPacket) {
                SubprocessIpcPacket packet = (SubprocessIpcPacket) message;

                switch (packet.getType()) {
                    case PING:
                        lastPing = System.currentTimeMillis();
                        break;

                    // Unused on this side.
                    case PRINT:
                        break;
                }
            } else {
                dispatchThread.submitTask(() -> {
                    handler.handleMessage(message);
                });
            }
        }

        @Override
        protected void send(IpcPacket packet) {
            nativeOut.println(Rson.DEFAULT.toJsonString(packet));
        }
    }

    @AllArgsConstructor
    private static class IpcOutputStream extends OutputStream {
        private PrintChannel printChannel;

        @Override
        public void write(int b) throws IOException {
        } // Never called.

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            byte[] content = new byte[len];
            System.arraycopy(b, off, content, 0, len);

            String base64 = Base64.getEncoder().encodeToString(content);
            connection.sendMessage(new SubprocessIpcPrintPacket(base64, this.printChannel));
        }
    }

}

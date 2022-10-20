package co.casterlabs.commons.ipc.impl.subprocess;

import static co.casterlabs.commons.ipc.impl.subprocess.SubprocessIpc.PING_INTERVAL;
import static co.casterlabs.commons.ipc.impl.subprocess.SubprocessIpc.PING_TIMEOUT;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.async.queue.ThreadExecutionQueue;
import co.casterlabs.commons.ipc.IpcConnection;
import co.casterlabs.commons.ipc.packets.IpcPacket;
import co.casterlabs.commons.ipc.packets.IpcPacket.IpcPacketType;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;

public abstract class SubprocessIpcHostHandler implements Closeable {
    private final ThreadExecutionQueue dispatchThread = new ThreadExecutionQueue();

    private WrappedIpcConnection connection = new WrappedIpcConnection();
    private long lastPing = System.currentTimeMillis();

    private OutputStream processIn;
    private Process process;

    public final void sendMessage(Object message) {
        this.connection.sendMessage(message);
    }

    public final boolean isAlive() {
        return this.process.isAlive();
    }

    public abstract void handleMessage(Object message);

    public abstract void handleByteMessage(int type, byte[] message);

    public abstract void onClose();

    @SneakyThrows
    @Override
    public final void close() {
        if (this.process.isAlive()) {
            this.process.destroy();
        }
    }

    /* -------------------- */
    /* Creation */
    /* -------------------- */

    public SubprocessIpcHostHandler(@NonNull Class<? extends SubprocessIpcClientHandler> handlerClass)
            throws IOException {
        try {
            handlerClass.getConstructor();
        } catch (Exception e) {
            throw new IllegalArgumentException("Handler class must have a public no-args constructor.");
        }

        List<String> exec = getExec(SubprocessIpcClientEntryPoint.class, handlerClass.getTypeName());

        this.process = new ProcessBuilder()
                .command(exec)
                .redirectError(Redirect.PIPE)
                .redirectInput(Redirect.PIPE)
                .redirectOutput(Redirect.PIPE)
                .start();

        InputStream proc_stdout = this.process.getInputStream();
        InputStream proc_stderr = this.process.getErrorStream();
        OutputStream proc_stdin = this.process.getOutputStream();

        this.processIn = proc_stdin;

        // Wait for the process to die.
        AsyncTask.createNonDaemon(() -> {
            try {
                this.process.waitFor();
            } catch (InterruptedException e) {
            }

            this.onClose();
        });

        // Read packets from the process.
        AsyncTask.createNonDaemon(() -> {
            try (Scanner in = new Scanner(proc_stdout)) {
                while (true) {
                    String line = in.nextLine();
                    JsonObject json = Rson.DEFAULT.fromJson(line, JsonObject.class);

                    IpcPacket packet = IpcPacketType.get(json);
                    this.connection.handlePacket(packet);
                }
            } catch (Exception e) {
                SubprocessIpc.debugError(e);
                this.close();
                return;
            }
        });

        // Read byte messages from the process.
        AsyncTask.createNonDaemon(() -> {
            try {
                while (true) {
                    // 1) Read the length.
                    // 2) Read the user provided type.
                    // 3) Read the message.
                    // 4) Finally, read a null, so we can double check the write was good.

                    int len = proc_stderr.read();
                    int type = proc_stderr.read();

                    byte[] buff = new byte[len];
                    if (proc_stderr.read(buff) < len) {
                        throw new IOException(
                                "Got less bytes than expected when reading from IPC process byte stream.");
                    }

                    int expectedNull = proc_stderr.read();
                    if (expectedNull != 0) {
                        throw new IOException("Expected a sanity null-termination byte.");
                    }

                    handleByteMessage(type, buff);
                }
            } catch (Exception e) {
                SubprocessIpc.debugError(e);
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
                SubprocessIpc.debugError(e);
                this.close();
                return;
            }
        });
    }

    private class WrappedIpcConnection extends IpcConnection {

        protected void handlePacket(IpcPacket packet) {
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
                dispatchThread.submitTask(() -> {
                    // Different method.
                    SubprocessIpcHostHandler.this.handleMessage(message);
                });
            }
        }

        @SneakyThrows
        @Override
        protected void send(IpcPacket packet) {
            processIn.write(Rson.DEFAULT.toJsonString(packet).getBytes());
            processIn.write('\n');
            processIn.flush();
        }
    }

    private static List<String> getExec(Class<?> main, String... programArgs) throws IOException {
        List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        String classpath = System.getProperty("java.class.path");
        String javaHome = System.getProperty("java.home");

        String entry = System.getProperty("sun.java.command"); // Tested, present in OpenJDK and Oracle
        String[] launchArgs = entry.split(" ");
        File entryFile = new File(launchArgs[0]);
        if (entryFile.exists()) { // If the entry is a file, not a class.
            classpath += ":" + entryFile.getCanonicalPath();
        }

        List<String> result = new ArrayList<>();

        result.add(String.format("\"%s/bin/java\"", javaHome));
        result.addAll(jvmArgs);
        result.add("-cp");
        result.add('"' + classpath + '"');
        result.add(main.getTypeName());
        result.addAll(Arrays.asList(programArgs));

        return result;
    }

}

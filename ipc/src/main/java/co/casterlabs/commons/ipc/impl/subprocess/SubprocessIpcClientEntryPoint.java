package co.casterlabs.commons.ipc.impl.subprocess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Base64;

import co.casterlabs.commons.ipc.impl.subprocess.SubprocessIpcPrintPacket.PrintChannel;
import lombok.AllArgsConstructor;

public class SubprocessIpcClientEntryPoint {
    private static final InputStream nativeIn = System.in;
    private static final PrintStream nativeOut = System.out;

    private static SubprocessIpcClientHandler handler;

    @SuppressWarnings({
            "deprecation",
            "unchecked"
    })
    public static void main(String[] args) throws Exception {
        // Override IO
        System.setOut(new PrintStream(new IpcOutputStream(PrintChannel.STDOUT), true));
//        System.setErr(new PrintStream(new IpcOutputStream(PrintChannel.STDERR), true));
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
        handler.init(nativeOut, nativeIn);
    }

    @AllArgsConstructor
    private static class IpcOutputStream extends OutputStream {
        private PrintChannel printChannel;

        @Override
        public void write(int b) throws IOException {} // Never called.

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            byte[] content = new byte[len];
            System.arraycopy(b, off, content, 0, len);

            String base64 = Base64.getEncoder().encodeToString(content);
            handler.sendMessage(new SubprocessIpcPrintPacket(base64, this.printChannel));
        }
    }

}

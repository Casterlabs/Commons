package co.casterlabs.commons.ipc.impl.subprocess;

public abstract class SubprocessIpcClientHandler {

    public synchronized final void sendMessage(Object message) {
        SubprocessIpcClientEntryPoint.connection.sendMessage(message);
    }

    public synchronized final void sendByteMessage(int type, byte[] message) {
        try {
            int length = message.length;

            // 1) Write the length.
            // 2) Write the user provided type.
            // 3) Write the message.
            // 4) Finally, write a null, so we can double check the read was good.

            SubprocessIpcClientEntryPoint.nativeErr.write(length);
            SubprocessIpcClientEntryPoint.nativeErr.write(type);
            SubprocessIpcClientEntryPoint.nativeErr.write(message);
            SubprocessIpcClientEntryPoint.nativeErr.write(0);
            SubprocessIpcClientEntryPoint.nativeErr.flush();
        } catch (Exception e) {
            SubprocessIpc.debugError(e);
            System.exit(1);
        }
    }

    public abstract void handleMessage(Object message);

}

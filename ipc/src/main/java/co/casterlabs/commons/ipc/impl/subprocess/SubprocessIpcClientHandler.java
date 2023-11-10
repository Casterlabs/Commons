package co.casterlabs.commons.ipc.impl.subprocess;

public abstract class SubprocessIpcClientHandler extends SubprocessIpcConnection {

    @Override
    protected void close0() {
        System.exit(0);
    }

}

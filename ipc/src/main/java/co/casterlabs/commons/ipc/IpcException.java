package co.casterlabs.commons.ipc;

public class IpcException extends RuntimeException {
    private static final long serialVersionUID = -973629754416327688L;

    public IpcException(String message) {
        super(message);
    }

}

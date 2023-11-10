package co.casterlabs.commons.ipc;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import co.casterlabs.commons.ipc.packets.IpcRemoteInvokePacket;
import co.casterlabs.commons.ipc.packets.IpcResultPacket;

public class IpcObject {
    private static final Map<String, WeakReference<IpcObject>> refs = new HashMap<>();

    private final WeakReference<IpcObject> $ref = new WeakReference<>(this);
    final String $id = UUID.randomUUID().toString();

    /* -------------------- */
    /* Lifecycle            */
    /* -------------------- */

    public IpcObject() {
        refs.put($id, $ref);
    }

    @Override
    protected void finalize() throws Throwable {
        refs.remove($id);
    }

    /* -------------------- */
    /* Remote Invocation    */
    /* -------------------- */

    Object invoke(Class<?> thisClass, String methodName, Object[] args) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<?>[] argClasses = new Class<?>[args.length];

        for (int i = 0; i < args.length; i++) {
            argClasses[i] = args[i].getClass();
        }

        Method methodHandle = _Util.deepMethodSearch(thisClass, methodName, argClasses);

        return methodHandle.invoke(this, args);
    }

    static void process(IpcRemoteInvokePacket remoteInvokePacket, IpcConnection connection) throws IOException {
        try {
            // Lookup the local instance.
            WeakReference<IpcObject> ref = refs.get(remoteInvokePacket.getInstanceId());
            if (ref == null) throw new IpcException("Remote object was already garbage collected.");

            IpcObject local = ref.get();
            if (local == null) {
                refs.remove(remoteInvokePacket.getInstanceId()); // Go ahead and remove the reference.
                throw new IpcException("Remote object was already garbage collected.");
            }

            // Deserialize the args.
            Object[] args = new Object[remoteInvokePacket.getArgs().length];
            for (int i = 0; i < args.length; i++) {
                args[i] = remoteInvokePacket.getArgs()[i].get(connection);
            }

            // Get the result.
            Object result = local.invoke(local.getClass(), remoteInvokePacket.getMethodName(), args);
            _FauxObject faux = new _FauxObject(result);

            // Send the result.
            connection.send(
                new IpcResultPacket(
                    remoteInvokePacket.getWaitingId(),
                    faux,
                    null
                )
            );
        } catch (Throwable t) {
            // Forward any thrown error.
            connection.send(
                new IpcResultPacket(
                    remoteInvokePacket.getWaitingId(),
                    null,
                    _Util.serializeThrowable(t)
                )
            );
        }
    }

}

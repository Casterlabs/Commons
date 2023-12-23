package co.casterlabs.commons.ipc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import co.casterlabs.commons.async.promise.Promise;
import co.casterlabs.commons.async.promise.PromiseResolver;
import co.casterlabs.commons.ipc.packets.IpcRemoteInvokePacket;
import co.casterlabs.commons.ipc.packets.IpcResultPacket;
import lombok.NonNull;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

class _RemoteObjectProxy implements MethodInterceptor {
    private static final Map<String, PromiseResolver<IpcResultPacket>> waiting = new HashMap<>();

    private String remoteInstanceId;
    private IpcConnection connection;

    /* -------------------- */
    /* Life Cycle           */
    /* -------------------- */

    _RemoteObjectProxy(String remoteInstanceId, IpcConnection connection) {
        this.remoteInstanceId = remoteInstanceId;
        this.connection = connection;
    }

    /* -------------------- */
    /* Remote Invocation    */
    /* -------------------- */

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        String waitingId = UUID.randomUUID().toString();
        PromiseResolver<IpcResultPacket> resolver = Promise.withResolvers();

        // Send the packet.
        {
            String methodName = method.getName();

            // Create faux objects for the args.
            _FauxObject[] fauxArgs = new _FauxObject[args.length];
            for (int i = 0; i < args.length; i++) {
                fauxArgs[i] = new _FauxObject(args[i]);
            }

            this.connection.send(new IpcRemoteInvokePacket(waitingId, this.remoteInstanceId, methodName, fauxArgs));
            waiting.put(waitingId, resolver); // Send succeeded, register waiting.
        }

        IpcResultPacket resultPacket = resolver.promise.await();
        waiting.remove(waitingId); // We've received our result, remove the wait.

        if (resultPacket.isSuccess()) {
            _FauxObject faux = resultPacket.getSuccess();

            return faux.get(this.connection);
        } else {
            Throwable t = _Util.deserializeThrowable(resultPacket.getError());
            throw t;
        }
    }

    static void fulfill(IpcResultPacket resultPacket) {
        PromiseResolver<IpcResultPacket> promise = waiting.get(resultPacket.getWaitingId());
        if (promise == null) return; // Discard result.

        promise.resolve(resultPacket);
    }

    /* -------------------- */
    /* Creation             */
    /* -------------------- */

    @SuppressWarnings("unchecked")
    static <T> T getProxy(@NonNull Class<T> clazz, @NonNull String instanceId, @NonNull IpcConnection connection) {
        _RemoteObjectProxy rop = new _RemoteObjectProxy(instanceId, connection);

        Enhancer e = new Enhancer();
        e.setClassLoader(_RemoteObjectProxy.class.getClassLoader());
        e.setSuperclass(clazz);
        e.setCallback(rop);

        return (T) e.create();
    }

}

package co.casterlabs.commons.ipc;

import co.casterlabs.commons.ipc.packets.IpcMessagePacket;
import co.casterlabs.commons.ipc.packets.IpcPacket;
import co.casterlabs.commons.ipc.packets.IpcRemoteInvokePacket;
import co.casterlabs.commons.ipc.packets.IpcResultPacket;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;

public abstract class IpcConnection {

    /* -------------------- */
    /* Message Handling     */
    /* -------------------- */

    public final void sendMessage(Object message) {
        if (message instanceof IpcObject) {
            _FauxObject faux = new _FauxObject(message);
            JsonElement value = Rson.DEFAULT.toJson(faux);

            this.send(new IpcMessagePacket(true, null, value));
        } else {
            String className = message.getClass().getTypeName();
            JsonElement value = Rson.DEFAULT.toJson(message);

            this.send(new IpcMessagePacket(false, className, value));
        }
    }

    /**
     * This is where you handle the messages from {@link #sendMessage(Object)}.
     */
    protected abstract void handleMessage(Object message);

    /* -------------------- */
    /* Packet Handling      */
    /* -------------------- */

    protected abstract void send(IpcPacket packet);

    /**
     * Call this with your deserialized packet, preferrably off of your read thread.
     */
    protected final void receive(IpcPacket packet) {
        try {
            switch (packet.getType()) {
                case REMOTE_INVOKE: {
                    IpcObject.process((IpcRemoteInvokePacket) packet, this);
                    break;
                }

                case RESULT: {
                    _RemoteObjectProxy.fulfill((IpcResultPacket) packet);
                    break;
                }

                case MESSAGE: {
                    IpcMessagePacket messagePacket = (IpcMessagePacket) packet;
                    Object messageObj;

                    if (messagePacket.isIpcObject()) {
                        _FauxObject faux = Rson.DEFAULT.fromJson(messagePacket.getValue(), _FauxObject.class);

                        messageObj = faux.get(this);
                    } else {
                        Class<?> valueClass = Class.forName(messagePacket.getValueClass());
                        messageObj = Rson.DEFAULT.fromJson(messagePacket.getValue(), valueClass);
                    }

                    this.handleMessage(messageObj);
                    break;
                }

            }
        } catch (Throwable t) {
            System.err.println("An error occurred whilst processing packet:");
            t.printStackTrace();
        }
    }

}

package co.casterlabs.commons.ipc.packets;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.AllArgsConstructor;

public abstract class IpcPacket {

    public abstract IpcPacketType getType();

    @JsonSerializationMethod("type")
    private JsonElement $serialize_type() {
        return new JsonString(this.getType().name());
    }

    @AllArgsConstructor
    public static enum IpcPacketType {
        RESULT(IpcResultPacket.class),
        REMOTE_INVOKE(IpcRemoteInvokePacket.class),
        MESSAGE(IpcMessagePacket.class),

        ;

        private Class<? extends IpcPacket> eventClass;

        public static IpcPacket get(JsonObject json) throws Exception {
            String packetType = json.getString("type");

            try {
                // 1) Lookup the event type
                // 2) Use RSON to deserialize to object using the eventClass.
                // 3) Profit!
                IpcPacketType type = IpcPacketType.valueOf(packetType);
                IpcPacket event = Rson.DEFAULT.fromJson(json, type.eventClass);

                return event;
            } catch (IllegalArgumentException e) {
                // 1.1) Lookup failed, so we don't actually have that event.
                // 1.2) Return nothing.
                return null;
            } catch (Exception e) {
                // 2.1) *Something* failed, so we probably don't have that event structured
                // correctly.
                throw e;
            }
        }

    }

}

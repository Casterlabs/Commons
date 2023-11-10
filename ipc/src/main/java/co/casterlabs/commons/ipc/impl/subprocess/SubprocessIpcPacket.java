package co.casterlabs.commons.ipc.impl.subprocess;

import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonString;

abstract class SubprocessIpcPacket {

    public abstract SubprocessIpcPacketType getType();

    @JsonSerializationMethod("type")
    private JsonElement $serialize_type() {
        return new JsonString(this.getType().name());
    }

    public static enum SubprocessIpcPacketType {
        PRINT,
        PING,
        ;
    }

}

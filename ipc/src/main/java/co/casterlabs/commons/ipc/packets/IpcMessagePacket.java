package co.casterlabs.commons.ipc.packets;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class IpcMessagePacket extends IpcPacket {
    private boolean isIpcObject;
    private String valueClass; // Null if isIpcObject.
    private JsonElement value;

    @Override
    public IpcPacketType getType() {
        return IpcPacketType.MESSAGE;
    }

}

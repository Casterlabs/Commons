package co.casterlabs.commons.ipc.packets;

import co.casterlabs.commons.ipc._FauxObject;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class IpcResultPacket extends IpcPacket {
    private String waitingId;
    private _FauxObject success;
    private String error;

    public boolean isSuccess() {
        return this.error == null;
    }

    @Override
    public IpcPacketType getType() {
        return IpcPacketType.RESULT;
    }

}

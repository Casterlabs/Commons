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
public class IpcRemoteInvokePacket extends IpcPacket {
    private String waitingId;
    private String instanceId;
    private String methodName;
    private _FauxObject[] args;

    @Override
    public IpcPacketType getType() {
        return IpcPacketType.REMOTE_INVOKE;
    }

}

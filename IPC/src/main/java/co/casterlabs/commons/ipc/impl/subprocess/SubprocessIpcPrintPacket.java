package co.casterlabs.commons.ipc.impl.subprocess;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonClass(exposeAll = true)
class SubprocessIpcPrintPacket extends SubprocessIpcPacket {
    private String bytes;
    private PrintChannel channel;

    @Override
    public SubprocessIpcPacketType getType() {
        return SubprocessIpcPacketType.PRINT;
    }

    public static enum PrintChannel {
        STDOUT,
        STDERR;

    }

}

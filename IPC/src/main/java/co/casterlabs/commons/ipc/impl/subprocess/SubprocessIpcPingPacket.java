package co.casterlabs.commons.ipc.impl.subprocess;

import lombok.NoArgsConstructor;

@NoArgsConstructor
class SubprocessIpcPingPacket extends SubprocessIpcPacket {
    static final SubprocessIpcPingPacket INSTANCE = new SubprocessIpcPingPacket();

    @Override
    public SubprocessIpcPacketType getType() {
        return SubprocessIpcPacketType.PING;
    }

}

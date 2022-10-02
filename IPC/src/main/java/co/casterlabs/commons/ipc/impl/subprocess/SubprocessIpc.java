package co.casterlabs.commons.ipc.impl.subprocess;

import java.util.concurrent.TimeUnit;

class SubprocessIpc {
    static final long PING_INTERVAL = TimeUnit.SECONDS.toMillis(1);
    static final long PING_TIMEOUT = PING_INTERVAL * 2;

}

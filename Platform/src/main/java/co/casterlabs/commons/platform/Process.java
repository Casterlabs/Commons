package co.casterlabs.commons.platform;

import java.io.IOException;

import lombok.NonNull;

public class Process {

    public static void kill(@NonNull String pid) throws IOException {
        switch (Platform.osFamily) {
            case DOS:
            case WINDOWS:
                Runtime.getRuntime().exec("taskkill /F /PID " + pid);
                return;

            case UNIX:
                Runtime.getRuntime().exec("kill -9 " + pid);
                return;

            case VMS:
                Runtime.getRuntime().exec("stop process /ID=" + pid);
                return;

            default:
                throw new RuntimeException("Unsupported osFamily: " + Platform.osFamily);
        }
    }

}

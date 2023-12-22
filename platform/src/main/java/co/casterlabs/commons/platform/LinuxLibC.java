package co.casterlabs.commons.platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * This class allows you to detect whether or not a machine uses GNU or MUSL
 * Libc.
 */
public class LinuxLibC {

    /**
     * If this returns true then you know that this OS supports GNU LibC. It may
     * also support MUSL or other standards.
     */
    public static boolean isGNU() throws IOException {
        if (Platform.osDistribution != OSDistribution.LINUX) {
            throw new IllegalStateException("LinuxLibC is only supported on Linux.");
        }

        java.lang.Process unameProc = Runtime.getRuntime().exec("uname -o");
        String unameResult = _PlatformUtil.readInputStreamString(unameProc.getInputStream(), StandardCharsets.UTF_8);

        return unameResult.contains("GNU/");
    }

}

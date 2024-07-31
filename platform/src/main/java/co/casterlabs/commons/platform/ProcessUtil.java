package co.casterlabs.commons.platform;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;

import lombok.NonNull;

public class ProcessUtil {

    /**
     * Tries to kill the given process.
     * 
     * @param  pid         The PID to kill.
     * 
     * @throws IOException If an I/O error occurs.
     */
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
                throw new UnsupportedOperationException("Unsupported osFamily: " + Platform.osFamily);
        }
    }

    /**
     * Tries to get the PID of the current running process.
     * 
     * @return   the pid as a string.
     * 
     * @implNote On Java 9+ VMs this returns the value of
     *           {@code ProcesHandle.current().pid();}, on Java 8 it returns a janky
     *           value that may not work on some VMs, YMMV.
     * 
     * @implNote This value is not guaranteed.
     */
    public static String getPid() {
        try {
            Class<?> c_ProcessHandle = Class.forName("java.lang.ProcessHandle");
            Object handle = c_ProcessHandle.getMethod("current").invoke(null);

            return String.valueOf((long) c_ProcessHandle.getMethod("pid").invoke(handle));
        } catch (Exception e) {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            return name.substring(0, name.indexOf('@'));
        }
    }

    /**
     * 
     * @return                                 The commandline used to execute the
     *                                         given process, can be used with
     *                                         {@link Runtime#exec(String)} to spawn
     *                                         a second process easily.
     * 
     * @implNote                               Windows editions < Win2000 will need
     *                                         WMI installed as the WMIC command is
     *                                         used to obtain process information.
     * 
     * @throws   IOException                   if an I/O error occurs.
     * @throws   UnsupportedOperationException if either ProcessHandle isn't
     *                                         supported or if the osFamily is not
     *                                         one of UNIX or WINDOWS.
     */
    public static String tryGetCommandLine(@NonNull String pid) throws IOException {
        switch (Platform.osFamily) {
            case UNIX: {
                Process proc = new ProcessBuilder()
                    .command("ps", "-p", String.valueOf(pid), "-o", "args")
                    .start();

                // "COMMAND\n...."
                String content = _PlatformUtil.readInputStreamString(proc.getInputStream(), StandardCharsets.UTF_8);

                return content.substring(content.indexOf('D') + 1).trim();
            }

            case WINDOWS: {
                try {
                    Process proc = new ProcessBuilder()
                        .command(
                            System.getenv("WINDIR") + "\\System32\\WindowsPowerShell\\v1.0\\powershell.exe",
                            "-NoProfile",
                            "(Get-CimInstance Win32_Process -Filter \"ProcessId=" + pid + "\").CommandLine"
                        )
                        .start();

                    String content = _PlatformUtil.readInputStreamString(proc.getInputStream(), StandardCharsets.UTF_8);
                    if (proc.exitValue() != 0) {
                        throw new IOException("Unknown command: " + content);
                    }

                    return content.trim();
                } catch (IOException ignored) {
                    // Fallback on the deprecated.
                    Process proc = new ProcessBuilder()
                        .command("wmic", "process", "where", "processid=" + pid, "get", "commandline", "/format:list")
                        .start();

                    // "CommandLine=...."
                    String content = _PlatformUtil.readInputStreamString(proc.getInputStream(), StandardCharsets.UTF_8);
                    return content.substring(content.indexOf('=') + 1).trim();
                }
            }

            default:
                throw new UnsupportedOperationException();
        }
    }

}

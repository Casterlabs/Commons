/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.platform;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import lombok.NonNull;

public class Platform {

    /* ---------------- */
    /* CPU Architecture */
    /* ---------------- */

    /**
     * Whether or not the current machine's endianess is big endian.
     * 
     * @implNote This just calls {@link ByteOrder#nativeOrder()}.
     */
    public static final boolean isBigEndian = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    /**
     * The processor's word size/bitness, or -1 if unknown. Usually 32 or 64.
     * 
     * @implNote Some IBM Z mainframes will return 32 even though their words are 31
     *           bits long.
     */
    public static final int wordSize = _PlatformUtil.getWordSize();

    /** The CPU Architecture of the host, e.g x86 or arm. */
    public static final ArchFamily archFamily = ArchFamily.get();

    /* ---------------- */
    /* Operating System */
    /* ---------------- */

    /** The family of the host's OS, e.g macOS or Windows NT */
    public static final OSFamily osFamily = OSFamily.get();

    /** The family distribution of the host's OS, e.g Unix or Windows */
    public static final OSDistribution osDistribution = OSDistribution.get(osFamily);

    /* ---------------- */
    /* Helpers          */
    /* ---------------- */

    /**
     * A convenience method for generating file names for OS-specific library files.
     * 
     * @param   libraryName The name of the library (e.g "WebView")
     * 
     * @return              the formatted string (e.g "libwebview.so" or
     *                      "WebView.dll")
     * 
     * @apiNote             &bull; This returns "*.dylib" on macOS, since that's the
     *                      more common format; Be aware that macOS supports both
     *                      .so and .dylib extensions for libraries.
     * 
     * @apiNote             &bull; This returns "*.dll" on Windows, since that's the
     *                      more common format; Be aware that Windows supports both
     *                      .exe and .dll extensions for libraries.
     */
    public static String formatLibrary(@NonNull String libraryName) {
        switch (osDistribution) {
            // DOS
            case MS_DOS:
                return String.format("%s.exe", libraryName).toUpperCase();

            // Windows
            case WINDOWS_9X:
            case WINDOWS_NT:
                return String.format("%s.dll", libraryName);

            // Unix
            case MACOS:
                return String.format("%s.dylib", libraryName).toLowerCase();

            case BSD:
            case SOLARIS:
            case LINUX:
                return String.format("%s.so", libraryName).toLowerCase();

            // VMS
            case OPEN_VMS:
                return String.format("%s.exe", libraryName).toUpperCase();

            case GENERIC:
                break;

            // Don't create a `default:` entry.
            // We want the compiler to warn us about missed values.

        }

        return libraryName;
    }

    /**
     * Tries to get the PID of the current running process.
     * 
     * @return   a long pointer of the pid.
     * 
     * @implNote On Java 9+ VMs this returns the value of
     *           {@code ProcesHandle.current().pid();}, on Java 8 it returns a janky
     *           value that may not work on some VMs, YMMV.
     * 
     * @implNote This value is not guaranteed.
     */
    public static long getPid() {
        try {
            Class<?> c_ProcessHandle = Class.forName("java.lang.ProcessHandle");
            Object handle = c_ProcessHandle.getMethod("current").invoke(null);

            return (long) c_ProcessHandle.getMethod("pid").invoke(handle);
        } catch (Exception e) {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            return Long.parseLong(
                name.substring(0, name.indexOf('@'))
            );
        }
    }

    /**
     * 
     * @return                                 The commandline used to execute this
     *                                         exact process, can be used with
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
    public static String tryGetCommandLine() throws IOException {
        switch (osFamily) {
            case UNIX: {
                long pid = getPid();
                Process proc = new ProcessBuilder()
                    .command("ps", "-p", String.valueOf(pid), "-o", "args")
                    .start();

                // "COMMAND\n...."
                String content = _PlatformUtil.readInputStreamString(proc.getInputStream(), StandardCharsets.UTF_8);

                return content.substring(content.indexOf('D') + 1).trim();
            }

            case WINDOWS: {
                long pid = getPid();

                try {
                    Process proc = new ProcessBuilder()
                        .command(
                            System.getenv("WINDIR") + "\\System32\\WindowsPowerShell\\v1.0\\powershell.exe",
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

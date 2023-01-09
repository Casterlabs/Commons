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

    /** The CPU Architecture of the host, e.g x86 or arm. */
    public static final ArchFamily archFamily = ArchFamily.get();

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
    public static final int wordSize = _getWordSize();

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
                Process proc = new ProcessBuilder()
                    .command("wmic", "process", "where", "processid=" + pid, "get", "commandline", "/format:list")
                    .start();

                // "CommandLine=...."
                String content = _PlatformUtil.readInputStreamString(proc.getInputStream(), StandardCharsets.UTF_8);

                return content.substring(content.indexOf('=') + 1).trim();
            }

            default:
                throw new UnsupportedOperationException();
        }
    }

    /* ---------------- */
    /* Static helpers   */
    /* ---------------- */

    private static int _getWordSize() {
        // Sources:
        // https://www.oracle.com/java/technologies/hotspotfaq.html#64bit_detection:~:text=When%20writing%20Java%20code%2C%20how%20do%20I%20distinguish%20between%2032%20and%2064%2Dbit%20operation%3F
        // https://stackoverflow.com/a/808314
        // https://www.ibm.com/docs/en/sdk-java-technology/8?topic=dja-determining-whether-your-application-is-running-32-bit-31-bit-z-64-bit-jvm

        String SADM = System.getProperty("sun.arch.data.model");
        if ((SADM != null) && !SADM.equals("unknown")) {
            return Integer.parseInt(SADM);
        }

        String CIVBM = System.getProperty("com.ibm.vm.bitmode");
        if (CIVBM != null) {
            return Integer.parseInt(CIVBM);
        }

        String vmName = System.getProperty("java.vm.name");
        if (vmName.contains("64-bit")) {
            return 64;
        } else if (vmName.contains("32-bit")) {
            return 32;
        }

        return -1;
    }

}

/* 
Copyright 2023 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.platform;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import lombok.NonNull;
import lombok.SneakyThrows;

public class FileLock {

    /**
     * Locks the given file.
     * 
     * @return   a Closeable, so you can release the lock.
     * 
     * @implNote This may block for an arbitrary amount of time IF another process
     *           already owns an exclusive lock on said file.
     */
    public static Closeable create(@NonNull File file) throws IOException {
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ);
        java.nio.channels.FileLock lock = channel.lock(0, Long.MAX_VALUE, true);

        return () -> {
            lock.close();
            channel.close();
        };
    }

    /**
     * Inspects the given file and returns a list of PIDs that are locking said
     * file.
     * 
     * @return a list of PIDs.
     */
    public static String[] inspect(@NonNull File file) {
        switch (Platform.osFamily) {
            case WINDOWS:
                if (!windows_handleExe.exists()) {
                    windows_loadHandle();
                }

                return windows_inspect(file);

            case UNIX:
                return unix_inspect(file);

            default:
                throw new RuntimeException("Unsupported osFamily: " + Platform.osFamily);
        }
    }

    /* ---------------- */
    /* Unix             */
    /* ---------------- */

    @SneakyThrows
    private static String[] unix_inspect(File file) {
        java.lang.Process lsofProc = Runtime.getRuntime().exec(String.format("lsof \"%s\"", file.getAbsolutePath()));
        String lsofResult = _PlatformUtil.readInputStreamString(lsofProc.getInputStream(), StandardCharsets.UTF_8);

        if (!lsofResult.contains("COMMAND")) {
            return new String[0];
        }

        // Here's some nasty code...
        String[] lines = lsofResult.split("\n");
        String[][] csv = new String[lines.length][]; // note: line 0 is always useless.

        for (int idx = 0; idx < lines.length; idx++) {
            csv[idx] = lines[idx].split(" ");
        }

        // "COMMAND PID USER FD TYPE DEVICE SIZE/OFF NODE NAME"

        String[] pids = new String[lines.length - 1];
        for (int idx = 1; idx < csv.length; idx++) {
            pids[idx - 1] = csv[idx][1].trim();
        }

        return pids;
    }

    /* ---------------- */
    /* Windows          */
    /* ---------------- */
    private static File windows_handleExe;

    static {
        if (Platform.osFamily == OSFamily.WINDOWS) {
            windows_handleExe = new File(System.getProperty("java.io.tmpdir"), String.format("%s_handle.exe", UUID.randomUUID()));
            windows_handleExe.deleteOnExit();
        }
    }

    @SneakyThrows
    private static void windows_loadHandle() {
        InputStream handle = FileLock.class.getResourceAsStream(String.format("/co/casterlabs/platform/lib/handle_%s.exe", Platform.archTarget));
        if (handle == null) throw new RuntimeException("Could not load native handle.exe for arch: " + Platform.archTarget);
        Files.copy(handle, windows_handleExe.toPath());
    }

    @SneakyThrows
    private static String[] windows_inspect(File file) {
        java.lang.Process handleProc = Runtime.getRuntime().exec(String.format("%s -accepteula -nobanner -v -u \"%s\"", windows_handleExe.getAbsolutePath(), file.getAbsolutePath()));
        String handleResult = _PlatformUtil.readInputStreamString(handleProc.getInputStream(), StandardCharsets.UTF_8);

        if (handleResult.contains("No matching handles found")) {
            return new String[0];
        }

        // Here's some nasty code...
        String[] lines = handleResult.split("\n");
        String[][] csv = new String[lines.length][]; // note: line 0 is always useless.

        for (int idx = 0; idx < lines.length; idx++) {
            csv[idx] = lines[idx].split(",");
        }

        // "Process,PID,User,Handle,Type,Share Flags,Name,Access"

        String[] pids = new String[lines.length - 1];
        for (int idx = 1; idx < csv.length; idx++) {
            pids[idx - 1] = csv[idx][1];
        }

        return pids;
    }

}

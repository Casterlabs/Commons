/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.platform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ArchExtensions {
    // @formatter:off

    // x86 Family 
    X86_FPU(ArchFamily.X86, "fpu"),

    ;
    // @formatter:on

    private ArchFamily family;
    private String name;

    static List<ArchExtensions> get() {
        try {
            switch (Platform.archFamily) {
                case X86:
                    return X86.get();

                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    /**
     * @return a "standardized" name of the architecture (e.g "armhf").
     */
    @Override
    public String toString() {
        return this.name;
    }

    static byte[] inputStreamToBytes(InputStream in) throws IOException {
        ByteArrayOutputStream dest = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int read = 0;
        while ((read = in.read(buffer)) != -1) {
            dest.write(buffer, 0, read);
        }

        return dest.toByteArray();
    }

}

class X86 {
    // https://git.kernel.org/pub/scm/linux/kernel/git/stable/linux.git/tree/arch/x86/include/asm/cpufeatures.h
    // https://en.wikipedia.org/wiki/CPUID#EAX=0:_Highest_Function_Parameter_and_Manufacturer_ID:~:text=sake%20of%20compatibility.-,Feature%20Information,-Bit

    static List<ArchExtensions> get() throws IOException {
        switch (Platform.osFamily) {
            case WINDOWS:
                return windows();

            default:
                return Collections.emptyList();
        }
    }

    /* ---------------- */
    /* Windows          */
    /* ---------------- */

    // https://learn.microsoft.com/en-us/windows/win32/cimwin32prov/win32-processor#:~:text=from%20CIM_LogicalDevice.-,ProcessorId,-Data%20type%3A

    private static long windows_cpuid() throws IOException {
        Process proc = new ProcessBuilder()
            .command("wmic", "cpu", "get", "ProcessorID", "/format:list")
            .start();

        byte[] bytes = ArchExtensions.inputStreamToBytes(proc.getInputStream());
        String content = new String(bytes).trim(); // "ProcessorId=...."

        String id = content.split("=")[1];
        return Long.parseUnsignedLong(id, 16);
    }

    private static List<ArchExtensions> windows() throws IOException {
        long cpuid = windows_cpuid(); // 1011111111101011111110111111111100000000000000010000011011100101
        int edx = (int) (cpuid >> 32);
        int ecx = (int) cpuid;

        System.out.println(Long.toUnsignedString(cpuid, 2));
        System.out.println(String.format("%1$32s", Integer.toUnsignedString(edx, 2)).replace(' ', '0'));
        System.out.println(String.format("%1$32s", Integer.toUnsignedString(ecx, 2)).replace(' ', '0'));

        System.out.printf("ia64:  %b\n", (edx & 1 << 30) != 0);
        System.out.printf("ssse3: %b\n", (ecx & 1 << 9) != 0);

        return null;
    }

    /* ---------------- */
    /* Linux            */
    /* ---------------- */

    // TODO Linux: cat /proc/cpuinfo | grep "flags" | head -1

    /* ---------------- */
    /* macOS            */
    /* ---------------- */

    // TODO macOS: sysctl -a | grep machdep.cpu.features

}

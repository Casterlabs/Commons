/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.platform;

import java.nio.ByteOrder;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Arch {
    // @formatter:off
	
    // x86/64 family.
    X86_64    ("x86_64",  64, "amd64|x86_64"),
    IA64      ("ia64",    64, "ia64|itanium64"),
    X86       ("x86",     32, "x86|i[0-9]86|ia32"),
    
    // Arm family.
    AARCH64   ("aarch64", 64, "arm64|aarch64"), // Apple M1 chip series.
    ARM       ("arm",     32, "arm"),

    // IBM's PowerPC architecture.
    PPC64     ("ppc64",   64, "ppc64"),
    PPC       ("ppc",     32, "ppc|power"),
    
    // Miscellaneous.
    // We mostly lump these together because of their similarities.
    RISC      ("risc",    -1, "risc|mips|alpha|sparc"), 
    
 ;
    // @formatter:on

    /**
     * A standardized name for the architecture (e.g "x86_64" or "aarch64").
     */
    private String name;
    private @Getter int wordSize;
    private String regex;

    static Arch get() {
        String osArch = System.getProperty("os.arch", "<blank>").toLowerCase();

        // Search the enums for a match, returning it.
        for (Arch arch : values()) {
            if (Pattern.compile(arch.regex).matcher(osArch).find()) {
                return arch;
            }
        }

        // Couldn't find a match.
        throw new UnsupportedOperationException("Unknown cpu arch: " + osArch);
    }

    /**
     * @return the standardized name of the architecture
     */
    @Override
    public String toString() {
        return this.name;
    }

    /**
     * @return whether or not the current machine's endianess is big endian.
     * 
     * @implNote This just calls {@link ByteOrder#nativeOrder()}.
     */
    public static boolean isBigEndian() {
        return ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
    }

}

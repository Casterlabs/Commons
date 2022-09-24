/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.platform;

import java.util.regex.Pattern;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Arch {
    // @formatter:off
	
    // x86/64 family.
    AMD64     ("amd64",    "amd64|x86_64"),
    IA64      ("ia64",     "ia64"),
    X86       ("x86",      "x86|i[0-9]86"),
    
    // Arm family.
    AARCH64   ("aarch64",  "arm64|aarch64"), // The Apple M1 chip series is aarch64.
    ARM32     ("arm32",    "arm"),

    // IBM's PowerPC architecture.
    POWERPC64 ("ppc64",    "ppc64"),
    POWERPC   ("ppc",      "ppc|power"),
    
    // Miscellaneous.
    // We mostly lump these together because of their similarities.
    RISC      ("risc",     "risc|mips|alpha|sparc"), 
    
 ;
    // @formatter:on

    private String str;
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
     * Returns a standardized string, such as amd64 or aarch64.
     *
     * @return a standardized string
     */
    @Override
    public String toString() {
        return this.str;
    }

}

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
public enum ArchFamily {
    // @formatter:off
    X86   ("x86",   "x86|i[0-9]86|ia32|amd64|ia64|itanium64"),
    ARM   ("arm",   "arm|aarch"),
    PPC   ("ppc",   "ppc|power"),
    SPARC ("sparc", "sparc"),
    MIPS  ("mips",  "mips"),
    S390  ("s390",  "s390"),
    RISCV ("riscv", "riscv"),
    ;
    // @formatter:on

    private String name;
    private String regex;

    static ArchFamily get() {
        String osArch = System.getProperty("os.arch", "<blank>").toLowerCase();

        // Search the enums for a match, returning it.
        for (ArchFamily arch : values()) {
            if (Pattern.compile(arch.regex).matcher(osArch).find()) {
                return arch;
            }
        }

        // Couldn't find a match.
        throw new UnsupportedOperationException("Unknown cpu arch: " + osArch);
    }

    /**
     * @return the standardized name of the architecture (e.g "x86" or "arm").
     */
    @Override
    public String toString() {
        return this.name;
    }

}

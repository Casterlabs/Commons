/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.platform;

import static co.casterlabs.commons.platform.OSFamily.DOS;
import static co.casterlabs.commons.platform.OSFamily.UNIX;
import static co.casterlabs.commons.platform.OSFamily.VMS;
import static co.casterlabs.commons.platform.OSFamily.WINDOWS;

import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum OSDistribution {
    // @formatter:off

	// DOS
	MS_DOS     (DOS,     "MS-DOS",      "<manually detected>"),
	
	// Windows
    WINDOWS_9X (WINDOWS, "Windows 9x",  "windows (95|98|me|ce)"),
    WINDOWS_NT (WINDOWS, "Windows NT",  "win"),

	// Unix
    MACOSX     (UNIX,    "macOS",       "mac|darwin"),
    SOLARIS    (UNIX,    "Solaris",     "sun|solaris"),
    BSD        (UNIX,    "BSD",         "bsd"),
    LINUX      (UNIX,    "Linux",       "nux"),

    // VMS
	OPEN_VMS   (VMS,     "OpenVMS",     "vms"),
	

    // This is our fallback, it'll get matched last.
    GENERIC    (null,    "Generic",     ""),
    
    ;
    // @formatter:on

    @Getter
    private OSFamily family;

    private String str;
    private String regex;

    static OSDistribution get(OSFamily family) {
        if ((family == OSFamily.DOS) && System.getProperty("path.separator", "").equals(";")) {
            return MS_DOS;
        }

        String osName = System.getProperty("os.name", "<blank>").toLowerCase();

        for (OSDistribution e : values()) {
            if (e.family != family)
                continue;

            if (Pattern.compile(e.regex).matcher(osName).find()) {
                return e;
            }
        }

        return GENERIC;
    }

    /**
     * Returns a friendly name for the distribution, such as macOS or Windows NT.
     *
     * @return the friendly name of the distribution
     */
    @Override
    public String toString() {
        return this.str;
    }

}

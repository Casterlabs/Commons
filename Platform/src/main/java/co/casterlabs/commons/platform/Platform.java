/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.platform;

import lombok.NonNull;

public class Platform {

    /** The CPU Architecture of the host, e.g amd64 or aarch64. */
    public static final Arch arch = Arch.get();

    /** The family of the host's OS, e.g macOS or Windows NT */
    public static final OSFamily osFamily = OSFamily.get();

    /** The family distribution of the host's OS, e.g Unix or Windows */
    public static final OSDistribution osDistribution = OSDistribution.get(osFamily);

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
            case MACOSX:
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

}

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

	private @Getter OSFamily family;
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

	@Override
	public String toString() {
		return this.str;
	}

}

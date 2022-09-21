package co.casterlabs.commons.platform;

import java.util.regex.Pattern;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum OSFamily {
	// @formatter:off
    UNIX     ("Unix",    "nux|bsd|.ix|sun|solaris|hp-ux"),
    WINDOWS  ("Windows", "win"),
    DOS      ("DOS",     "dos"),
    VMS      ("VMS",     "vms"),

    GENERIC  ("Generic", ""),
    
    ;
    // @formatter:on

	private String str;
	private String regex;

	static OSFamily get() {
		String osName = System.getProperty("os.name", "<blank>").toLowerCase();

		for (OSFamily e : values()) {
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
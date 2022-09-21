package co.casterlabs.commons.platform;

import java.util.regex.Pattern;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Arch {
	// @formatter:off
	
    AMD64     ("amd64",    "amd64|x86_64"),
    IA64      ("ia64",     "ia64"),
    X86       ("x86",      "x86|i[0-9]86"),
    
    AARCH64   ("aarch64",  "arm64|aarch64"),
    ARM32     ("arm32",    "arm"),

    POWERPC64 ("ppc64",    "ppc64"),
    POWERPC   ("ppc",      "ppc|power"),
    
    RISC      ("risc",     "risc|mips|alpha|sparc"), // We lump these together because of their similarities.
    
    ;
    // @formatter:on

	private String str;
	private String regex;

	static Arch get() {
		String osArch = System.getProperty("os.arch", "<blank>").toLowerCase();

		for (Arch arch : values()) {
			if (Pattern.compile(arch.regex).matcher(osArch).find()) {
				return arch;
			}
		}

		throw new UnsupportedOperationException("Unknown cpu arch: " + osArch);
	}

	@Override
	public String toString() {
		return this.str;
	}

}
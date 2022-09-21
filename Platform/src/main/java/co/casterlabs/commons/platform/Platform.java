package co.casterlabs.commons.platform;

public class Platform {
	public static final Arch arch = Arch.get();

	public static final OSFamily osFamily = OSFamily.get();
	public static final OSDistribution osDistribution = OSDistribution.get(osFamily);

}
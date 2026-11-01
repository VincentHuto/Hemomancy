package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

/** Ending permissions; callers still enforce purity, knowledge, degree and payment. */
public final class HarbingerPathPermissions {
	private HarbingerPathPermissions() {}

	/** A Silentium: the eighth degree reached by drawing in Vesper's memory. */
	public static boolean isSilentium(int degree, EnumArchonPath path) {
		return degree >= 8 && path == EnumArchonPath.SILENTIUM;
	}

	/** An Apotheos: the eighth degree reached through the Rite of Apotheosis. */
	public static boolean isApotheos(int degree, EnumArchonPath path) {
		return degree >= 8 && path == EnumArchonPath.APOTHEOS;
	}

	/**
	 * Whether the hivemind still reaches this player: its whispers and Ancestral Communion. A Silentium cut the antenna
	 * that tied them to the hive, and hearing nothing from it is what makes one. Our Lady is not the hive.
	 */
	public static boolean hearsTheHive(int degree, EnumArchonPath path) {
		return !isSilentium(degree, path);
	}

	/** The lang-key suffix the hive's voice uses to address this player: ".apotheos" for an Apotheos, else none (Archon). */
	public static String hiveAddressSuffix(int degree, EnumArchonPath path) {
		return isApotheos(degree, path) ? ".apotheos" : "";
	}

	/** The degree a teacher's greeting tree answers: their eighth-degree tree is the Apotheos's; a Silentium hears the Archon's. */
	public static int greetingDegree(int degree, EnumArchonPath path) {
		return degree >= 8 && path != EnumArchonPath.APOTHEOS ? 7 : degree;
	}

	public static boolean isChoiceUnresolved(int pomes, EnumArchonPath path) {
		return pomes >= 9 && path != EnumArchonPath.APOTHEOS && path != EnumArchonPath.SILENTIUM;
	}

	public static boolean canUsePower(int pomes, EnumArchonPath path, boolean staffRecovery) {
		return staffRecovery || !isChoiceUnresolved(pomes, path);
	}

	public static boolean canPerformRite(int pomes, EnumArchonPath path, boolean apotheos) {
		return apotheos ? path == EnumArchonPath.APOTHEOS_PENDING : !isChoiceUnresolved(pomes, path);
	}
}

package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

/** Ending permissions; callers still enforce purity, knowledge, degree and payment. */
public final class HarbingerPathPermissions {
	private HarbingerPathPermissions() {}

	public static boolean isChoiceUnresolved(int pomes, EnumArchonPath path) {
		return pomes >= 9 && path != EnumArchonPath.APOTHEOS && path != EnumArchonPath.SILENT_ARCHON;
	}

	public static boolean canUsePower(int pomes, EnumArchonPath path, boolean staffRecovery) {
		return staffRecovery || !isChoiceUnresolved(pomes, path);
	}

	public static boolean canPerformRite(int pomes, EnumArchonPath path, boolean apotheos) {
		return apotheos ? path == EnumArchonPath.APOTHEOS_PENDING : !isChoiceUnresolved(pomes, path);
	}
}

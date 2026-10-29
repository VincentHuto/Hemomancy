package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

/** Who may draw Vesper's memory in and so become Silentium. */
public final class SilentiumAscensionRules {
	private SilentiumAscensionRules() {}

	/** A degree-7 Silence still pending, whose own refusal broke Vesper. */
	public static boolean canAbsorbVesperMemory(int degree, EnumArchonPath path, boolean vesperDefeated) {
		return degree == 7 && path == EnumArchonPath.SILENT_PENDING && vesperDefeated;
	}

	/** The memory is the only way to Silentium, so one that was lost comes back to the Silence that earned it, once at a time. */
	public static boolean shouldReissueMemory(int degree, EnumArchonPath path, boolean vesperDefeated,
			boolean memoryAwakened, boolean memoryCarried, boolean memoryPending) {
		return canAbsorbVesperMemory(degree, path, vesperDefeated) && !memoryAwakened && !memoryCarried
				&& !memoryPending;
	}
}

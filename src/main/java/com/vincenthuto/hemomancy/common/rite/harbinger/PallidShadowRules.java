package com.vincenthuto.hemomancy.common.rite.harbinger;

/** Safety and PvP gates for the hostile Rite of the Pallid Shadow. */
public final class PallidShadowRules {
	private PallidShadowRules() {
	}

	public static boolean canTarget(boolean pvpAllowed, boolean creative,
			boolean spectator, boolean allied, boolean hasUnstainedProgress) {
		return pvpAllowed && !creative && !spectator && !allied && hasUnstainedProgress;
	}
}

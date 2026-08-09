package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import java.util.UUID;

final class VesperAbsorptionEligibilityRules {
	private VesperAbsorptionEligibilityRules() {
	}

	static boolean canAbsorb(boolean awaitingAbsorption, int downedTicks, boolean ordealResolved,
			UUID ordealOwner, UUID player) {
		return awaitingAbsorption && VesperCombatRules.isDefeatAnimationComplete(downedTicks) && !ordealResolved
				&& (ordealOwner == null || ordealOwner.equals(player));
	}
}

package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;

public final class ScarNoeticRoutingRules {
	private ScarNoeticRoutingRules() {
	}

	public static boolean qualifies(String manipulationName) {
		return manipulationName != null && !manipulationName.isBlank()
				&& !ManipulationEquipHelper.isFixedMechanicalManip(manipulationName);
	}

	public static int bestMatchingTier(EnumBloodTendency tendency, Iterable<ScarDefinition> activeScars) {
		int best = 0;
		for (ScarDefinition scar : activeScars) {
			if (scar.getScarType() == ScarType.CEREBRAL && scar.getAssignedTendency() == tendency) {
				best = Math.max(best, scar.getTier());
			}
		}
		return best;
	}

	public static float adjustedStrain(float strain, int bestMatchingTier) {
		int tier = Math.max(0, Math.min(3, bestMatchingTier));
		return strain * (1F - tier * 0.05F);
	}
}

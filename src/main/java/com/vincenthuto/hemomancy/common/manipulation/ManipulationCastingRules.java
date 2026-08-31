package com.vincenthuto.hemomancy.common.manipulation;

import net.minecraft.util.Mth;

public final class ManipulationCastingRules {
	private ManipulationCastingRules() {
	}

	public static float chargeFraction(float chargeTicks, int requiredChargeTicks) {
		return requiredChargeTicks <= 0 ? 0.0F : Mth.clamp(chargeTicks / requiredChargeTicks, 0.0F, 1.0F);
	}

	public static double chargedCost(double modifiedCost, float chargeTicks, int requiredChargeTicks) {
		return modifiedCost * chargeFraction(chargeTicks, requiredChargeTicks);
	}
}

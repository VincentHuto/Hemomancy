package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.TemperWeaponHelper;

public final class VesperTemperDefenseRules {
	public static final float MATCHING_TEMPER_MULTIPLIER = 0.5F;

	private VesperTemperDefenseRules() {
	}

	public static float damageMultiplier(EnumBloodTemper activeTemper,
			EnumBloodTemper attackingTemper, float opposingAffinityMultiplier) {
		if (activeTemper == attackingTemper) {
			return MATCHING_TEMPER_MULTIPLIER;
		}
		if (TemperWeaponHelper.isOpposingTemper(attackingTemper, activeTemper)) {
			return opposingAffinityMultiplier;
		}
		return 1.0F;
	}
}

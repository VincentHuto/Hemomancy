package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.TendencyWeaponHelper;

public final class VesperTendencyDefenseRules {
	public static final float MATCHING_TENDENCY_MULTIPLIER = 0.5F;

	private VesperTendencyDefenseRules() {
	}

	public static float damageMultiplier(EnumBloodTendency activeTendency,
			EnumBloodTendency attackingTendency, float opposingAffinityMultiplier) {
		if (activeTendency == attackingTendency) {
			return MATCHING_TENDENCY_MULTIPLIER;
		}
		if (TendencyWeaponHelper.isOpposingTendency(attackingTendency, activeTendency)) {
			return opposingAffinityMultiplier;
		}
		return 1.0F;
	}
}

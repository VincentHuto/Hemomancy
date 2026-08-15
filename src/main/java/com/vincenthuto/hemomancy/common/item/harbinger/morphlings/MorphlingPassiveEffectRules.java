package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import net.minecraft.world.effect.MobEffectInstance;

public final class MorphlingPassiveEffectRules {
	private MorphlingPassiveEffectRules() {
	}

	public static int effectDurationTicks(int refreshIntervalTicks) {
		return MobEffectInstance.INFINITE_DURATION;
	}

	public static boolean shouldRefresh(int durationTicks, int currentAmplifier, int desiredAmplifier,
			int refreshIntervalTicks) {
		return currentAmplifier != desiredAmplifier
				|| durationTicks != MobEffectInstance.INFINITE_DURATION;
	}
}

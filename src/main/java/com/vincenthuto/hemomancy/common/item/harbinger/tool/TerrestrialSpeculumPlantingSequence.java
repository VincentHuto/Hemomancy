package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRitePlantingSequence;
import net.minecraft.util.Mth;

/** Timing shared by the Speculum's planting action and the Living Staff planting pose. */
public final class TerrestrialSpeculumPlantingSequence {
	private TerrestrialSpeculumPlantingSequence() {
	}

	public static boolean shouldManifest(long startedAt, long gameTime) {
		return gameTime - startedAt >= CardinalRitePlantingSequence.IMPACT_TICK;
	}

	public static boolean isActive(long startedAt, long gameTime) {
		long elapsed = gameTime - startedAt;
		return elapsed >= 0L && elapsed < CardinalRitePlantingSequence.DURATION_TICKS;
	}

	public static float extractionLift(float recoveryProgress) {
		float recovery = Mth.clamp(recoveryProgress, 0.0F, 1.0F);
		return recovery * recovery * (3.0F - 2.0F * recovery) * 0.46F;
	}
}

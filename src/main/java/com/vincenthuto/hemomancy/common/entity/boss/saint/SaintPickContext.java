package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/**
 * What the picker knows when a Saint chooses its next move: position (band, sector, distance), pressure (kiting,
 * hugging, out of sight), timing (since the last opening, in the phase), the meters (the Saint's fraction and the
 * target's own value and secondary), the deeds and the awakening, and the challenger count.
 */
public record SaintPickContext(@Nullable SaintTrialEntity saint, int phase, @Nullable LivingEntity target,
		BossBands.Band band, BossBands.Sector sector, double distance, int kiteTicks, int hugTicks, int unseenTicks,
		int ticksSinceOpening, int ticksInPhase, float meterFraction, float targetMeter, float targetSecondary,
		int deedMask, String awakening, int challengers) {
	/** A context with only position and phase, for tests and tables. */
	public static SaintPickContext of(int phase, BossBands.Band band, BossBands.Sector sector) {
		return new SaintPickContext(null, phase, null, band, sector, 0.0D, 0, 0, 0, 0, 0, 0.0F, 0.0F, 0.0F, 0,
				"NONE", 1);
	}

	public SaintMoveInputs inputs(int variant) {
		return new SaintMoveInputs(phase, variant, deedMask, challengers);
	}
}

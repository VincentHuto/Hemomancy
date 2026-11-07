package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

import java.util.List;

/**
 * What a Saint on the combat kit provides, once, from {@code createKit()}: its moves, locomotion, meter, presence,
 * phases, damage type, the health added per challenger past the first (1 + h(n - 1), resolution B-5 makes it 0.5),
 * and how its ending runs.
 */
public record SaintKit(List<? extends SaintMove> moves, SaintLocomotion locomotion, SaintMeter meter,
		SaintPresence presence, SaintPhaseTable phases, ResourceKey<DamageType> damageType,
		double healthPerExtraChallenger, SaintEnding.Spec ending) {
	/** The one scaling coefficient (resolution B-5): Saint health and a Saint-owned meter's maximum or decay. */
	public static final double CHALLENGER_SCALING = 0.5D;

	public SaintKit {
		moves = List.copyOf(moves);
	}

	/** 1 + 0.5 (n - 1): the factor a Saint-owned meter's maximum or decay takes for {@code challengers}. */
	public static float scale(int challengers) {
		return (float) (1.0D + CHALLENGER_SCALING * (Math.max(1, challengers) - 1));
	}
}

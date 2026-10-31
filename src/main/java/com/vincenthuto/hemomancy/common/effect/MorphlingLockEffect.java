package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * A marker effect read by {@code MorphlingLock}: {@code pinned} holds a body in place (Holdfast's Holding, Kinbud's
 * Grasping Crown, Red Rope, Fold Shut) and {@code morphling_sealed} stops a host attacking or using items (Fold Shut,
 * Deliquesce, Cryptobiosis). Neither does anything on its own tick.
 */
public final class MorphlingLockEffect extends MobEffect {
	public MorphlingLockEffect(MobEffectCategory category, int color) {
		super(category, color);
	}
}

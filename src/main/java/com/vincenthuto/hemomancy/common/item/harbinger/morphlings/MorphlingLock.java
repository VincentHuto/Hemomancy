package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * The one "Morphling-locked" check (track note: Fold Shut and Deliquesce share it). {@code Paralysis.blocksActions}
 * folds {@link #blocksActions} in, so every input, use-item, attack and casting path that already respects paralysis
 * respects a sealed host; the paralysis movement mixin folds {@link #isPinned} in, so a pinned body cannot walk or
 * jump. Pinning a boss is capped at {@link DeadMansRopeRules#RED_ROPE_BOSS_ROOT_TICKS}.
 */
public final class MorphlingLock {
	private MorphlingLock() {
	}

	/** A sealed host cannot attack, use items or cast. */
	public static boolean blocksActions(LivingEntity entity) {
		return entity.isAlive() && entity.hasEffect(EffectInit.morphling_sealed);
	}

	/** A pinned body cannot walk or jump; knockback and falling still move it. */
	public static boolean isPinned(LivingEntity entity) {
		return entity.isAlive() && entity.hasEffect(EffectInit.pinned);
	}

	public static void seal(LivingEntity entity, int ticks) {
		entity.addEffect(new MobEffectInstance(EffectInit.morphling_sealed, ticks, 0, false, false, true));
		entity.stopUsingItem();
	}

	/** Pins a body for {@code ticks}, or the boss cap for a boss. */
	public static void pin(LivingEntity entity, int ticks) {
		int duration = ManipulationReactiveEvents.isBoss(entity)
				? Math.min(ticks, DeadMansRopeRules.RED_ROPE_BOSS_ROOT_TICKS) : ticks;
		entity.addEffect(new MobEffectInstance(EffectInit.pinned, duration, 0, false, true, true));
		if (entity instanceof net.minecraft.world.entity.Mob mob) mob.getNavigation().stop();
	}

	public static void release(LivingEntity entity) {
		entity.removeEffect(EffectInit.morphling_sealed);
		entity.removeEffect(EffectInit.pinned);
	}

	/** A sealed host's swing ends Deliquesce early instead of landing (spec: "attacking ends it early"). */
	public static void onBlockedAttack(LivingEntity entity) {
		if (!entity.level().isClientSide && blocksActions(entity)) {
			InkCapMorphlingItem.endDeliquesce(entity);
		}
	}
}

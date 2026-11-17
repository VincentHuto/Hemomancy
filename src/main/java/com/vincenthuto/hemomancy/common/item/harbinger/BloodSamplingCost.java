package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Sampling costs the creature something (core-loop Track 5): a draw takes health and leaves a per-creature cooldown,
 * held on the creature as an attachment. The numbers are {@link BloodSamplingRules}.
 */
public final class BloodSamplingCost {
	private BloodSamplingCost() {
	}

	/** Whether {@code target} was drawn too recently to be drawn again. A player is never on a sampling cooldown. */
	public static boolean recentlyDrawn(LivingEntity target) {
		return !(target instanceof Player)
				&& BloodSamplingRules.recentlyDrawn(HemoCapabilityAccess.getSampleDrawnUntil(target), target.level().getGameTime());
	}

	/** The draw's price, paid by the creature once its blood is in the vial: health, then the cooldown. */
	public static void charge(Player drawer, LivingEntity target, boolean syringe) {
		if (target instanceof Player || target.level().isClientSide) return;
		HemoCapabilityAccess.setSampleDrawnUntil(target, target.level().getGameTime() + BloodSamplingRules.COOLDOWN_TICKS);
		DamageSource source = new DamageSource(target.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(HemoDamageTypes.BLOOD_DRAW), drawer, drawer);
		target.hurt(source, BloodSamplingRules.healthCost(syringe));
	}
}

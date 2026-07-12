package com.vincenthuto.hemomancy.mixin.util;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.ClarityBiologyRules;
import com.vincenthuto.hemomancy.common.init.AttributeInit;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class MixinHooks {
	public static float scalePotionResponse(LivingEntity entity, float amount) {
		return scaleForClarity(entity, amount, ClarityBiologyRules::scalePotionResponse);
	}

	public static float scalePoisonDamage(LivingEntity entity, float amount) {
		return scaleForClarity(entity, amount, ClarityBiologyRules::scalePoisonDamage);
	}

	public static float scaleHungerExhaustion(Player player, float amount) {
		return scaleForClarity(player, amount, ClarityBiologyRules::scaleHungerExhaustion);
	}

	private static float scaleForClarity(LivingEntity entity, float amount, ClarityScaler scaler) {
		if (!(entity instanceof Player player)) {
			return amount;
		}
		return HemoCapabilityAccess.getUnstainedProgress(player)
				.map(progress -> scaler.scale(amount, progress.hasClarityUnlocked(), progress.getClarity()))
				.orElse(amount);
	}

	@FunctionalInterface
	private interface ClarityScaler {
		float scale(float amount, boolean clarityUnlocked, float clarity);
	}

	@SuppressWarnings("ConstantConditions")
	public static boolean canFly(LivingEntity livingEntity, boolean oldFlag, boolean newFlag) {
		AttributeInit.TriState fallFly = AttributeInit.canFallFly(livingEntity);

		if (fallFly == AttributeInit.TriState.DENY) {
			return false;
		} else if (fallFly == AttributeInit.TriState.DEFAULT) {
			return newFlag;
		}
		return !livingEntity.onGround() && !livingEntity.isPassenger() && !livingEntity.hasEffect(MobEffects.LEVITATION)
				&& oldFlag;
	}
}

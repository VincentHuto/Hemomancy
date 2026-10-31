package com.vincenthuto.hemomancy.mixin.util;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.ClarityBiologyRules;
import com.vincenthuto.hemomancy.common.init.AttributeInit;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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
		float scaled = scaleForClarity(player, amount, ClarityBiologyRules::scaleHungerExhaustion);
		return com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.MorphlingPowerEvents
				.scaleExhaustion(player, scaled);
	}

	/** Holdfast's Cling keeps a submerged host out of the current. */
	public static boolean resistsCurrents(Player player) {
		return com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.MorphlingPowerEvents
				.resistsCurrents(player);
	}

	public static boolean canWalkOnPowderSnow(Entity entity, boolean vanillaResult) {
		if (entity instanceof LivingEntity living && living.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.cryoprotection)) return true;
		return vanillaResult;
	}

	private static float scaleForClarity(LivingEntity entity, float amount, ClarityScaler scaler) {
		if (!(entity instanceof Player player)) {
			return amount;
		}
		return scaler.scale(amount, HemoCapabilityAccess.getUnstainedProgress(player).hasClarityUnlocked(), HemoCapabilityAccess.getUnstainedProgress(player).getClarity());
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
		}
		if (fallFly == AttributeInit.TriState.DEFAULT) {
			return newFlag;
		}
		return !livingEntity.onGround() && !livingEntity.isPassenger() && !livingEntity.hasEffect(MobEffects.LEVITATION)
				&& oldFlag;
	}
}

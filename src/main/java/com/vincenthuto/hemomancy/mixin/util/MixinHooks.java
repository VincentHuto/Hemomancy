package com.vincenthuto.hemomancy.mixin.util;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.ClarityBiologyRules;
import com.vincenthuto.hemomancy.common.init.AttributeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.EmberfangMorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.WinterShroudMorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.WinterShroudResilienceRules;

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
		return EmberfangMorphlingItem.scaleExhaustion(player,
				scaleForClarity(player, amount, ClarityBiologyRules::scaleHungerExhaustion));
	}

	public static boolean canWalkOnPowderSnow(Entity entity, boolean vanillaResult) {
		if (vanillaResult || !(entity instanceof Player player)) return vanillaResult;
		return HemoCapabilityAccess.getEquippedMorphling(player)
				.filter(cap -> cap.hasMorphling()
						&& cap.getEquippedMorphling().getItem() instanceof WinterShroudMorphlingItem)
				.map(cap -> WinterShroudResilienceRules.canTraversePowderSnow(
						MorphlingItem.getMaturityLevel(cap.getEquippedMorphling())))
				.orElse(false);
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
		}
		if (fallFly == AttributeInit.TriState.DEFAULT) {
			return newFlag;
		}
		return !livingEntity.onGround() && !livingEntity.isPassenger() && !livingEntity.hasEffect(MobEffects.LEVITATION)
				&& oldFlag;
	}
}

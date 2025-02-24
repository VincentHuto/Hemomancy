package com.vincenthuto.hemomancy.mixin.util;

import com.vincenthuto.hemomancy.common.init.AttributeInit;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class MixinHooks {
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

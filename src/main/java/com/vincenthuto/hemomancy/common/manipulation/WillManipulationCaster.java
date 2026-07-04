package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public final class WillManipulationCaster {
	private WillManipulationCaster() {
	}

	public static boolean cast(WillEntity will, ResourceLocation manipId) {
		if (will == null || manipId == null || will.level().isClientSide) return false;
		LivingEntity target = will.getTarget();
		if (target == null || !target.isAlive()) return false;
		BloodManipulation manipulation = ManipulationInit.MANIPS_TYPE_REGISTRY.get(manipId);
		if (!(manipulation instanceof EntityCastableManipulation castable)) return false;
		if (!EntityManipulationEffects.isSupported(manipulation)) return false;
		return castable.castFromEntity(ManipulationCastContext.forWill(will, target, will.getMainHandItem()));
	}
}

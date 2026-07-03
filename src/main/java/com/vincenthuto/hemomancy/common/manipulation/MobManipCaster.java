package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PathfinderMob;

public final class MobManipCaster {
	private MobManipCaster() {
	}

	public static boolean cast(PathfinderMob caster, ResourceLocation manipId, int radius) {
		if (caster == null || manipId == null || caster.level().isClientSide) return false;
		BloodManipulation manipulation = ManipulationInit.MANIPS_TYPE_REGISTRY.get(manipId);
		if (manipulation == null) return false;
		DrudgeAction action = manipulation.getDrudgeAction().orElse(null);
		if (action == null || action == DrudgeAction.DRUDGE_UNSUPPORTED) return false;
		return action.execute(caster, caster.level(), caster.blockPosition(), radius);
	}
}

package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar;

import net.minecraft.world.entity.LivingEntity;

public interface IScar {

	default boolean canEquip(LivingEntity player) {
		return true;
	}

	default boolean canUnequip(LivingEntity player) {
		return true;
	}

	ScarType getScarType();
	

	default void onEquipped(LivingEntity player) {
	}

	default void onUnequipped(LivingEntity player) {
	}

	default void onWornTick(LivingEntity player) {
	}

	default boolean willAutoSync(LivingEntity player) {
		return false;
	}
}
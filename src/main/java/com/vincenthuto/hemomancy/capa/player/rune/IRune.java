package com.vincenthuto.hemomancy.capa.player.rune;

import net.minecraft.world.entity.LivingEntity;

public interface IRune {

	RuneType getRuneType();

	default void onWornTick(LivingEntity player) {
	}

	default void onEquipped(LivingEntity player) {
	}

	default void onUnequipped(LivingEntity player) {
	}

	default boolean canEquip(LivingEntity player) {
		return true;
	}

	default boolean canUnequip(LivingEntity player) {
		return true;
	}

	default boolean willAutoSync(LivingEntity player) {
		return false;
	}
}
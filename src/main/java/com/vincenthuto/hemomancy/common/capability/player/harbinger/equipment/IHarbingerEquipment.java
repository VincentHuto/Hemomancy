package com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment;

import net.minecraft.world.entity.LivingEntity;

public interface IHarbingerEquipment {

	default boolean canEquip(LivingEntity player) {
		return true;
	}

	default boolean canUnequip(LivingEntity player) {
		return true;
	}

	HarbingerEquipmentType getHarbingerEquipmentType();
	

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

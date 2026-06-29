package com.vincenthuto.hemomancy.common.armor;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public final class ArmorSetHelper {
	private ArmorSetHelper() {
	}

	public static boolean hasFullPhantasmalBloodlust(Player player) {
		return player != null
				&& player.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.phantasmal_blood_lust_helm.get())
				&& player.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.phantasmal_blood_lust_chest.get())
				&& player.getItemBySlot(EquipmentSlot.LEGS).is(ItemInit.phantasmal_blood_lust_legs.get())
				&& player.getItemBySlot(EquipmentSlot.FEET).is(ItemInit.phantasmal_blood_lust_boots.get());
	}
}

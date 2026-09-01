package com.vincenthuto.hemomancy.common.item.shared.armor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface PlayerLayerHidingArmor {
	static boolean isWorn(LivingEntity entity) {
		return isWorn(entity.getArmorSlots());
	}

	static boolean isWorn(Iterable<ItemStack> armor) {
		for (ItemStack stack : armor) {
			if (hidesPlayerLayers(stack.getItem())) return true;
		}
		return false;
	}

	static boolean hidesPlayerLayers(Object item) {
		return item instanceof PlayerLayerHidingArmor;
	}
}

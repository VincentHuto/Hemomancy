package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class CellHandFormHelper {
	private CellHandFormHelper() {
	}

	public static boolean applySelection(Player player, BloodManipulation selectedManip) {
		String selectedName = selectedManip == null ? "" : selectedManip.getName();
		if (!isCellHandManip(selectedName)) {
			return true;
		}
		ItemStack held = player.getMainHandItem();
		if (!isCellHandForm(held)) {
			return true;
		}
		Item targetItem = itemFor(selectedName);
		if (held.is(targetItem)) {
			return true;
		}
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(targetItem));
		return true;
	}

	public static boolean isCellHandManip(String manipName) {
		return ManipulationEquipHelper.BLOOD_ABSORPTION.equals(manipName)
				|| ManipulationEquipHelper.BLOOD_PROJECTION.equals(manipName);
	}

	public static boolean isCellHandForm(ItemStack stack) {
		return !stack.isEmpty()
				&& (stack.getItem() instanceof BloodAbsorptionItem
				|| stack.getItem() instanceof BloodProjectionItem);
	}

	private static Item itemFor(String manipName) {
		return ManipulationEquipHelper.BLOOD_PROJECTION.equals(manipName)
				? ItemInit.blood_projection.get()
				: ItemInit.blood_absorption.get();
	}
}

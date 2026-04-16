package com.vincenthuto.hemomancy.common.menu.slot;

import com.vincenthuto.hemomancy.common.item.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.menu.PallidRetortMenu;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PallidRetortFlaskSlot extends Slot {

	public static boolean isFlask(ItemStack stack) {
		return stack.is(HLItemInit.cured_clay_flask.get()) || stack.getItem() instanceof BloodyFlaskItem;
	}

	public PallidRetortFlaskSlot(PallidRetortMenu menu, Container container, int slotIndex, int xPos, int yPos) {
		super(container, slotIndex, xPos, yPos);
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return 16;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return isFlask(stack);
	}
}

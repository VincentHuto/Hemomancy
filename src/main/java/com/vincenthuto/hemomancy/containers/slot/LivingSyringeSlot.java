package com.vincenthuto.hemomancy.containers.slot;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.item.BloodVialItem;
import com.vincenthuto.hemomancy.itemhandler.LivingSyringeItemHandler;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class LivingSyringeSlot extends SlotItemHandler {
	public LivingSyringeSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
	}

	@Override
	public int getMaxStackSize(@Nonnull ItemStack stack) {
		return 1;
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
		return stack.getItem() instanceof BloodVialItem;
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (getItemHandler() instanceof LivingSyringeItemHandler)
			((LivingSyringeItemHandler) getItemHandler()).setDirty();
	}
}
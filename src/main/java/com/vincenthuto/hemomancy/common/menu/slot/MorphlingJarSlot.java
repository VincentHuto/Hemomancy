package com.vincenthuto.hemomancy.common.menu.slot;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.common.item.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.itemhandler.MorphlingJarItemHandler;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class MorphlingJarSlot extends SlotItemHandler {
	public MorphlingJarSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
	}

	@Override
	public int getMaxStackSize(@Nonnull ItemStack stack) {
		return 1;
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
		return stack.getItem() instanceof MorphlingItem;


	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (getItemHandler() instanceof MorphlingJarItemHandler)
			((MorphlingJarItemHandler) getItemHandler()).setDirty();
	}
}
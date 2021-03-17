package com.huto.hemomancy.capabilities.mindrune;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IRunesItemHandler extends IItemHandlerModifiable {

	boolean isItemValidForSlot(int slot, ItemStack stack);

	boolean isEventBlocked();

	void setEventBlock(boolean blockEvents);

	void tick();
}
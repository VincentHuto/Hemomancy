package com.vincenthuto.hemomancy.capa.player.rune;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IRunesItemHandler extends IItemHandlerModifiable {

	boolean isEventBlocked();

	boolean isItemValidForSlot(int slot, ItemStack stack);

	void setEventBlock(boolean blockEvents);

	void tick();
}
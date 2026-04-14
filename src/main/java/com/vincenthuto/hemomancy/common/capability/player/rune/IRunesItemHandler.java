package com.vincenthuto.hemomancy.common.capability.player.rune;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IRunesItemHandler extends IItemHandlerModifiable {

	boolean isEventBlocked();

	boolean isItemValidForSlot(int slot, ItemStack stack);

	boolean isRunesUnlocked();

	void setRunesUnlocked(boolean unlocked);

	void setEventBlock(boolean blockEvents);

	void tick();
}
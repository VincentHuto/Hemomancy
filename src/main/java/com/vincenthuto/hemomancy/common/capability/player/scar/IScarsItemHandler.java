package com.vincenthuto.hemomancy.common.capability.player.scar;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IScarsItemHandler extends IItemHandlerModifiable {

	boolean isEventBlocked();

	boolean isItemValidForSlot(int slot, ItemStack stack);

	boolean isScarsUnlocked();

	void setScarsUnlocked(boolean unlocked);

	void setEventBlock(boolean blockEvents);

	void tick();
}
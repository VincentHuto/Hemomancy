package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public interface IScarsItemHandler extends IItemHandlerModifiable {

	boolean isEventBlocked();

	boolean isItemValidForSlot(int slot, ItemStack stack);

	boolean isScarsUnlocked();

	void setScarsUnlocked(boolean unlocked);

	void setEventBlock(boolean blockEvents);

	void tick();
}
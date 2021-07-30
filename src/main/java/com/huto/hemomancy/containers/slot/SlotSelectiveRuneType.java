package com.huto.hemomancy.containers.slot;

import com.huto.hemomancy.capa.rune.IRune;
import com.huto.hemomancy.capa.rune.IRunesItemHandler;
import com.huto.hemomancy.capa.rune.RunesCapabilities;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import Player;

public class SlotSelectiveRuneType extends SlotItemHandler {
	int mindruneSlot;
	Player player;
	Class<? extends Item> itemType;

	public SlotSelectiveRuneType(Player player, Class<? extends Item> itemType, IRunesItemHandler itemHandler,
			int slot, int par4, int par5) {
		super(itemHandler, slot, par4, par5);
		this.mindruneSlot = slot;
		this.player = player;
		this.itemType = itemType;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return itemType.isInstance(stack.getItem());
	}

	@Override
	public boolean canTakeStack(Player player) {
		ItemStack stack = getStack();
		if (stack.isEmpty())
			return false;

		IRune mindrune = stack.getCapability(RunesCapabilities.ITEM_RUNE).orElseThrow(NullPointerException::new);
		return mindrune.canUnequip(player);
	}

	@Override
	public ItemStack onTake(PlayerEntity playerIn, ItemStack stack) {
		if (!getHasStack() && !((IRunesItemHandler) getItemHandler()).isEventBlocked()
				&& stack.getCapability(RunesCapabilities.ITEM_RUNE).isPresent()) {
			stack.getCapability(RunesCapabilities.ITEM_RUNE, null).ifPresent((iRune) -> iRune.onUnequipped(playerIn));
		}
		super.onTake(playerIn, stack);
		return stack;
	}

	@Override
	public void putStack(ItemStack stack) {
		if (getHasStack() && !ItemStack.areItemStacksEqual(stack, getStack())
				&& !((IRunesItemHandler) getItemHandler()).isEventBlocked()
				&& getStack().getCapability(RunesCapabilities.ITEM_RUNE, null).isPresent()) {
			getStack().getCapability(RunesCapabilities.ITEM_RUNE, null)
					.ifPresent((iRune) -> iRune.onUnequipped(player));
		}

		ItemStack oldstack = getStack().copy();
		super.putStack(stack);

		if (getHasStack() && !ItemStack.areItemStacksEqual(oldstack, getStack())
				&& !((IRunesItemHandler) getItemHandler()).isEventBlocked()
				&& getStack().getCapability(RunesCapabilities.ITEM_RUNE, null).isPresent()) {
			getStack().getCapability(RunesCapabilities.ITEM_RUNE, null).ifPresent((iRune) -> iRune.onEquipped(player));
		}
	}

	@Override
	public int getSlotStackLimit() {
		return 1;
	}
}
package com.huto.hemomancy.containers.slot;

import com.huto.hemomancy.capabilities.mindrune.IRune;
import com.huto.hemomancy.capabilities.mindrune.IRunesItemHandler;
import com.huto.hemomancy.capabilities.mindrune.RunesCapabilities;
import com.huto.hemomancy.item.rune.ItemContractRune;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class SlotContractRune extends SlotItemHandler {
	int mindruneSlot;
	PlayerEntity player;

	public SlotContractRune(PlayerEntity player, IRunesItemHandler itemHandler, int slot, int par4, int par5) {
		super(itemHandler, slot, par4, par5);
		this.mindruneSlot = slot;
		this.player = player;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		if (stack.getItem() instanceof ItemContractRune) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean canTakeStack(PlayerEntity player) {
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
			stack.getCapability(RunesCapabilities.ITEM_RUNE, null)
					.ifPresent((iBauble) -> iBauble.onUnequipped(playerIn));
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
					.ifPresent((iBauble) -> iBauble.onUnequipped(player));
		}

		ItemStack oldstack = getStack().copy();
		super.putStack(stack);

		if (getHasStack() && !ItemStack.areItemStacksEqual(oldstack, getStack())
				&& !((IRunesItemHandler) getItemHandler()).isEventBlocked()
				&& getStack().getCapability(RunesCapabilities.ITEM_RUNE, null).isPresent()) {
			getStack().getCapability(RunesCapabilities.ITEM_RUNE, null)
					.ifPresent((iBauble) -> iBauble.onEquipped(player));
		}
	}

	@Override
	public int getSlotStackLimit() {
		return 1;
	}
}
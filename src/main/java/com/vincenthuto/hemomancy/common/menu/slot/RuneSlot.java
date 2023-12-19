package com.vincenthuto.hemomancy.common.menu.slot;

import com.vincenthuto.hemomancy.common.capability.player.rune.IRune;
import com.vincenthuto.hemomancy.common.capability.player.rune.IRunesItemHandler;
import com.vincenthuto.hemomancy.common.capability.player.rune.RunesCapabilities;
import com.vincenthuto.hemomancy.common.item.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.rune.ItemContractRune;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class RuneSlot extends SlotItemHandler {
	int runeSlot;
	Player player;

	public RuneSlot(Player player, IRunesItemHandler itemHandler, int slot, int par4, int par5) {
		super(itemHandler, slot, par4, par5);
		this.runeSlot = slot;
		this.player = player;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		if (stack.getItem() instanceof IRune && !(stack.getItem() instanceof ItemContractRune)
				&& !(stack.getItem() instanceof VasculariumCharmItem)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean mayPickup(Player player) {
		ItemStack stack = getItem();
		if (stack.isEmpty())
			return false;

		IRune mindrune = stack.getCapability(RunesCapabilities.ITEM_RUNE).orElseThrow(NullPointerException::new);
		return mindrune.canUnequip(player);
	}

	@Override
	public void onTake(Player playerIn, ItemStack stack) {
		if (!hasItem() && !((IRunesItemHandler) getItemHandler()).isEventBlocked()
				&& stack.getCapability(RunesCapabilities.ITEM_RUNE).isPresent()) {
			stack.getCapability(RunesCapabilities.ITEM_RUNE, null)
					.ifPresent((iBauble) -> iBauble.onUnequipped(playerIn));
		}
		super.onTake(playerIn, stack);
	}

	@Override
	public void set(ItemStack stack) {
		if (hasItem() && !ItemStack.matches(stack, getItem())
				&& !((IRunesItemHandler) getItemHandler()).isEventBlocked()
				&& getItem().getCapability(RunesCapabilities.ITEM_RUNE, null).isPresent()) {
			getItem().getCapability(RunesCapabilities.ITEM_RUNE, null)
					.ifPresent((iBauble) -> iBauble.onUnequipped(player));
		}

		ItemStack oldstack = getItem().copy();
		super.set(stack);

		if (hasItem() && !ItemStack.matches(oldstack, getItem())
				&& !((IRunesItemHandler) getItemHandler()).isEventBlocked()
				&& getItem().getCapability(RunesCapabilities.ITEM_RUNE, null).isPresent()) {
			getItem().getCapability(RunesCapabilities.ITEM_RUNE, null)
					.ifPresent((iBauble) -> iBauble.onEquipped(player));
		}
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}
}
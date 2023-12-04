package com.vincenthuto.hemomancy.common.menu.slot;

import com.vincenthuto.hemomancy.common.capability.player.rune.IRune;
import com.vincenthuto.hemomancy.common.capability.player.rune.IRunesItemHandler;
import com.vincenthuto.hemomancy.common.capability.player.rune.RunesCapabilities;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class SelectiveRuneTypeSlot extends SlotItemHandler {
	int mindruneSlot;
	Player player;
	Class<? extends Item> itemType;

	public SelectiveRuneTypeSlot(Player player, Class<? extends Item> itemType, IRunesItemHandler itemHandler, int slot,
			int par4, int par5) {
		super(itemHandler, slot, par4, par5);
		this.mindruneSlot = slot;
		this.player = player;
		this.itemType = itemType;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
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
	public boolean mayPlace(ItemStack stack) {

		return itemType.isInstance(stack.getItem());
	}

	@Override
	public void onTake(Player playerIn, ItemStack stack) {
		if (!hasItem() && !((IRunesItemHandler) getItemHandler()).isEventBlocked()
				&& stack.getCapability(RunesCapabilities.ITEM_RUNE).isPresent()) {
			stack.getCapability(RunesCapabilities.ITEM_RUNE, null).ifPresent((iRune) -> iRune.onUnequipped(playerIn));
		}
		super.onTake(playerIn, stack);
	}

	@Override
	public void set(ItemStack stack) {
		if (hasItem() && !ItemStack.isSameItemSameTags(stack, getItem())
				&& !((IRunesItemHandler) getItemHandler()).isEventBlocked()
				&& getItem().getCapability(RunesCapabilities.ITEM_RUNE, null).isPresent()) {
			getItem().getCapability(RunesCapabilities.ITEM_RUNE, null).ifPresent((iRune) -> iRune.onUnequipped(player));
		}

		ItemStack oldstack = getItem().copy();
		super.set(stack);

		if (hasItem() && !ItemStack.isSameItemSameTags(oldstack, getItem())
				&& !((IRunesItemHandler) getItemHandler()).isEventBlocked()
				&& getItem().getCapability(RunesCapabilities.ITEM_RUNE, null).isPresent()) {
			getItem().getCapability(RunesCapabilities.ITEM_RUNE, null).ifPresent((iRune) -> iRune.onEquipped(player));
		}
	}
}
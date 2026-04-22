package com.vincenthuto.hemomancy.common.menu.slot;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.scar.IScar;
import com.vincenthuto.hemomancy.common.capability.player.scar.IScarsItemHandler;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SelectiveScarTypeSlot extends SlotItemHandler {
	int mindScarSlot;
	Player player;
	Class<? extends Item> itemType;

	public SelectiveScarTypeSlot(Player player, Class<? extends Item> itemType, IScarsItemHandler itemHandler, int slot,
			int par4, int par5) {
		super(itemHandler, slot, par4, par5);
		this.mindScarSlot = slot;
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

		IScar mindscar = HemoCapabilityAccess.getScar(stack).orElseThrow(NullPointerException::new);
		return mindscar.canUnequip(player);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {

		return itemType.isInstance(stack.getItem());
	}

	@Override
	public void onTake(Player playerIn, ItemStack stack) {
		if (!hasItem() && !((IScarsItemHandler) getItemHandler()).isEventBlocked()
				&& HemoCapabilityAccess.getScar(stack).isPresent()) {
			HemoCapabilityAccess.getScar(stack).ifPresent((IScar) -> IScar.onUnequipped(playerIn));
		}
		super.onTake(playerIn, stack);
	}

	@Override
	public void set(ItemStack stack) {
		if (hasItem() && !ItemStack.isSameItemSameTags(stack, getItem())
				&& !((IScarsItemHandler) getItemHandler()).isEventBlocked()
				&& HemoCapabilityAccess.getScar(getItem()).isPresent()) {
			HemoCapabilityAccess.getScar(getItem()).ifPresent((IScar) -> IScar.onUnequipped(player));
		}

		ItemStack oldstack = getItem().copy();
		super.set(stack);

		if (hasItem() && !ItemStack.isSameItemSameTags(oldstack, getItem())
				&& !((IScarsItemHandler) getItemHandler()).isEventBlocked()
				&& HemoCapabilityAccess.getScar(getItem()).isPresent()) {
			HemoCapabilityAccess.getScar(getItem()).ifPresent((IScar) -> IScar.onEquipped(player));
		}
	}
}
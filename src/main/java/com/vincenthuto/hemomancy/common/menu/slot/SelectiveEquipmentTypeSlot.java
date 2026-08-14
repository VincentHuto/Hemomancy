package com.vincenthuto.hemomancy.common.menu.slot;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SelectiveEquipmentTypeSlot extends SlotItemHandler {
	int mindScarSlot;
	Player player;
	Class<? extends Item> itemType;

	public SelectiveEquipmentTypeSlot(Player player, Class<? extends Item> itemType, IItemHandlerModifiable itemHandler, int slot,
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

		return HemoCapabilityAccess.getScar(stack).map(scar -> scar.canUnequip(player)).orElse(true);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {

		return itemType.isInstance(stack.getItem());
	}

}

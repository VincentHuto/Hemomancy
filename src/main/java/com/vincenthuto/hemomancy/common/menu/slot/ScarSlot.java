package com.vincenthuto.hemomancy.common.menu.slot;

import com.vincenthuto.hemomancy.common.capability.player.scar.IScar;
import com.vincenthuto.hemomancy.common.capability.player.scar.IScarsItemHandler;
import com.vincenthuto.hemomancy.common.capability.player.scar.ScarsCapabilities;
import com.vincenthuto.hemomancy.common.item.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.scar.ItemFungalScar;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ScarSlot extends SlotItemHandler {
	int ScarSlot;
	Player player;

	public ScarSlot(Player player, IScarsItemHandler itemHandler, int slot, int par4, int par5) {
		super(itemHandler, slot, par4, par5);
		this.ScarSlot = slot;
		this.player = player;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		if (stack.getItem() instanceof IScar && !(stack.getItem() instanceof ItemFungalScar)
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

		IScar mindscar = stack.getCapability(ScarsCapabilities.ITEM_SCAR).orElseThrow(NullPointerException::new);
		return mindscar.canUnequip(player);
	}

	@Override
	public void onTake(Player playerIn, ItemStack stack) {
		if (!hasItem() && !((IScarsItemHandler) getItemHandler()).isEventBlocked()
				&& stack.getCapability(ScarsCapabilities.ITEM_SCAR).isPresent()) {
			stack.getCapability(ScarsCapabilities.ITEM_SCAR, null)
					.ifPresent((iBauble) -> iBauble.onUnequipped(playerIn));
		}
		super.onTake(playerIn, stack);
	}

	@Override
	public void set(ItemStack stack) {
		if (hasItem() && !ItemStack.matches(stack, getItem())
				&& !((IScarsItemHandler) getItemHandler()).isEventBlocked()
				&& getItem().getCapability(ScarsCapabilities.ITEM_SCAR, null).isPresent()) {
			getItem().getCapability(ScarsCapabilities.ITEM_SCAR, null)
					.ifPresent((iBauble) -> iBauble.onUnequipped(player));
		}

		ItemStack oldstack = getItem().copy();
		super.set(stack);

		if (hasItem() && !ItemStack.matches(oldstack, getItem())
				&& !((IScarsItemHandler) getItemHandler()).isEventBlocked()
				&& getItem().getCapability(ScarsCapabilities.ITEM_SCAR, null).isPresent()) {
			getItem().getCapability(ScarsCapabilities.ITEM_SCAR, null)
					.ifPresent((iBauble) -> iBauble.onEquipped(player));
		}
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}
}
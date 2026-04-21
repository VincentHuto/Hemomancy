package com.vincenthuto.hemomancy.common.menu.slot;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.crafting.VialCentrifugeBlockEntity;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CentrifugeSlot extends Slot {

	public CentrifugeSlot(Container pContainer, int pSlot, int pX, int pY) {
		super(pContainer, pSlot, pX, pY);
	}

	@Override
	public boolean mayPlace(ItemStack pStack) {
		return pStack.getItem() == ItemInit.bloody_vial.get();
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean mayPickup(net.minecraft.world.entity.player.Player pPlayer) {
		if (container instanceof VialCentrifugeBlockEntity te) {
			return !te.isSpinning();
		}
		return super.mayPickup(pPlayer);
	}

}

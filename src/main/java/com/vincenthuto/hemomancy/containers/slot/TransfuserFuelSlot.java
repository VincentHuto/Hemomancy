package com.vincenthuto.hemomancy.containers.slot;

import com.vincenthuto.hemomancy.container.MenuEarthlyTransfuser;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TransfuserFuelSlot extends Slot {
	private final MenuEarthlyTransfuser menu;

	public TransfuserFuelSlot(MenuEarthlyTransfuser p_39520_, Container p_39521_, int p_39522_, int p_39523_, int p_39524_) {
		super(p_39521_, p_39522_, p_39523_, p_39524_);
		this.menu = p_39520_;
	}

	@Override
	public boolean mayPlace(ItemStack p_39526_) {
		return this.menu.isFuel(p_39526_) && p_39526_.getItem() != Items.LAVA_BUCKET;
	}

	@Override
	public int getMaxStackSize(ItemStack p_39528_) {
		return isBucket(p_39528_) ? 1 : super.getMaxStackSize(p_39528_);
	}

	public static boolean isBucket(ItemStack p_39530_) {
		return p_39530_.is(Items.BUCKET);
	}
}
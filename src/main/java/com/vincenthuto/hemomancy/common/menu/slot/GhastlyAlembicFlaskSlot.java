package com.vincenthuto.hemomancy.common.menu.slot;

import com.vincenthuto.hemomancy.common.item.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.menu.GhastlyAlembicMenu;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GhastlyAlembicFlaskSlot extends Slot {

	public static boolean isFlask(ItemStack stack) {
		return stack.is(HLItemInit.cured_clay_flask.get()) || stack.getItem() instanceof BloodyFlaskItem;
	}

	public GhastlyAlembicFlaskSlot(GhastlyAlembicMenu p_39520_, Container p_39521_, int p_39522_, int p_39523_,
			int p_39524_) {
		super(p_39521_, p_39522_, p_39523_, p_39524_);
	}

	@Override
	public int getMaxStackSize(ItemStack p_39528_) {
		return 16;
	}

	@Override
	public boolean mayPlace(ItemStack p_39526_) {
		return isFlask(p_39526_);
	}
}
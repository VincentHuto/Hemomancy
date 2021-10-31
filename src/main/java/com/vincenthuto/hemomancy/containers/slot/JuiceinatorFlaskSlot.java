package com.vincenthuto.hemomancy.containers.slot;

import com.vincenthuto.hemomancy.container.MenuJuiceinator;
import com.vincenthuto.hutoslib.common.item.HLItemInit;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class JuiceinatorFlaskSlot extends Slot {

	public JuiceinatorFlaskSlot(MenuJuiceinator p_39520_, Container p_39521_, int p_39522_, int p_39523_,
			int p_39524_) {
		super(p_39521_, p_39522_, p_39523_, p_39524_);
	}

	@Override
	public boolean mayPlace(ItemStack p_39526_) {
		return isBucket(p_39526_);
	}

	@Override
	public int getMaxStackSize(ItemStack p_39528_) {
		return 1;
	}

	public static boolean isBucket(ItemStack p_39530_) {
		return p_39530_.is(HLItemInit.cured_clay_flask.get());
	}
}
package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.world.item.ItemStack;

/** Selects item-specific ambient visuals for media seated in a Cardinal Focus. */
public final class CardinalFocusMediumVisualRules {
	private CardinalFocusMediumVisualRules() {
	}

	public static boolean emitsQliphothRoots(ItemStack medium) {
		return medium != null && medium.is(ItemInit.qliphoth_seed.get());
	}
}

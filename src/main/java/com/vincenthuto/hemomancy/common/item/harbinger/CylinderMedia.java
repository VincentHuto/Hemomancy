package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.world.item.ItemStack;

public final class CylinderMedia {
	private CylinderMedia() {}

	public static boolean supported(ItemStack stack) {
		return stack.is(ItemInit.wax_cylinder.get()) || stack.is(ItemInit.ambergris_cylinder.get());
	}

	public static boolean masterCapable(ItemStack stack) {
		return stack.is(ItemInit.ambergris_cylinder.get());
	}

	public static boolean blank(ItemStack stack) {
		return supported(stack) && stack.getCount() == 1
				&& !stack.has(DataComponentInit.ANCIENT_RECORDING.get())
				&& !stack.has(DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get())
				&& !stack.has(DataComponentInit.RESONANT_PATTERN.get());
	}
}

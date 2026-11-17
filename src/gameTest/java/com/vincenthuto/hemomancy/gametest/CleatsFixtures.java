package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.item.harbinger.armor.CleatsFitting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Boots with Hematic Cleats already fitted and on, as the Armature leaves them (CN-10). */
public final class CleatsFixtures {
	private CleatsFixtures() {
	}

	public static ItemStack fitted(Item boots) {
		ItemStack stack = new ItemStack(boots);
		CleatsFitting.fit(stack);
		return stack;
	}
}

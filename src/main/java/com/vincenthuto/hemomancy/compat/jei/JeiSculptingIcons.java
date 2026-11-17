package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** The memory a JEI row shows for the manipulation that must be selected (its Blood Memory, by the memory_ id). */
final class JeiSculptingIcons {
	private JeiSculptingIcons() {
	}

	static ItemStack memoryFor(ResourceLocation manipulation) {
		ResourceLocation memory = ResourceLocation.fromNamespaceAndPath(manipulation.getNamespace(),
				"memory_" + manipulation.getPath());
		Item item = BuiltInRegistries.ITEM.get(memory);
		return item == Items.AIR ? new ItemStack(ItemInit.blood_projection.get()) : new ItemStack(item);
	}

	/** The manipulation's display name ({@code hemomancy.manipulation.<id>}). */
	static Component nameOf(ResourceLocation manipulation) {
		return Component.translatable("hemomancy.manipulation." + manipulation.getPath());
	}
}

package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;

/**
 * Represents a single material or process entry displayed as a node in the
 * "Materials &amp; Processes" tab on either the Skill Tree or Unstained
 * Progress screen.
 *
 * @param name        Registry-style name used for the translation key
 * @param displayName Human-readable display name
 * @param description Short description shown in the tooltip
 * @param category    Grouping category (e.g. "Blocks", "Materials", "Equipment")
 * @param iconStack   Supplier for the ItemStack to render inside the node
 * @param hasRecipe   Whether clicking should attempt to show a crafting recipe
 */
public record MaterialEntry(
		String name,
		String displayName,
		String description,
		String category,
		Supplier<ItemStack> iconStack,
		boolean hasRecipe
) {
	/** Convenience constructor without recipe. */
	public MaterialEntry(String name, String displayName, String description,
						 String category, Supplier<ItemStack> iconStack) {
		this(name, displayName, description, category, iconStack, true);
	}
}

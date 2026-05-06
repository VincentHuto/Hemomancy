package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.UnlockPredicate;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * Represents a single material or process entry displayed as a node in the
 * "Materials &amp; Processes" tab on either the Skill Tree or Unstained
 * Progress screen.
 *
 * @param name             Registry-style name used for the translation key
 * @param displayName      Human-readable display name
 * @param description      Short description shown in the tooltip
 * @param category         Grouping category (e.g. "Blocks", "Materials", "Equipment")
 * @param iconStack        Supplier for the ItemStack to render inside the node
 * @param hasRecipe        Whether clicking should attempt to show a crafting recipe
 * @param unlockPredicate  Condition that must be true for this entry to appear;
 *                         defaults to {@link UnlockPredicate#always()}
 */
public record MaterialEntry(
		String name,
		String displayName,
		String description,
		String category,
		Supplier<ItemStack> iconStack,
		boolean hasRecipe,
		UnlockPredicate unlockPredicate
) {
	/** Convenience constructor: no recipe flag, always visible. */
	public MaterialEntry(String name, String displayName, String description,
						 String category, Supplier<ItemStack> iconStack) {
		this(name, displayName, description, category, iconStack, true, UnlockPredicate.always());
	}

	/** Convenience constructor: explicit recipe flag, always visible. */
	public MaterialEntry(String name, String displayName, String description,
						 String category, Supplier<ItemStack> iconStack, boolean hasRecipe) {
		this(name, displayName, description, category, iconStack, hasRecipe, UnlockPredicate.always());
	}
}

package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.UnlockPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * Represents a single material or process entry displayed as a node in the
 * "Materials &amp; Processes" tab on either the Skill Tree or Unstained
 * Progress screen.
 *
 * @param name             Registry-style material id
 * @param nameKey          Translation key of the display name (often the item's or block's own)
 * @param descriptionKey   Translation key of the short tooltip description; empty for none
 * @param categoryKey      Translation key of the grouping category (e.g. "Blocks", "Materials", "Equipment")
 * @param iconStack        Supplier for the ItemStack to render inside the node
 * @param hasRecipe        Whether clicking should attempt to show a crafting recipe
 * @param unlockPredicate  Condition that must be true for this entry to appear;
 *                         defaults to {@link UnlockPredicate#always()}
 */
public record MaterialEntry(
		String name,
		String nameKey,
		String descriptionKey,
		String categoryKey,
		Supplier<ItemStack> iconStack,
		boolean hasRecipe,
		UnlockPredicate unlockPredicate
) {
	/** Convenience constructor: no recipe flag, always visible. */
	public MaterialEntry(String name, String nameKey, String descriptionKey,
						 String categoryKey, Supplier<ItemStack> iconStack) {
		this(name, nameKey, descriptionKey, categoryKey, iconStack, true, UnlockPredicate.always());
	}

	/** Convenience constructor: explicit recipe flag, always visible. */
	public MaterialEntry(String name, String nameKey, String descriptionKey,
						 String categoryKey, Supplier<ItemStack> iconStack, boolean hasRecipe) {
		this(name, nameKey, descriptionKey, categoryKey, iconStack, hasRecipe, UnlockPredicate.always());
	}

	public MutableComponent displayName() {
		return Component.translatable(nameKey);
	}

	public MutableComponent description() {
		return descriptionKey.isEmpty() ? Component.empty() : Component.translatable(descriptionKey);
	}

	public MutableComponent category() {
		return Component.translatable(categoryKey);
	}
}

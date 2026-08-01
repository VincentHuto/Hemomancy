package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record RecipeMapEntry(RecipeMapKey key, String displayName, int column, String family,
		int order, boolean visible, boolean unlocked) {
	public enum Kind { RITE, CRAFTING, SIGIL }

	public RecipeMapEntry {
		Objects.requireNonNull(key, "key");
		displayName = Objects.requireNonNullElse(displayName, key.id().getPath());
		family = Objects.requireNonNullElse(family, RecipeMapLayout.MISC_FAMILY);
		column = Math.max(0, Math.min(8, column));
	}

	public ResourceLocation id() {
		return key.id();
	}
}

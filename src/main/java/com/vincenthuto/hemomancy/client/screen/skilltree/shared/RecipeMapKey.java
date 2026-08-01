package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record RecipeMapKey(RecipeMapEntry.Kind kind, ResourceLocation id) {
	public RecipeMapKey {
		Objects.requireNonNull(kind, "kind");
		Objects.requireNonNull(id, "id");
	}
}

package com.vincenthuto.hemomancy.common.recipe;

import net.minecraft.world.item.crafting.Ingredient;

public record BloodStructureOffering(Ingredient ingredient, int count) {
	public BloodStructureOffering {
		if (ingredient == null || ingredient.isEmpty()) {
			throw new IllegalArgumentException("Blood structure offering ingredient cannot be empty");
		}
		if (count <= 0) {
			throw new IllegalArgumentException("Blood structure offering count must be positive");
		}
	}
}

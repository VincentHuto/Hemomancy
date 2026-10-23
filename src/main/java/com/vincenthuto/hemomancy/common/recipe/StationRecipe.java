package com.vincenthuto.hemomancy.common.recipe;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * L-8: a recipe that a Hemomancy station, rite or loom reads through its own rules from
 * {@code RecipeManager.getAllRecipesFor}. Vanilla never matches it against a crafting grid, so it is a plain
 * {@code Recipe<RecipeInput>} rather than a {@code CustomRecipe}, and it stays out of the recipe book by default.
 */
public abstract class StationRecipe implements Recipe<RecipeInput> {
	@Override
	public boolean isSpecial() {
		return true;
	}
}

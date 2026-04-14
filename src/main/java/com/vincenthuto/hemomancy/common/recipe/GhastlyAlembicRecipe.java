package com.vincenthuto.hemomancy.common.recipe;

import java.util.List;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class GhastlyAlembicRecipe extends AbstractCookingRecipe {
	public static List<GhastlyAlembicRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.ghastly_alembic_recipe_type.get());
	}

	public GhastlyAlembicRecipe(ResourceLocation resourceLocation, String group, Ingredient ingredient, ItemStack result,
			float experience, int cookingTime) {
		super(RecipeInit.ghastly_alembic_recipe_type.get(), resourceLocation, group, CookingBookCategory.MISC, ingredient, result, experience,
				cookingTime);
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> allIngredients = NonNullList.create();
		allIngredients.add(this.ingredient);
		return allIngredients;
	}


	@Override
	public ItemStack getResultItem(RegistryAccess p_266851_) {
		return this.result;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.ghastly_alembic_serializer.get();
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(BlockInit.ghastly_alembic.get());
	}
}
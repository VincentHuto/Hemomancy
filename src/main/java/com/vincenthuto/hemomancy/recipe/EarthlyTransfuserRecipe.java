package com.vincenthuto.hemomancy.recipe;

import java.util.Collection;

import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.init.RecipeInit;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class EarthlyTransfuserRecipe extends AbstractCookingRecipe {
	public EarthlyTransfuserRecipe(ResourceLocation resourceLocation, String group, Ingredient ingredient,
			ItemStack result, float experience, int cookingTime) {
		super(RecipeInit.earthly_transfuser_recipe_type, resourceLocation, group, ingredient, result, experience,
				cookingTime);
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(BlockInit.earthly_transfuser.get());
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.earthly_transfuser_serializer.get();
	}

	public static Collection<EarthlyTransfuserRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.earthly_transfuser_recipe_type);
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> allIngredients = NonNullList.create();
		allIngredients.add(this.ingredient);
		return allIngredients;
	}
}
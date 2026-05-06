package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IncubatorRecipe extends CustomRecipe {

	private ResourceLocation id;
	private final NonNullList<Ingredient> catalysts;
	private final ItemStack result;

	public IncubatorRecipe(ResourceLocation recipeId, NonNullList<Ingredient> catalysts, ItemStack result) {
		super(CraftingBookCategory.MISC);
		this.id = recipeId;
		this.catalysts = catalysts;
		this.result = result;
	}

	public ResourceLocation getId() { return id; }

	public static List<IncubatorRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.incubator_recipe_type.get())
				.stream().map(RecipeHolder::value).collect(Collectors.toList());
	}

	public NonNullList<Ingredient> getCatalysts() {
		return catalysts;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return catalysts;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
		return result.copy();
	}

	public ItemStack getResultItemStack() {
		return result;
	}

	/**
	 * Order-insensitive match: checks that the provided list of items
	 * contains exactly the same items as the catalysts (by ingredient test).
	 */
	public boolean matchesCatalysts(List<Item> items) {
		if (items.size() != catalysts.size()) return false;

		List<Item> remaining = new ArrayList<>(items);
		for (Ingredient catalyst : catalysts) {
			boolean found = false;
			for (int i = 0; i < remaining.size(); i++) {
				if (catalyst.test(new ItemStack(remaining.get(i)))) {
					remaining.remove(i);
					found = true;
					break;
				}
			}
			if (!found) return false;
		}
		return remaining.isEmpty();
	}

	/**
	 * Checks if a given item matches any catalyst ingredient in this recipe.
	 */
	public boolean isValidCatalyst(ItemStack stack) {
		for (Ingredient catalyst : catalysts) {
			if (catalyst.test(stack)) return true;
		}
		return false;
	}

	@Override
	public boolean matches(CraftingInput container, Level level) {
		return false;
	}

	@Override
	public ItemStack assemble(CraftingInput container, HolderLookup.Provider registryAccess) {
		return result.copy();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.incubator_serializer.get();
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.incubator_recipe_type.get();
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(BlockInit.morphling_incubator.get());
	}
}

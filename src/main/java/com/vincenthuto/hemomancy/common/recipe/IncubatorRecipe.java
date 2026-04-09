package com.vincenthuto.hemomancy.common.recipe;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class IncubatorRecipe extends CustomRecipe {

	private final NonNullList<Ingredient> catalysts;
	private final ItemStack result;

	public IncubatorRecipe(ResourceLocation id, NonNullList<Ingredient> catalysts, ItemStack result) {
		super(id, CraftingBookCategory.MISC);
		this.catalysts = catalysts;
		this.result = result;
	}

	public static List<IncubatorRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.incubator_recipe_type.get());
	}

	public NonNullList<Ingredient> getCatalysts() {
		return catalysts;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return catalysts;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registryAccess) {
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
	public boolean matches(CraftingContainer container, Level level) {
		return false; // Not used in a crafting table
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
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

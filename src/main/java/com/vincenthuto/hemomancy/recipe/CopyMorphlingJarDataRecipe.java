package com.vincenthuto.hemomancy.recipe;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.item.morphlings.ItemMorphlingJar;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class CopyMorphlingJarDataRecipe extends ShapedRecipe {
	public CopyMorphlingJarDataRecipe(final ResourceLocation id, final String group, final int recipeWidth,
			final int recipeHeight, final NonNullList<Ingredient> ingredients, final ItemStack recipeOutput) {
		super(id, group, recipeWidth, recipeHeight, ingredients, recipeOutput);
	}

	public CopyMorphlingJarDataRecipe(ShapedRecipe shapedRecipe) {
		super(shapedRecipe.getId(), shapedRecipe.getGroup(), shapedRecipe.getRecipeWidth(),
				shapedRecipe.getRecipeHeight(), shapedRecipe.getIngredients(), shapedRecipe.getResultItem());
	}

	@Override
	public ItemStack assemble(CraftingContainer inv) {
		final ItemStack craftingResult = super.assemble(inv);
		ItemStack dataSource = ItemStack.EMPTY;

		if (!craftingResult.isEmpty()) {
			for (int i = 0; i < inv.getContainerSize(); i++) {
				final ItemStack item = inv.getItem(i);
				if (!item.isEmpty() && item.getItem() instanceof ItemMorphlingJar) {
					dataSource = item;
					break;
				}
			}

			if (!dataSource.isEmpty() && dataSource.hasTag()) {
				craftingResult.setTag(dataSource.getTag().copy());
			}
		}

		return craftingResult;
	}

	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>
			implements RecipeSerializer<CopyMorphlingJarDataRecipe> {
		@Nullable
		@Override
		public CopyMorphlingJarDataRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			return new CopyMorphlingJarDataRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(recipeId, buffer));
		}

		@Override
		public CopyMorphlingJarDataRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				return new CopyMorphlingJarDataRecipe(RecipeSerializer.SHAPED_RECIPE.fromJson(recipeId, json));
			} catch (Exception exception) {
				Hemomancy.LOGGER.info("Error reading CopyJar Recipe from packet: ", exception);
				throw exception;
			}
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, CopyMorphlingJarDataRecipe recipe) {
			try {
				RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
			} catch (Exception exception) {
				Hemomancy.LOGGER.info("Error writing CopyJar Recipe to packet: ", exception);
				throw exception;
			}
		}
	}

}
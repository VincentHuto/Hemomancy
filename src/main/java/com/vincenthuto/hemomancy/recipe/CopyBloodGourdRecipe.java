package com.vincenthuto.hemomancy.recipe;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.item.tool.BloodGourdItem;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class CopyBloodGourdRecipe extends ShapedRecipe {
	public static class Serializer implements RecipeSerializer<CopyBloodGourdRecipe> {
		@Override
		public CopyBloodGourdRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				return new CopyBloodGourdRecipe(RecipeSerializer.SHAPED_RECIPE.fromJson(recipeId, json));
			} catch (Exception exception) {
				Hemomancy.LOGGER.info("Error reading CopyBloodGourdRecipe from packet: ", exception);
				throw exception;
			}
		}

		@Nullable
		@Override
		public CopyBloodGourdRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			return new CopyBloodGourdRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(recipeId, buffer));
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, CopyBloodGourdRecipe recipe) {
			try {
				RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
			} catch (Exception exception) {
				Hemomancy.LOGGER.info("Error writing CopyBloodGourdRecipe Recipe to packet: ", exception);
				throw exception;
			}
		}
	}

	public CopyBloodGourdRecipe(final ResourceLocation id, final String group, final int recipeWidth,
			final int recipeHeight, final NonNullList<Ingredient> ingredients, final ItemStack recipeOutput) {
		super(id, group, null, recipeWidth, recipeHeight, ingredients, recipeOutput);
	}

	public CopyBloodGourdRecipe(ShapedRecipe shapedRecipe) {
		super(shapedRecipe.getId(), shapedRecipe.getGroup(), CraftingBookCategory.MISC, shapedRecipe.getRecipeWidth(),
				shapedRecipe.getRecipeHeight(), shapedRecipe.getIngredients(), shapedRecipe.getResultItem());
	}

	@Override
	public ItemStack assemble(CraftingContainer inv) {
		final ItemStack craftingResult = super.assemble(inv);
		ItemStack dataSource = ItemStack.EMPTY;

		if (!craftingResult.isEmpty()) {
			for (int i = 0; i < inv.getContainerSize(); i++) {
				final ItemStack item = inv.getItem(i);
				if (!item.isEmpty() && item.getItem() instanceof BloodGourdItem) {

					dataSource = item;
					break;
				}
			}
			if (dataSource.getItem() instanceof BloodGourdItem) {
				if (!dataSource.isEmpty() && dataSource.hasTag()) {
					craftingResult.setTag(dataSource.getTag().copy());
					IBloodVolume bloodVolume = dataSource.getCapability(BloodVolumeProvider.VOLUME_CAPA)
							.orElseThrow(NullPointerException::new);
					if (craftingResult.getItem() instanceof BloodGourdItem) {
						IBloodVolume resultVolume = craftingResult.getCapability(BloodVolumeProvider.VOLUME_CAPA)
								.orElseThrow(NullPointerException::new);
						resultVolume.setBloodVolume(bloodVolume.getBloodVolume());
					}
				}
			}
		}

		return craftingResult;
	}

}
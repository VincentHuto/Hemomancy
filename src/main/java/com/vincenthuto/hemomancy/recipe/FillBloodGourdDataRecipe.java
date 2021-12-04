package com.vincenthuto.hemomancy.recipe;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.item.tool.ItemBloodGourd;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class FillBloodGourdDataRecipe extends ShapedRecipe {
	public FillBloodGourdDataRecipe(final ResourceLocation id, final String group, final int recipeWidth,
			final int recipeHeight, final NonNullList<Ingredient> ingredients, final ItemStack recipeOutput) {
		super(id, group, recipeWidth, recipeHeight, ingredients, recipeOutput);
	}

	public FillBloodGourdDataRecipe(ShapedRecipe shapedRecipe) {
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
				if (!item.isEmpty() && item.getItem() instanceof ItemBloodGourd) {

					dataSource = item;
					break;
				}
			}
			if (dataSource.getItem()instanceof ItemBloodGourd gourd1) {
				if (!dataSource.isEmpty() && dataSource.hasTag()) {
					craftingResult.setTag(dataSource.getTag().copy());
					IBloodVolume bloodVolume = dataSource.getCapability(BloodVolumeProvider.VOLUME_CAPA)
							.orElseThrow(NullPointerException::new);
					if (craftingResult.getItem()instanceof ItemBloodGourd gourd2) {
						IBloodVolume resultVolume = craftingResult.getCapability(BloodVolumeProvider.VOLUME_CAPA)
								.orElseThrow(NullPointerException::new);

						float maxBlood = gourd2.getMaxBlood();
						float fillBlood = bloodVolume.getBloodVolume() + 200;
						float fillAmount = fillBlood <= maxBlood ? fillBlood : maxBlood;

						resultVolume.setBloodVolume(fillAmount);
					}
				}
			}
		}

		return craftingResult;
	}

	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>
			implements RecipeSerializer<FillBloodGourdDataRecipe> {
		@Nullable
		@Override
		public FillBloodGourdDataRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			return new FillBloodGourdDataRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(recipeId, buffer));
		}

		@Override
		public FillBloodGourdDataRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				return new FillBloodGourdDataRecipe(RecipeSerializer.SHAPED_RECIPE.fromJson(recipeId, json));
			} catch (Exception exception) {
				Hemomancy.LOGGER.info("Error reading CopyBloodGourdRecipe from packet: ", exception);
				throw exception;
			}
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, FillBloodGourdDataRecipe recipe) {
			try {
				RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
			} catch (Exception exception) {
				Hemomancy.LOGGER.info("Error writing CopyBloodGourdRecipe Recipe to packet: ", exception);
				throw exception;
			}
		}
	}

}
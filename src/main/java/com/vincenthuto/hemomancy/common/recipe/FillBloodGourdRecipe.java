package com.vincenthuto.hemomancy.common.recipe;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class FillBloodGourdRecipe extends ShapedRecipe {
	public static class Serializer implements RecipeSerializer<FillBloodGourdRecipe> {
		@Override
		public FillBloodGourdRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				return new FillBloodGourdRecipe(RecipeSerializer.SHAPED_RECIPE.fromJson(recipeId, json));
			} catch (Exception exception) {
				Hemomancy.LOGGER.info("Error reading CopyBloodGourdRecipe from packet: ", exception);
				throw exception;
			}
		}

		@Nullable
		@Override
		public FillBloodGourdRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			return new FillBloodGourdRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(recipeId, buffer));
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, FillBloodGourdRecipe recipe) {
			try {
				RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
			} catch (Exception exception) {
				Hemomancy.LOGGER.info("Error writing CopyBloodGourdRecipe Recipe to packet: ", exception);
				throw exception;
			}
		}
	}

	public FillBloodGourdRecipe(final ResourceLocation id, final String group, final int recipeWidth,
			final int recipeHeight, final NonNullList<Ingredient> ingredients, final ItemStack recipeOutput) {
		super(id, group, CraftingBookCategory.MISC, recipeWidth, recipeHeight, ingredients, recipeOutput);
	}

	public FillBloodGourdRecipe(ShapedRecipe shapedRecipe) {
		super(shapedRecipe.getId(), shapedRecipe.getGroup(), CraftingBookCategory.MISC, shapedRecipe.getRecipeWidth(),
				shapedRecipe.getRecipeHeight(), shapedRecipe.getIngredients(), shapedRecipe.getResultItem(null));
	}

	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess p_266725_) {
		final ItemStack craftingResult = super.assemble(inv, p_266725_);
		ItemStack dataSource = ItemStack.EMPTY;

		if (!craftingResult.isEmpty()) {
			for (int i = 0; i < inv.getContainerSize(); i++) {
				final ItemStack item = inv.getItem(i);
				if (!item.isEmpty() && item.getItem() instanceof BloodGourdItem) {

					dataSource = item;
					break;
				}
			}
			if (dataSource.getItem() instanceof BloodGourdItem gourd1) {
				if (!dataSource.isEmpty() && dataSource.hasTag()) {
					craftingResult.setTag(dataSource.getTag().copy());
					IBloodVolume bloodVolume = dataSource.getCapability(BloodVolumeProvider.VOLUME_CAPA)
							.orElseThrow(NullPointerException::new);
					if (craftingResult.getItem() instanceof BloodGourdItem gourd2) {
						IBloodVolume resultVolume = craftingResult.getCapability(BloodVolumeProvider.VOLUME_CAPA)
								.orElseThrow(NullPointerException::new);

						double maxBlood = gourd2.getMaxBlood();
						double fillBlood = bloodVolume.getBloodVolume() + 200;
						double fillAmount = fillBlood <= maxBlood ? fillBlood : maxBlood;

						resultVolume.setBloodVolume(fillAmount);
					}
				}
			}
		}

		return craftingResult;
	}

}
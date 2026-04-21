package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.IncubatorRecipe;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class IncubatorRecipeSerializer implements RecipeSerializer<IncubatorRecipe> {

	@Override
	public IncubatorRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
		// Read catalysts array
		JsonArray catalystsArray = GsonHelper.getAsJsonArray(json, "catalysts");
		NonNullList<Ingredient> catalysts = NonNullList.create();
		for (int i = 0; i < catalystsArray.size(); i++) {
			Ingredient ingredient = Ingredient.fromJson(catalystsArray.get(i));
			if (!ingredient.isEmpty()) {
				catalysts.add(ingredient);
			}
		}

		// Read result
		ItemStack result;
		if (json.get("result").isJsonObject()) {
			result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
		} else {
			String resultId = GsonHelper.getAsString(json, "result");
			int count = GsonHelper.getAsInt(json, "count", 1);
			ResourceLocation resultLoc = new ResourceLocation(resultId);
			result = new ItemStack(net.neoforged.neoforge.registries.ForgeRegistries.ITEMS.getValue(resultLoc), count);
		}

		return new IncubatorRecipe(recipeId, catalysts, result);
	}

	@Override
	public IncubatorRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
		try {
			int catalystCount = buffer.readVarInt();
			NonNullList<Ingredient> catalysts = NonNullList.create();
			for (int i = 0; i < catalystCount; i++) {
				catalysts.add(Ingredient.fromNetwork(buffer));
			}
			ItemStack result = buffer.readItem();
			return new IncubatorRecipe(recipeId, catalysts, result);
		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error reading incubator recipe from packet.", e);
			throw e;
		}
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, IncubatorRecipe recipe) {
		try {
			NonNullList<Ingredient> catalysts = recipe.getCatalysts();
			buffer.writeVarInt(catalysts.size());
			for (Ingredient catalyst : catalysts) {
				catalyst.toNetwork(buffer);
			}
			buffer.writeItem(recipe.getResultItemStack());
		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error writing incubator recipe to packet.", e);
			throw e;
		}
	}
}

package com.vincenthuto.hemomancy.recipe.serializer;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.recipe.RecallerRecipe;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistries;

public class RecallerRecipeSerializer implements RecipeSerializer<RecallerRecipe> {
	public static HashMap<ResourceLocation, RecallerRecipe> ALL_RECIPES = new HashMap<>();

	public static RecallerRecipe getRecipe(String path) {
		return ALL_RECIPES.get(new ResourceLocation("hemomancy:recaller/" + path));
	}

	@Override
	public RecallerRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {

		Map<EnumBloodTendency, Float> tendency = RecallerRecipe.blank();
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			if (pJson.has(tend.toString().toLowerCase())) {
				tendency.put(tend, pJson.get(tend.toString().toLowerCase()).getAsFloat());
			} else {
				tendency.put(tend, 0f);
			}
		}

		ItemStack result;
		if (pJson.get("result").isJsonObject())
			result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));
		else {
			int c = GsonHelper.getAsInt(pJson, "count");
			String s1 = GsonHelper.getAsString(pJson, "result");
			ResourceLocation resourcelocation = new ResourceLocation(s1);
			result = new ItemStack(ForgeRegistries.ITEMS.getValue(resourcelocation), c);

		}

		if (pJson.has("ingredient")) {
			JsonElement jsonelement = GsonHelper.isArrayNode(pJson, "ingredient")
					? GsonHelper.getAsJsonArray(pJson, "ingredient")
					: GsonHelper.getAsJsonObject(pJson, "ingredient");
			Ingredient ingredient = Ingredient.fromJson(jsonelement);
			RecallerRecipe recipe = new RecallerRecipe(pRecipeId, ingredient, tendency, result);
			ALL_RECIPES.put(pRecipeId, recipe);
			return recipe;
		} else {
			Ingredient ingredient = Ingredient.EMPTY;
			RecallerRecipe recipe = new RecallerRecipe(pRecipeId, ingredient, tendency, result);
			ALL_RECIPES.put(pRecipeId, recipe);
			return recipe;
		}

	}

	@Override
	public RecallerRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
		try {
			ResourceLocation id = pBuffer.readResourceLocation();
			Ingredient input = Ingredient.of(pBuffer.readItem());
			Map<EnumBloodTendency, Float> tends = new HashMap<>();
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				tends.put(tend, pBuffer.readFloat());
			}
			ItemStack output = pBuffer.readItem();
			return new RecallerRecipe(id, input, tends, output);
		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error reading recaller recipe from packet.", e);
			throw e;
		}
	}

	@Override
	public void toNetwork(FriendlyByteBuf pBuffer, RecallerRecipe pRecipe) {
		try {
			pBuffer.writeResourceLocation(pRecipe.getId());
			if (pRecipe.getIngredients().get(0).getItems().length > 0) {
				pBuffer.writeItem(pRecipe.getIngredients().get(0).getItems()[0]);
			} else {
				pBuffer.writeItem(ItemStack.EMPTY);
			}
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				pBuffer.writeFloat(pRecipe.getTendency().get(tend));
			}
			pBuffer.writeItemStack(pRecipe.getResultItem(null), false);
		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error writing recaller recipe to packet.", e);
			throw e;
		}
	}
}

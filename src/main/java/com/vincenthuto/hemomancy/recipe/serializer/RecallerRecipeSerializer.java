package com.vincenthuto.hemomancy.recipe.serializer;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.recipe.RecallerRecipe;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class RecallerRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>>
		implements RecipeSerializer<RecallerRecipe> {

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
			result = new ItemStack(Registry.ITEM.getOptional(resourcelocation).orElseThrow(() -> {
				return new IllegalStateException("Item: " + s1 + " does not exist");
			}), c);
		}

		if (pJson.has("ingredient")) {
			JsonElement jsonelement = GsonHelper.isArrayNode(pJson, "ingredient")
					? GsonHelper.getAsJsonArray(pJson, "ingredient")
					: GsonHelper.getAsJsonObject(pJson, "ingredient");
			Ingredient ingredient = Ingredient.fromJson(jsonelement);
			RecallerRecipe recipe = new RecallerRecipe(pRecipeId, ingredient, tendency, result);
			return recipe;
		} else {
			RecallerRecipe recipe = new RecallerRecipe(pRecipeId, tendency, result);
			return recipe;
		}

	}

	@Override
	public RecallerRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
		try {
			ResourceLocation id = pBuffer.readResourceLocation();
			Map<EnumBloodTendency, Float> tends = new HashMap<EnumBloodTendency, Float>();
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				tends.put(tend, pBuffer.readFloat());
			}
			ItemStack output = pBuffer.readItem();
			Ingredient input = Ingredient.of(pBuffer.readItem());
			return new RecallerRecipe(id, input, tends, output);
		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error reading recaller recipe from packet.", (Throwable) e);
			throw e;
		}
	}

	@Override
	public void toNetwork(FriendlyByteBuf pBuffer, RecallerRecipe pRecipe) {
		try {

			pBuffer.writeResourceLocation(pRecipe.getId());
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				pBuffer.writeFloat(pRecipe.getTendency().get(tend));
			}
			pBuffer.writeItemStack(pRecipe.getResultItem(), false);
			pBuffer.writeItem(pRecipe.getIngredients().get(0).getItems()[0]);

		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error writing recaller recipe to packet.", (Throwable) e);
			throw e;
		}
	}
}

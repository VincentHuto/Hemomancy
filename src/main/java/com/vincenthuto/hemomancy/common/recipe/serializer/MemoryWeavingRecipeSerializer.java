package com.vincenthuto.hemomancy.common.recipe.serializer;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.registries.ForgeRegistries;

public class MemoryWeavingRecipeSerializer implements RecipeSerializer<MemoryWeavingRecipe> {
	public static HashMap<ResourceLocation, MemoryWeavingRecipe> ALL_RECIPES = new HashMap<>();

	public static MemoryWeavingRecipe getRecipe(String path) {
		return ALL_RECIPES.get(ResourceLocation.parse("hemomancy:memory_weaving/" + path));
	}

	@Override
	public MemoryWeavingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {

		Map<EnumBloodTendency, Float> tendency = MemoryWeavingRecipe.blank();
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			String key = tend.toString().toLowerCase();
			if (pJson.has(key)) {
				tendency.put(tend, pJson.get(key).getAsBoolean() ? 1f : 0f);
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
			ResourceLocation resourcelocation = ResourceLocation.parse(s1);
			result = new ItemStack(ForgeRegistries.ITEMS.getValue(resourcelocation), c);

		}

		if (pJson.has("ingredient")) {
			JsonElement jsonelement = GsonHelper.isArrayNode(pJson, "ingredient")
					? GsonHelper.getAsJsonArray(pJson, "ingredient")
					: GsonHelper.getAsJsonObject(pJson, "ingredient");
			Ingredient ingredient = Ingredient.fromJson(jsonelement);
			MemoryWeavingRecipe recipe = new MemoryWeavingRecipe(pRecipeId, ingredient, tendency, result);
			ALL_RECIPES.put(pRecipeId, recipe);
			return recipe;
		} else {
			Ingredient ingredient = Ingredient.EMPTY;
			MemoryWeavingRecipe recipe = new MemoryWeavingRecipe(pRecipeId, ingredient, tendency, result);
			ALL_RECIPES.put(pRecipeId, recipe);
			return recipe;
		}

	}

	@Override
	public MemoryWeavingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
		try {
			ResourceLocation id = pBuffer.readResourceLocation();
			Ingredient input = Ingredient.of(pBuffer.readItem());
			Map<EnumBloodTendency, Float> tends = new HashMap<>();
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				tends.put(tend, pBuffer.readBoolean() ? 1f : 0f);
			}
			ItemStack output = pBuffer.readItem();
			return new MemoryWeavingRecipe(id, input, tends, output);
		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error reading memory weaving recipe from packet.", e);
			throw e;
		}
	}

	@Override
	public void toNetwork(FriendlyByteBuf pBuffer, MemoryWeavingRecipe pRecipe) {
		try {
			pBuffer.writeResourceLocation(pRecipe.getId());
			if (pRecipe.getIngredients().get(0).getItems().length > 0) {
				pBuffer.writeItem(pRecipe.getIngredients().get(0).getItems()[0]);
			} else {
				pBuffer.writeItem(ItemStack.EMPTY);
			}
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				pBuffer.writeBoolean(pRecipe.getTendency().getOrDefault(tend, 0f) > 0f);
			}
			pBuffer.writeItemStack(pRecipe.getResultItem(null), false);
		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error writing memory weaving recipe to packet.", e);
			throw e;
		}
	}
}

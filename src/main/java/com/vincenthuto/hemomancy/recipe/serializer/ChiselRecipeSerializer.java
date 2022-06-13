package com.vincenthuto.hemomancy.recipe.serializer;

import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.rune.RuneType;
import com.vincenthuto.hemomancy.recipe.ChiselRecipe;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class ChiselRecipeSerializer implements RecipeSerializer<ChiselRecipe> {
	public static HashMap<ResourceLocation, ChiselRecipe> ALL_RECIPES = new HashMap<ResourceLocation, ChiselRecipe>();

	public static ChiselRecipe getRecipe(String path) {
		return ALL_RECIPES.get(new ResourceLocation("hemomancy:chisel/" + path));
	}

	@Override
	public ChiselRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {

		int tier = 0;
		RuneType runetype = RuneType.OVERRIDE;
		Ingredient ingredient1 = Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "ingredient1"));
		Ingredient ingredient2 = Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "ingredient2"));
		byte[][] pattern;

		if (pJson.has("tier")) {
			tier = pJson.get("tier").getAsInt();
		}
		if (pJson.has("runetype")) {
			runetype = pJson.get("runetype").getAsString().toUpperCase().equals(RuneType.CONTRACT.toString())
					? RuneType.CONTRACT
					: pJson.get("runetype").getAsString().toUpperCase().equals(RuneType.RUNE.toString()) ? RuneType.RUNE
							: RuneType.OVERRIDE;
		}
		JsonArray arr = pJson.getAsJsonArray("pattern");
		pattern = new byte[arr.size()][];
		for (int i = 0; i < arr.size(); ++i) {
			JsonElement elem = arr.get(i);
			if (!elem.isJsonArray())
				continue;
			JsonArray subArr = elem.getAsJsonArray();
			pattern[i] = new byte[subArr.size()];
			for (int j = 0; j < subArr.size(); ++j) {
				pattern[i][j] = subArr.get(j).getAsByte();
			}
		}

		ItemStack itemstack;
		if (pJson.get("result").isJsonObject())
			itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));
		else {
			int c = GsonHelper.getAsInt(pJson, "count");
			String s1 = GsonHelper.getAsString(pJson, "result");
			ResourceLocation resourcelocation = new ResourceLocation(s1);
			itemstack = new ItemStack(Registry.ITEM.getOptional(resourcelocation).orElseThrow(() -> {
				return new IllegalStateException("Item: " + s1 + " does not exist");
			}), c);
		}

		ChiselRecipe recipe = new ChiselRecipe(pRecipeId, tier, runetype, ingredient1, ingredient2, pattern, itemstack);
		ALL_RECIPES.put(pRecipeId, recipe);
		return recipe;
	}

	@Override
	public ChiselRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
		try {
			ResourceLocation id = pBuffer.readResourceLocation();
			Ingredient input1 = Ingredient.of(pBuffer.readItem());
			Ingredient input2 = Ingredient.of(pBuffer.readItem());
			int tier = pBuffer.readInt();
			RuneType runetype = RuneType.valueOf(pBuffer.readUtf());
			int len = pBuffer.readInt();
			byte[][] pattern = new byte[len][];
			for (int i = 0; i < len; ++i) {
				pattern[i] = pBuffer.readByteArray();
			}

			ItemStack result = pBuffer.readItem();
			ChiselRecipe recipe = new ChiselRecipe(id, tier, runetype, input1, input2, pattern, result);
			recipe.setPatternBytes(pattern);
			ALL_RECIPES.put(pRecipeId, recipe);
			return recipe;
		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error reading chisel pattern recipe from packet.", (Throwable) e);
			throw e;
		}
	}

	@Override
	public void toNetwork(FriendlyByteBuf pBuffer, ChiselRecipe pRecipe) {
		try {

			pBuffer.writeResourceLocation(pRecipe.getId());
			pBuffer.writeItem(pRecipe.getIngredient1().getItems()[0]);
			pBuffer.writeItem(pRecipe.getIngredient2().getItems()[0]);
			pBuffer.writeInt(pRecipe.getTier());
			pBuffer.writeUtf(pRecipe.getRuneType().toString());
			byte[][] pattern = pRecipe.getPattern();
			pBuffer.writeInt(pattern.length);
			for (int i = 0; i < pattern.length; ++i) {
				pBuffer.writeByteArray(pattern[i]);
			}
			pBuffer.writeItem(pRecipe.getResultItem());

		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error writing chisel pattern recipe to packet.", (Throwable) e);
			throw e;
		}
	}

}
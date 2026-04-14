package com.vincenthuto.hemomancy.common.recipe.serializer;

import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.scar.ScarType;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistries;

public class ScarRecipeSerializer implements RecipeSerializer<ScarRecipe> {
	public static HashMap<ResourceLocation, ScarRecipe> ALL_RECIPES = new HashMap<ResourceLocation, ScarRecipe>();

	public static ScarRecipe getRecipe(String path) {
		return ALL_RECIPES.get(new ResourceLocation("hemomancy:chisel/" + path));
	}

	@Override
	public ScarRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {

		int tier = 0;
		ScarType scarType = ScarType.OVERRIDE;
		Ingredient ingredient1 = Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "ingredient1"));
		Ingredient ingredient2 = Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "ingredient2"));
		byte[][] pattern;

		if (pJson.has("tier")) {
			tier = pJson.get("tier").getAsInt();
		}
		if (pJson.has("ScarType")) {
			scarType = ScarType.fromString(pJson.get("ScarType").getAsString());
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
			itemstack = new ItemStack(ForgeRegistries.ITEMS.getValue(resourcelocation), c);
		}

		ScarRecipe recipe = new ScarRecipe(pRecipeId, tier, scarType, ingredient1, ingredient2, pattern, itemstack);
		ALL_RECIPES.put(pRecipeId, recipe);
		return recipe;
	}

	@Override
	public ScarRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
		try {
			ResourceLocation id = pBuffer.readResourceLocation();
			Ingredient input1 = Ingredient.of(pBuffer.readItem());
			Ingredient input2 = Ingredient.of(pBuffer.readItem());
			int tier = pBuffer.readInt();
			ScarType scarType = ScarType.fromString(pBuffer.readUtf());
			int len = pBuffer.readInt();
			byte[][] pattern = new byte[len][];
			for (int i = 0; i < len; ++i) {
				pattern[i] = pBuffer.readByteArray();
			}

			ItemStack result = pBuffer.readItem();
			ScarRecipe recipe = new ScarRecipe(id, tier, scarType, input1, input2, pattern, result);
			recipe.setPatternBytes(pattern);
			ALL_RECIPES.put(pRecipeId, recipe);
			return recipe;
		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error reading chisel pattern recipe from packet.", (Throwable) e);
			throw e;
		}
	}

	@Override
	public void toNetwork(FriendlyByteBuf pBuffer, ScarRecipe pRecipe) {
		try {

			pBuffer.writeResourceLocation(pRecipe.getId());
			pBuffer.writeItem(pRecipe.getIngredient1().getItems()[0]);
			pBuffer.writeItem(pRecipe.getIngredient2().getItems()[0]);
			pBuffer.writeInt(pRecipe.getTier());
			pBuffer.writeUtf(pRecipe.getScarType().toString());
			byte[][] pattern = pRecipe.getPattern();
			pBuffer.writeInt(pattern.length);
			for (int i = 0; i < pattern.length; ++i) {
				pBuffer.writeByteArray(pattern[i]);
			}
			pBuffer.writeItem(pRecipe.getResultItem(null));

		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error writing chisel pattern recipe to packet.", (Throwable) e);
			throw e;
		}
	}

}
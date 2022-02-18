package com.vincenthuto.hemomancy.recipe.serializer;

import java.util.HashMap;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.rune.RuneType;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ChiselRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>>
		implements RecipeSerializer<ChiselRecipe> {
	public static HashMap<ResourceLocation, ChiselRecipe> ALL_RECIPES = new HashMap();

	@Override
	public ChiselRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
		Ingredient ingredient1 = Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "ingredient1"));
		Ingredient ingredient2 = Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "ingredient2"));

		int tier = 0;
		if (pJson.has("tier")) {
			tier = pJson.get("tier").getAsInt();
		}
		RuneType type = RuneType.OVERRIDE;
		if (pJson.has("runetype")) {
			type = pJson.get("runetype").getAsString().toUpperCase().equals(RuneType.CONTRACT.toString())
					? RuneType.CONTRACT
					: pJson.get("runetype").getAsString().toUpperCase().equals(RuneType.RUNE.toString()) ? RuneType.RUNE
							: RuneType.OVERRIDE;
		}
		byte[][] pattern;
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

		ChiselRecipe recipe = new ChiselRecipe(pRecipeId, tier, type, ingredient2, ingredient2, pattern, itemstack);
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
			RuneType type = RuneType.valueOf(pBuffer.readUtf().toUpperCase());
			int len = pBuffer.readInt();
			byte[][] pattern = new byte[len][];
			for (int i = 0; i < len; ++i) {
				pattern[i] = pBuffer.readByteArray();
			}
			ItemStack result = pBuffer.readItem();

			ChiselRecipe mwpattern = new ChiselRecipe(id, tier, type, input1, input2, pattern, result);
			mwpattern.setPatternBytes(pattern);
			ALL_RECIPES.put(pRecipeId, mwpattern);
			return mwpattern;
		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error reading chisel pattern recipe from packet.", (Throwable) e);
			throw e;
		}
	}

	@Override
	public void toNetwork(FriendlyByteBuf pBuffer, ChiselRecipe pRecipe) {
		try {

			pBuffer.writeResourceLocation(pRecipe.getId());
			pBuffer.writeUtf(pRecipe.getGroup());
			pBuffer.writeInt(pRecipe.getTier());
			pBuffer.writeUtf(pRecipe.type.toString().toUpperCase());
			pBuffer.writeItem(pRecipe.ingredient1.getItems()[0]);
			pBuffer.writeItem(pRecipe.ingredient2.getItems()[0]);
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
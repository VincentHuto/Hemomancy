package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

final class RecipeResultStackParser {
	private RecipeResultStackParser() {
	}

	static ItemStack parseResultStack(JsonObject recipeJson, String resultKey) {
		JsonElement resultElement = recipeJson.get(resultKey);
		if (resultElement == null || resultElement.isJsonNull()) {
			throw new JsonSyntaxException("Missing result field: " + resultKey);
		}

		if (resultElement.isJsonObject()) {
			JsonObject normalized = resultElement.getAsJsonObject().deepCopy();
			if (!normalized.has("id") && normalized.has("item")) {
				normalized.add("id", normalized.remove("item"));
			}
			return Codec.withAlternative(ItemStack.STRICT_CODEC, ItemStack.CODEC)
					.parse(JsonOps.INSTANCE, normalized)
					.getOrThrow(err -> new JsonSyntaxException("Invalid result item (supports id/item): " + err));
		}

		String resultId = GsonHelper.convertToString(resultElement, resultKey);
		int count = GsonHelper.getAsInt(recipeJson, "count", 1);
		ResourceLocation resultLoc = ResourceLocation.parse(resultId);
		return new ItemStack(BuiltInRegistries.ITEM.get(resultLoc), count);
	}
}


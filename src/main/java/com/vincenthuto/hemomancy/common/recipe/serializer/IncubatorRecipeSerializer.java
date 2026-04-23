package com.vincenthuto.hemomancy.common.recipe.serializer;

import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.IncubatorRecipe;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class IncubatorRecipeSerializer implements RecipeSerializer<IncubatorRecipe> {

	// ---- JSON helpers ----

	private static <T> JsonObject toJsonObject(DynamicOps<T> ops, MapLike<T> input) {
		JsonObject json = new JsonObject();
		input.entries().forEach(pair -> {
			String key = ops.getStringValue(pair.getFirst()).getOrThrow(IllegalStateException::new);
			JsonElement value = ops.convertTo(JsonOps.INSTANCE, pair.getSecond());
			json.add(key, value);
		});
		return json;
	}

	private static IncubatorRecipe fromJsonObject(ResourceLocation recipeId, JsonObject json) {
		JsonArray catalystsArray = GsonHelper.getAsJsonArray(json, "catalysts");
		NonNullList<Ingredient> catalysts = NonNullList.create();
		for (int i = 0; i < catalystsArray.size(); i++) {
			Ingredient ingredient = Ingredient.fromJson(catalystsArray.get(i), false);
			if (!ingredient.isEmpty()) catalysts.add(ingredient);
		}

		ItemStack result;
		if (json.get("result").isJsonObject()) {
			result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
		} else {
			String resultId = GsonHelper.getAsString(json, "result");
			int count = GsonHelper.getAsInt(json, "count", 1);
			ResourceLocation resultLoc = ResourceLocation.parse(resultId);
			result = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(resultLoc), count);
		}

		return new IncubatorRecipe(recipeId, catalysts, result);
	}

	// ---- RecipeSerializer 1.21.1 API ----

	private static final MapCodec<IncubatorRecipe> CODEC = new MapCodec<IncubatorRecipe>() {
		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of("id", "catalysts", "result", "count").map(ops::createString);
		}

		@Override
		public <T> DataResult<IncubatorRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
			try {
				JsonObject json = toJsonObject(ops, input);
				ResourceLocation id = json.has("id")
						? ResourceLocation.parse(json.get("id").getAsString())
						: Hemomancy.rloc("incubator/unknown");
				return DataResult.success(fromJsonObject(id, json));
			} catch (Exception e) {
				return DataResult.error(() -> "Failed to decode IncubatorRecipe: " + e.getMessage());
			}
		}

		@Override
		public <T> RecordBuilder<T> encode(IncubatorRecipe recipe, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			prefix.add("id", ops.createString(recipe.getId().toString()));
			JsonArray catalystsArr = new JsonArray();
			for (Ingredient catalyst : recipe.getCatalysts()) {
				Ingredient.CODEC_NONEMPTY.encodeStart(JsonOps.INSTANCE, catalyst).result().ifPresent(catalystsArr::add);
			}
			prefix.add("catalysts", ops.convertFrom(JsonOps.INSTANCE, catalystsArr));
			ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, recipe.getResultItemStack()).result()
					.ifPresent(e -> prefix.add("result", ops.convertFrom(JsonOps.INSTANCE, e)));
			return prefix;
		}
	};

	private static final StreamCodec<RegistryFriendlyByteBuf, IncubatorRecipe> STREAM_CODEC = StreamCodec.of(
			IncubatorRecipeSerializer::toNetwork,
			IncubatorRecipeSerializer::fromNetwork);

	@Override
	public MapCodec<IncubatorRecipe> codec() { return CODEC; }

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, IncubatorRecipe> streamCodec() { return STREAM_CODEC; }

	private static IncubatorRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
		try {
			ResourceLocation id = buffer.readResourceLocation();
			int catalystCount = buffer.readVarInt();
			NonNullList<Ingredient> catalysts = NonNullList.create();
			for (int i = 0; i < catalystCount; i++) {
				catalysts.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
			}
			ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
			return new IncubatorRecipe(id, catalysts, result);
		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error reading incubator recipe from packet.", e);
			throw e;
		}
	}

	private static void toNetwork(RegistryFriendlyByteBuf buffer, IncubatorRecipe recipe) {
		try {
			buffer.writeResourceLocation(recipe.getId());
			NonNullList<Ingredient> catalysts = recipe.getCatalysts();
			buffer.writeVarInt(catalysts.size());
			for (Ingredient catalyst : catalysts) {
				Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, catalyst);
			}
			ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItemStack());
		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error writing incubator recipe to packet.", e);
			throw e;
		}
	}
}

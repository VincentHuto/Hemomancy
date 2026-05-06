package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class MemoryWeavingRecipeSerializer implements RecipeSerializer<MemoryWeavingRecipe> {
	public static HashMap<ResourceLocation, MemoryWeavingRecipe> ALL_RECIPES = new HashMap<>();

	public static MemoryWeavingRecipe getRecipe(String path) {
		return ALL_RECIPES.get(ResourceLocation.parse("hemomancy:memory_weaving/" + path));
	}

	// ---- JSON helpers (reused by codec) ----

	private static <T> JsonObject toJsonObject(DynamicOps<T> ops, MapLike<T> input) {
		JsonObject json = new JsonObject();
		input.entries().forEach(pair -> {
			String key = ops.getStringValue(pair.getFirst()).getOrThrow(IllegalStateException::new);
			JsonElement value = ops.convertTo(JsonOps.INSTANCE, pair.getSecond());
			json.add(key, value);
		});
		return json;
	}

	private static MemoryWeavingRecipe fromJsonObject(ResourceLocation pRecipeId, JsonObject pJson) {
		Map<EnumBloodTendency, Float> tendency = MemoryWeavingRecipe.blank();
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			String key = tend.toString().toLowerCase();
			if (pJson.has(key)) {
				tendency.put(tend, pJson.get(key).getAsBoolean() ? 1f : 0f);
			} else {
				tendency.put(tend, 0f);
			}
		}

		ItemStack result = RecipeResultStackParser.parseResultStack(pJson, "result");

		// Validate result is not empty
		if (result.isEmpty()) {
			Hemomancy.LOGGER.warn("Memory weaving recipe {} has an empty result item. This recipe will be skipped.", pRecipeId);
			// Use a non-empty fallback so network sync cannot crash.
			result = new ItemStack(Items.BARRIER);
		}

		if (pJson.has("ingredient")) {
			JsonElement jsonelement = GsonHelper.isArrayNode(pJson, "ingredient")
					? GsonHelper.getAsJsonArray(pJson, "ingredient")
					: GsonHelper.getAsJsonObject(pJson, "ingredient");
			Ingredient ingredient = Ingredient.CODEC.parse(JsonOps.INSTANCE, jsonelement)
					.getOrThrow(err -> new JsonSyntaxException("Invalid ingredient: " + err));
			return new MemoryWeavingRecipe(pRecipeId, ingredient, tendency, result);
		} else {
			return new MemoryWeavingRecipe(pRecipeId, Ingredient.EMPTY, tendency, result);
		}
	}

	// ---- RecipeSerializer 1.21.1 API ----

	private static final MapCodec<MemoryWeavingRecipe> CODEC = new MapCodec<MemoryWeavingRecipe>() {
		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.concat(
					Stream.of("id", "ingredient", "result", "count"),
					Stream.of(EnumBloodTendency.values()).map(e -> e.toString().toLowerCase()))
					.map(ops::createString);
		}

		@Override
		public <T> DataResult<MemoryWeavingRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
			try {
				JsonObject json = toJsonObject(ops, input);
				ResourceLocation id = json.has("id")
						? ResourceLocation.parse(json.get("id").getAsString())
						: Hemomancy.rloc("memory_weaving/unknown");
				MemoryWeavingRecipe recipe = fromJsonObject(id, json);
				ALL_RECIPES.put(id, recipe);
				return DataResult.success(recipe);
			} catch (Exception e) {
				return DataResult.error(() -> "Failed to decode MemoryWeavingRecipe: " + e.getMessage());
			}
		}

		@Override
		public <T> RecordBuilder<T> encode(MemoryWeavingRecipe recipe, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			prefix.add("id", ops.createString(recipe.getId().toString()));
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				prefix.add(tend.toString().toLowerCase(), ops.createBoolean(recipe.isTendencyRequired(tend)));
			}
			Ingredient.CODEC_NONEMPTY.encodeStart(JsonOps.INSTANCE, recipe.getIngredient()).result()
					.ifPresent(e -> prefix.add("ingredient", JsonOps.INSTANCE.convertTo(ops, e)));
			ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, recipe.getResultItem(null)).result()
					.ifPresent(e -> prefix.add("result", JsonOps.INSTANCE.convertTo(ops, e)));
			return prefix;
		}
	};

	private static final StreamCodec<RegistryFriendlyByteBuf, MemoryWeavingRecipe> STREAM_CODEC = StreamCodec.of(
			MemoryWeavingRecipeSerializer::toNetwork,
			MemoryWeavingRecipeSerializer::fromNetwork);

	@Override
	public MapCodec<MemoryWeavingRecipe> codec() { return CODEC; }

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, MemoryWeavingRecipe> streamCodec() { return STREAM_CODEC; }

	private static MemoryWeavingRecipe fromNetwork(RegistryFriendlyByteBuf pBuffer) {
		try {
			ResourceLocation id = pBuffer.readResourceLocation();
			boolean hasIngredient = pBuffer.readBoolean();
			Ingredient input = hasIngredient
					? Ingredient.CONTENTS_STREAM_CODEC.decode(pBuffer)
					: Ingredient.EMPTY;
			Map<EnumBloodTendency, Float> tends = new HashMap<>();
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				tends.put(tend, pBuffer.readBoolean() ? 1f : 0f);
			}
			ItemStack output = ItemStack.STREAM_CODEC.decode(pBuffer);
			return new MemoryWeavingRecipe(id, input, tends, output);
		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error reading memory weaving recipe from packet.", e);
			throw e;
		}
	}

	private static void toNetwork(RegistryFriendlyByteBuf pBuffer, MemoryWeavingRecipe pRecipe) {
		try {
			pBuffer.writeResourceLocation(pRecipe.getId());
			Ingredient ingredient = pRecipe.getIngredient();
			boolean hasIngredient = ingredient != null && !ingredient.isEmpty();
			pBuffer.writeBoolean(hasIngredient);
			if (hasIngredient) {
				Ingredient.CONTENTS_STREAM_CODEC.encode(pBuffer, ingredient);
			}
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				pBuffer.writeBoolean(pRecipe.getTendency().getOrDefault(tend, 0f) > 0f);
			}
			// Validate result is not empty before encoding
			ItemStack result = pRecipe.getResultItem(null);
			if (result.isEmpty()) {
				Hemomancy.LOGGER.warn("Memory weaving recipe {} has an empty result item. Skipping network sync.", pRecipe.getId());
				// Write a non-empty sentinel stack to maintain protocol alignment.
				ItemStack.STREAM_CODEC.encode(pBuffer, new ItemStack(Items.BARRIER));
			} else {
				ItemStack.STREAM_CODEC.encode(pBuffer, result);
			}
		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error writing memory weaving recipe to packet.", e);
			throw e;
		}
	}
}

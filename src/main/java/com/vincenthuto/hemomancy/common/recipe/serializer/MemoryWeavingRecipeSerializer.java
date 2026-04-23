package com.vincenthuto.hemomancy.common.recipe.serializer;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

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

		ItemStack result;
		if (pJson.get("result").isJsonObject())
			result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));
		else {
			int c = GsonHelper.getAsInt(pJson, "count");
			String s1 = GsonHelper.getAsString(pJson, "result");
			ResourceLocation resourcelocation = ResourceLocation.parse(s1);
			result = new ItemStack(BuiltInRegistries.ITEM.get(resourcelocation), c);
		}

		if (pJson.has("ingredient")) {
			JsonElement jsonelement = GsonHelper.isArrayNode(pJson, "ingredient")
					? GsonHelper.getAsJsonArray(pJson, "ingredient")
					: GsonHelper.getAsJsonObject(pJson, "ingredient");
			Ingredient ingredient = Ingredient.fromJson(jsonelement, false);
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
					.ifPresent(e -> prefix.add("ingredient", ops.convertFrom(JsonOps.INSTANCE, e)));
			ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, recipe.getResultItem()).result()
					.ifPresent(e -> prefix.add("result", ops.convertFrom(JsonOps.INSTANCE, e)));
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
			Ingredient input = Ingredient.of(ItemStack.STREAM_CODEC.decode(pBuffer));
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
			if (pRecipe.getIngredients().get(0).getItems().length > 0) {
				ItemStack.STREAM_CODEC.encode(pBuffer, pRecipe.getIngredients().get(0).getItems()[0]);
			} else {
				ItemStack.STREAM_CODEC.encode(pBuffer, ItemStack.EMPTY);
			}
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				pBuffer.writeBoolean(pRecipe.getTendency().getOrDefault(tend, 0f) > 0f);
			}
			ItemStack.STREAM_CODEC.encode(pBuffer, pRecipe.getResultItem());
		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error writing memory weaving recipe to packet.", e);
			throw e;
		}
	}
}

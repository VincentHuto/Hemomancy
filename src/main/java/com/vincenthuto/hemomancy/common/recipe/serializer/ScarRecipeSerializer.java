package com.vincenthuto.hemomancy.common.recipe.serializer;

import java.util.HashMap;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.*;
import com.mojang.serialization.JsonOps;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.scar.ScarType;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ScarRecipeSerializer implements RecipeSerializer<ScarRecipe> {
	public static HashMap<ResourceLocation, ScarRecipe> ALL_RECIPES = new HashMap<ResourceLocation, ScarRecipe>();

	public static ScarRecipe getRecipe(String path) {
		// Current recipe IDs live under "hemomancy:scar/<path>".
		ScarRecipe recipe = ALL_RECIPES.get(ResourceLocation.parse("hemomancy:scar/" + path));
		if (recipe != null) {
			return recipe;
		}
		// Legacy fallbacks kept for migrated worlds / old naming.
		recipe = ALL_RECIPES.get(ResourceLocation.parse("hemomancy:chisel/" + path));
		if (recipe != null) {
			return recipe;
		}
		return ALL_RECIPES.get(ResourceLocation.parse("hemomancy:rune/" + path));
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

	private static ScarRecipe fromJsonObject(ResourceLocation id, JsonObject pJson) {
		int tier = 0;
		ScarType scarType = ScarType.OVERRIDE;
		Ingredient ingredient1 = Ingredient.CODEC_NONEMPTY
				.parse(JsonOps.INSTANCE, GsonHelper.getAsJsonObject(pJson, "ingredient1"))
				.getOrThrow(err -> new JsonSyntaxException("Invalid ingredient1: " + err));
		Ingredient ingredient2 = Ingredient.CODEC_NONEMPTY
				.parse(JsonOps.INSTANCE, GsonHelper.getAsJsonObject(pJson, "ingredient2"))
				.getOrThrow(err -> new JsonSyntaxException("Invalid ingredient2: " + err));
		byte[][] pattern;

		if (pJson.has("tier")) {
			tier = pJson.get("tier").getAsInt();
		}
		if (pJson.has("scartype")) {
			scarType = ScarType.fromString(pJson.get("scartype").getAsString());
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
			itemstack = Codec.withAlternative(ItemStack.STRICT_CODEC, ItemStack.CODEC)
					.parse(JsonOps.INSTANCE, GsonHelper.getAsJsonObject(pJson, "result"))
					.getOrThrow(err -> new JsonSyntaxException("Invalid result item: " + err));
		else {
			int c = GsonHelper.getAsInt(pJson, "count");
			String s1 = GsonHelper.getAsString(pJson, "result");
			ResourceLocation resourcelocation = ResourceLocation.parse(s1);
			itemstack = new ItemStack(BuiltInRegistries.ITEM.get(resourcelocation), c);
		}

		return new ScarRecipe(id, tier, scarType, ingredient1, ingredient2, pattern, itemstack);
	}

	private static <T> void encodeToBuilder(ScarRecipe recipe, DynamicOps<T> ops, RecordBuilder<T> prefix) {
		prefix.add("id", ops.createString(recipe.getId().toString()));
		// ingredient1/ingredient2
		Ingredient.CODEC_NONEMPTY.encodeStart(JsonOps.INSTANCE, recipe.getIngredient1()).result()
				.ifPresent(e -> prefix.add("ingredient1", JsonOps.INSTANCE.convertTo(ops, e)));
		Ingredient.CODEC_NONEMPTY.encodeStart(JsonOps.INSTANCE, recipe.getIngredient2()).result()
				.ifPresent(e -> prefix.add("ingredient2", JsonOps.INSTANCE.convertTo(ops, e)));
		prefix.add("tier", ops.createInt(recipe.getTier()));
		prefix.add("scartype", ops.createString(recipe.getScarType().toString()));
		// pattern
		byte[][] pat = recipe.getPattern();
		com.google.gson.JsonArray patArr = new com.google.gson.JsonArray();
		for (byte[] row : pat) {
			com.google.gson.JsonArray rowArr = new com.google.gson.JsonArray();
			for (byte b : row) rowArr.add(b);
			patArr.add(rowArr);
		}
		prefix.add("pattern", JsonOps.INSTANCE.convertTo(ops, patArr));
		// result
		ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, recipe.getResultItem()).result()
				.ifPresent(e -> prefix.add("result", JsonOps.INSTANCE.convertTo(ops, e)));
	}

	// ---- RecipeSerializer 1.21.1 API ----

	private static final MapCodec<ScarRecipe> CODEC = new MapCodec<ScarRecipe>() {
		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of("id", "ingredient1", "ingredient2", "tier", "scartype", "pattern", "result", "count")
					.map(ops::createString);
		}

		@Override
		public <T> DataResult<ScarRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
			try {
				JsonObject json = toJsonObject(ops, input);
				ResourceLocation id = json.has("id")
						? ResourceLocation.parse(json.get("id").getAsString())
						: Hemomancy.rloc("scar/unknown");
				ScarRecipe recipe = fromJsonObject(id, json);
				ALL_RECIPES.put(id, recipe);
				return DataResult.success(recipe);
			} catch (Exception e) {
				return DataResult.error(() -> "Failed to decode ScarRecipe: " + e.getMessage());
			}
		}

		@Override
		public <T> RecordBuilder<T> encode(ScarRecipe input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			encodeToBuilder(input, ops, prefix);
			return prefix;
		}
	};

	private static final StreamCodec<RegistryFriendlyByteBuf, ScarRecipe> STREAM_CODEC = StreamCodec.of(
			ScarRecipeSerializer::toNetwork,
			ScarRecipeSerializer::fromNetwork);

	@Override
	public MapCodec<ScarRecipe> codec() { return CODEC; }

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ScarRecipe> streamCodec() { return STREAM_CODEC; }

	private static ScarRecipe fromNetwork(RegistryFriendlyByteBuf pBuffer) {
		try {
			ResourceLocation id = pBuffer.readResourceLocation();
			Ingredient input1 = Ingredient.of(ItemStack.STREAM_CODEC.decode(pBuffer));
			Ingredient input2 = Ingredient.of(ItemStack.STREAM_CODEC.decode(pBuffer));
			int tier = pBuffer.readInt();
			ScarType scarType = ScarType.fromString(pBuffer.readUtf());
			int len = pBuffer.readInt();
			byte[][] pattern = new byte[len][];
			for (int i = 0; i < len; ++i) {
				pattern[i] = pBuffer.readByteArray();
			}
			ItemStack result = ItemStack.STREAM_CODEC.decode(pBuffer);
			ScarRecipe recipe = new ScarRecipe(id, tier, scarType, input1, input2, pattern, result);
			recipe.setPatternBytes(pattern);
			ALL_RECIPES.put(id, recipe);
			return recipe;
		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error reading scar recipe from packet.", (Throwable) e);
			throw e;
		}
	}

	private static void toNetwork(RegistryFriendlyByteBuf pBuffer, ScarRecipe pRecipe) {
		try {
			pBuffer.writeResourceLocation(pRecipe.getId());
			ItemStack.STREAM_CODEC.encode(pBuffer, pRecipe.getIngredient1().getItems().length > 0
					? pRecipe.getIngredient1().getItems()[0] : ItemStack.EMPTY);
			ItemStack.STREAM_CODEC.encode(pBuffer, pRecipe.getIngredient2().getItems().length > 0
					? pRecipe.getIngredient2().getItems()[0] : ItemStack.EMPTY);
			pBuffer.writeInt(pRecipe.getTier());
			pBuffer.writeUtf(pRecipe.getScarType().toString());
			byte[][] pattern = pRecipe.getPattern();
			pBuffer.writeInt(pattern.length);
			for (int i = 0; i < pattern.length; ++i) {
				pBuffer.writeByteArray(pattern[i]);
			}
			ItemStack.STREAM_CODEC.encode(pBuffer, pRecipe.getResultItem());
		} catch (Exception e) {
			Hemomancy.LOGGER.error("Error writing scar recipe to packet.", (Throwable) e);
			throw e;
		}
	}
}

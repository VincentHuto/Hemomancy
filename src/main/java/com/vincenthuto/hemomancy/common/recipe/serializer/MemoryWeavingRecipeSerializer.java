package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.*;
import java.util.stream.Stream;

public class MemoryWeavingRecipeSerializer implements RecipeSerializer<MemoryWeavingRecipe> {
	public static final HashMap<ResourceLocation, MemoryWeavingRecipe> ALL_RECIPES = new HashMap<>();

	public static MemoryWeavingRecipe getRecipe(String path) {
		if (path == null || path.isBlank()) {
			return null;
		}
		ResourceLocation direct = ResourceLocation.tryParse(path);
		if (direct != null && ALL_RECIPES.containsKey(direct)) {
			return ALL_RECIPES.get(direct);
		}
		String cleanPath = path;
		int colon = cleanPath.indexOf(':');
		if (colon >= 0) {
			cleanPath = cleanPath.substring(colon + 1);
		}
		return ALL_RECIPES.get(ResourceLocation.parse("hemomancy:memory_weaving/" + cleanPath));
	}

	private static <T> JsonObject toJsonObject(DynamicOps<T> ops, MapLike<T> input) {
		JsonObject json = new JsonObject();
		input.entries().forEach(pair -> {
			String key = ops.getStringValue(pair.getFirst()).getOrThrow(IllegalStateException::new);
			JsonElement value = ops.convertTo(JsonOps.INSTANCE, pair.getSecond());
			json.add(key, value);
		});
		return json;
	}

	private static MemoryWeavingRecipe fromJsonObject(ResourceLocation recipeId, JsonObject json) {
		List<Ingredient> catalysts = parseCatalysts(json);
		EnumMap<EnumBloodTendency, Integer> enzymes = parseEnzymes(json);
		double blood = json.has("blood")
				? Math.max(0.0D, GsonHelper.getAsDouble(json, "blood"))
				: legacyBloodCost(enzymes);

		ItemStack result = RecipeResultStackParser.parseResultStack(json, "result");
		if (result.isEmpty()) {
			Hemomancy.LOGGER.warn("Memory weaving recipe {} has an empty result item. This recipe will be skipped.", recipeId);
			result = new ItemStack(Items.BARRIER);
		}

		MemoryWeavingRecipe recipe = new MemoryWeavingRecipe(recipeId, catalysts, enzymes, blood, result);
		cacheRecipe(recipe);
		return recipe;
	}

	private static List<Ingredient> parseCatalysts(JsonObject json) {
		List<Ingredient> catalysts = new ArrayList<>();
		if (json.has("catalysts")) {
			JsonElement catalystElement = json.get("catalysts");
			if (catalystElement.isJsonArray()) {
				JsonArray array = catalystElement.getAsJsonArray();
				for (JsonElement element : array) {
					catalysts.add(parseIngredient(element));
				}
			} else {
				catalysts.add(parseIngredient(catalystElement));
			}
		} else if (json.has("ingredient")) {
			JsonElement legacy = GsonHelper.isArrayNode(json, "ingredient")
					? GsonHelper.getAsJsonArray(json, "ingredient")
					: GsonHelper.getAsJsonObject(json, "ingredient");
			catalysts.add(parseIngredient(legacy));
		}
		if (catalysts.isEmpty()) {
			throw new JsonSyntaxException("Memory weaving recipes require at least one catalyst");
		}
		return catalysts;
	}

	private static Ingredient parseIngredient(JsonElement element) {
		return Ingredient.CODEC.parse(JsonOps.INSTANCE, element)
				.getOrThrow(err -> new JsonSyntaxException("Invalid ingredient: " + err));
	}

	private static EnumMap<EnumBloodTendency, Integer> parseEnzymes(JsonObject json) {
		EnumMap<EnumBloodTendency, Integer> enzymes = MemoryWeavingRecipe.blankEnzymeRequirements();
		if (json.has("enzymes")) {
			JsonObject enzymeJson = GsonHelper.getAsJsonObject(json, "enzymes");
			for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
				String key = tendency.toString().toLowerCase();
				int amount = enzymeJson.has(key) ? GsonHelper.getAsInt(enzymeJson, key) : 0;
				validateEnzymeAmount(key, amount);
				enzymes.put(tendency, amount);
			}
			return enzymes;
		}

		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			String key = tendency.toString().toLowerCase();
			enzymes.put(tendency, json.has(key) && GsonHelper.getAsBoolean(json, key) ? 1 : 0);
		}
		return enzymes;
	}

	private static void validateEnzymeAmount(String key, int amount) {
		if (amount < 0 || amount > MemoryWeavingRecipe.MAX_ENZYMES_PER_TENDENCY) {
			throw new JsonSyntaxException("Memory weaving enzyme '" + key + "' must be between 0 and "
					+ MemoryWeavingRecipe.MAX_ENZYMES_PER_TENDENCY + ", got " + amount);
		}
	}

	private static double legacyBloodCost(Map<EnumBloodTendency, Integer> enzymes) {
		int requiredTendencies = 0;
		for (int amount : enzymes.values()) {
			if (amount > 0) {
				requiredTendencies++;
			}
		}
		return requiredTendencies * 50.0D;
	}

	private static void cacheRecipe(MemoryWeavingRecipe recipe) {
		ALL_RECIPES.put(recipe.getId(), recipe);
		ResourceLocation resultId = BuiltInRegistries.ITEM.getKey(recipe.getResultItem(null).getItem());
		if (resultId != null) {
			ALL_RECIPES.put(Hemomancy.rloc("memory_weaving/" + resultId.getPath()), recipe);
		}
	}

	private static final MapCodec<MemoryWeavingRecipe> CODEC = new MapCodec<>() {
		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.concat(
					Stream.of("id", "catalysts", "ingredient", "enzymes", "blood", "result", "count"),
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
				return DataResult.success(fromJsonObject(id, json));
			} catch (Exception e) {
				return DataResult.error(() -> "Failed to decode MemoryWeavingRecipe: " + e.getMessage());
			}
		}

		@Override
		public <T> RecordBuilder<T> encode(MemoryWeavingRecipe recipe, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			prefix.add("id", ops.createString(recipe.getId().toString()));
			JsonArray catalysts = new JsonArray();
			for (Ingredient ingredient : recipe.getCatalysts()) {
				Ingredient.CODEC_NONEMPTY.encodeStart(JsonOps.INSTANCE, ingredient).result()
						.ifPresent(catalysts::add);
			}
			prefix.add("catalysts", JsonOps.INSTANCE.convertTo(ops, catalysts));

			JsonObject enzymes = new JsonObject();
			for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
				enzymes.addProperty(tendency.toString().toLowerCase(), recipe.getEnzymeRequirement(tendency));
			}
			prefix.add("enzymes", JsonOps.INSTANCE.convertTo(ops, enzymes));
			prefix.add("blood", ops.createDouble(recipe.getBloodCost()));
			ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, recipe.getResultItem(null)).result()
					.ifPresent(e -> prefix.add("result", JsonOps.INSTANCE.convertTo(ops, e)));
			return prefix;
		}
	};

	private static final StreamCodec<RegistryFriendlyByteBuf, MemoryWeavingRecipe> STREAM_CODEC = StreamCodec.of(
			MemoryWeavingRecipeSerializer::toNetwork,
			MemoryWeavingRecipeSerializer::fromNetwork);

	@Override
	public MapCodec<MemoryWeavingRecipe> codec() {
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, MemoryWeavingRecipe> streamCodec() {
		return STREAM_CODEC;
	}

	private static MemoryWeavingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
		ResourceLocation id = buffer.readResourceLocation();
		int catalystCount = buffer.readVarInt();
		List<Ingredient> catalysts = new ArrayList<>(catalystCount);
		for (int i = 0; i < catalystCount; i++) {
			catalysts.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
		}
		EnumMap<EnumBloodTendency, Integer> enzymes = MemoryWeavingRecipe.blankEnzymeRequirements();
		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			enzymes.put(tendency, buffer.readVarInt());
		}
		double blood = buffer.readDouble();
		ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
		MemoryWeavingRecipe recipe = new MemoryWeavingRecipe(id, catalysts, enzymes, blood, output);
		cacheRecipe(recipe);
		return recipe;
	}

	private static void toNetwork(RegistryFriendlyByteBuf buffer, MemoryWeavingRecipe recipe) {
		buffer.writeResourceLocation(recipe.getId());
		buffer.writeVarInt(recipe.getCatalysts().size());
		for (Ingredient catalyst : recipe.getCatalysts()) {
			Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, catalyst);
		}
		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			buffer.writeVarInt(recipe.getEnzymeRequirement(tendency));
		}
		buffer.writeDouble(recipe.getBloodCost());
		ItemStack result = recipe.getResultItem(null);
		ItemStack.STREAM_CODEC.encode(buffer, result.isEmpty() ? new ItemStack(Items.BARRIER) : result);
	}
}

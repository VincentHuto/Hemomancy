package com.vincenthuto.hemomancy.common.recipe.serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hutoslib.math.MultiblockPattern;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;

public class CardinalRiteRecipeSerializer implements RecipeSerializer<CardinalRiteRecipe> {
	private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private static Block blockFromJson(JsonObject pItemObject) {
		String s = GsonHelper.getAsString(pItemObject, "block");
		Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(s));
		if (block == Blocks.AIR) throw new JsonSyntaxException("Invalid block: " + s);
		return block;
	}

	private static Predicate<BlockInWorld> blockPredFromHash(Map<String, Block> symbolList, String string) {
		Block target = symbolList.get(string);
		return BlockInWorld.hasState(state -> state.getBlock() == target);
	}

	private static BlockPattern generateBlockPatternFromArray(Map<String, Block> symbolList, String[][] schematic) {
		BlockPatternBuilder builder = BlockPatternBuilder.start();
		for (String[] element : schematic) {
			builder.aisle(element);
			for (String element3 : element) {
				List<String> distinct = getDistinctChars(element3);
				for (String element2 : distinct) {
					if (" ".equals(element2)) continue;
					builder.where(element2.toCharArray()[0], blockPredFromHash(symbolList, element2));
				}
			}
		}
		return builder.build();
	}

	private static List<String> getDistinctChars(String chars) {
		List<String> distinct = new ArrayList<>();
		for (int i = 0; i < chars.length(); i++) {
			if (!distinct.contains(String.valueOf(chars.charAt(i)))) {
				distinct.add(String.valueOf(chars.charAt(i)));
			}
		}
		return distinct;
	}

	private static Map<String, Block> keyFromJson(JsonObject pKeyEntry) {
		Map<String, Block> map = Maps.newHashMap();
		for (Entry<String, JsonElement> entry : pKeyEntry.entrySet()) {
			if (entry.getKey().length() != 1)
				throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' must be 1 character.");
			if (" ".equals(entry.getKey()))
				throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
			map.put(entry.getKey(), blockFromJson(entry.getValue().getAsJsonObject()));
		}
		map.put(" ", Blocks.AIR);
		return map;
	}

	private static String[][] patternFromJson(JsonArray pPatternArray) {
		List<String[]> pattern = new ArrayList<>();
		for (int i = 0; i < pPatternArray.size(); i++) {
			String[] row = GSON.fromJson(pPatternArray.get(i), String[].class);
			pattern.add(row);
		}
		return pattern.toArray(new String[0][]);
	}

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

	private static CardinalRiteRecipe fromJsonObject(ResourceLocation pRecipeId, JsonObject pJson) {
		double cost = GsonHelper.getAsFloat(pJson, "bloodCost");
		String riteTypeName = GsonHelper.getAsString(pJson, "riteType");
		CardinalRiteType riteType = CardinalRiteType.byName(riteTypeName);
		String riteName = GsonHelper.getAsString(pJson, "riteName", "");
		String riteDescription = GsonHelper.getAsString(pJson, "riteDescription", "");
		String[][] pattern = patternFromJson(GsonHelper.getAsJsonArray(pJson, "pattern"));
		Map<String, Block> keyMap = keyFromJson(GsonHelper.getAsJsonObject(pJson, "key"));
		ItemStack result = ItemStack.EMPTY;
		if (pJson.has("result")) {
			result = RecipeResultStackParser.parseResultStack(pJson, "result");
		}
		BlockPattern bp = generateBlockPatternFromArray(keyMap, pattern);
		MultiblockPattern mbPattern = new MultiblockPattern(bp, keyMap, pattern);
		int requiredDegree = GsonHelper.getAsInt(pJson, "requiredDegree", -1);
		boolean breakBlocksOnCreation = GsonHelper.getAsBoolean(pJson, "breakBlocksOnCreation", true);
		boolean unstained = GsonHelper.getAsBoolean(pJson, "unstained", false);
		return new CardinalRiteRecipe(pRecipeId, cost, riteType, mbPattern, result, riteName,
				riteDescription, requiredDegree, breakBlocksOnCreation, unstained);
	}

	// ---- RecipeSerializer 1.21.1 API ----

	private static final MapCodec<CardinalRiteRecipe> CODEC = new MapCodec<CardinalRiteRecipe>() {
		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of("id", "bloodCost", "riteType", "riteName", "riteDescription", "pattern", "key",
					"result", "requiredDegree", "breakBlocksOnCreation", "unstained").map(ops::createString);
		}

		@Override
		public <T> DataResult<CardinalRiteRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
			try {
				JsonObject json = toJsonObject(ops, input);
				ResourceLocation id = json.has("id")
						? ResourceLocation.parse(json.get("id").getAsString())
						: Hemomancy.rloc("cardinal_rite/unknown");
				return DataResult.success(fromJsonObject(id, json));
			} catch (Exception e) {
				return DataResult.error(() -> "Failed to decode CardinalRiteRecipe: " + e.getMessage());
			}
		}

		@Override
		public <T> RecordBuilder<T> encode(CardinalRiteRecipe recipe, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			prefix.add("id", ops.createString(recipe.getId().toString()));
			prefix.add("bloodCost", ops.createDouble(recipe.getBloodCost()));
			prefix.add("riteType", ops.createString(recipe.getRiteType().getSerializedName()));
			prefix.add("riteName", ops.createString(recipe.getRiteName()));
			prefix.add("riteDescription", ops.createString(recipe.getRiteDescription()));
			// pattern / key are complex — handled via stream codec
			ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, recipe.getResult()).result()
					.ifPresent(e -> prefix.add("result", JsonOps.INSTANCE.convertTo(ops, e)));
			prefix.add("requiredDegree", ops.createInt(recipe.getRequiredDegree()));
			prefix.add("breakBlocksOnCreation", ops.createBoolean(recipe.shouldBreakBlocksOnCreation()));
			prefix.add("unstained", ops.createBoolean(recipe.isUnstained()));
			return prefix;
		}
	};

	private static final StreamCodec<RegistryFriendlyByteBuf, CardinalRiteRecipe> STREAM_CODEC = StreamCodec.of(
			CardinalRiteRecipeSerializer::toNetwork,
			CardinalRiteRecipeSerializer::fromNetwork);

	@Override
	public MapCodec<CardinalRiteRecipe> codec() { return CODEC; }

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, CardinalRiteRecipe> streamCodec() { return STREAM_CODEC; }

	private static CardinalRiteRecipe fromNetwork(RegistryFriendlyByteBuf pBuffer) {
		ResourceLocation id = pBuffer.readResourceLocation();
		double cost = pBuffer.readDouble();
		CardinalRiteType riteType = CardinalRiteType.byName(pBuffer.readUtf());
		String riteName = pBuffer.readUtf();
		String riteDescription = pBuffer.readUtf();
		int length = pBuffer.readInt();
		List<String[]> patternList = new ArrayList<>();
		for (int i = 0; i < length; i++) {
			int width = pBuffer.readInt();
			String[] row = new String[width];
			for (int j = 0; j < width; j++) row[j] = pBuffer.readUtf();
			patternList.add(row);
		}
		String[][] pattern = patternList.toArray(new String[0][]);
		Map<String, Block> map = Maps.newHashMap();
		int symbolListLength = pBuffer.readInt();
		for (int i = 0; i < symbolListLength; i++) {
			String key = pBuffer.readUtf();
			Block block = BuiltInRegistries.BLOCK.get(pBuffer.readResourceLocation());
			map.put(key, block);
		}
		BlockPattern bp = generateBlockPatternFromArray(map, pattern);
		MultiblockPattern mbPattern = new MultiblockPattern(bp, map, pattern);
		boolean hasResult = pBuffer.readBoolean();
		ItemStack result = hasResult ? ItemStack.STREAM_CODEC.decode(pBuffer) : ItemStack.EMPTY;
		int requiredDegree = pBuffer.readInt();
		boolean breakBlocksOnCreation = pBuffer.readBoolean();
		boolean unstained = pBuffer.readBoolean();
		return new CardinalRiteRecipe(id, cost, riteType, mbPattern, result, riteName,
				riteDescription, requiredDegree, breakBlocksOnCreation, unstained);
	}

	private static void toNetwork(RegistryFriendlyByteBuf pBuffer, CardinalRiteRecipe pRecipe) {
		pBuffer.writeResourceLocation(pRecipe.getId());
		pBuffer.writeDouble(pRecipe.getBloodCost());
		pBuffer.writeUtf(pRecipe.getRiteType().getSerializedName());
		pBuffer.writeUtf(pRecipe.getRiteName());
		pBuffer.writeUtf(pRecipe.getRiteDescription());
		pBuffer.writeInt(pRecipe.getPattern().getPatternArray().length);
		for (String[] row : pRecipe.getPattern().getPatternArray()) {
			pBuffer.writeInt(row.length);
			for (String cell : row) pBuffer.writeUtf(cell);
		}
		pBuffer.writeInt(pRecipe.getPattern().getSymbolList().size());
		pRecipe.getPattern().getSymbolList().forEach((k, v) -> {
			pBuffer.writeUtf(k);
			pBuffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(v));
		});
		ItemStack result = pRecipe.getResult();
		boolean hasResult = result != null && !result.isEmpty();
		pBuffer.writeBoolean(hasResult);
		if (hasResult) {
			ItemStack.STREAM_CODEC.encode(pBuffer, result);
		}
		pBuffer.writeInt(pRecipe.getRequiredDegree());
		pBuffer.writeBoolean(pRecipe.shouldBreakBlocksOnCreation());
		pBuffer.writeBoolean(pRecipe.isUnstained());
	}
}

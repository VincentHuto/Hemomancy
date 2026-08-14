package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import com.vincenthuto.hutoslib.math.MultiblockPatternKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class CardinalRitePatternJson {
	private static final Gson GSON = new Gson();

	private CardinalRitePatternJson() {
	}

	public static MultiblockPattern parse(JsonObject owner) {
		String[][] pattern = patternFromJson(owner.getAsJsonArray("pattern"));
		Map<String, PatternKeyEntry> keys = keysFromJson(owner.getAsJsonObject("key"));
		validateSymbols(pattern, keys);
		BlockPattern blockPattern = generateBlockPattern(keys, pattern);
		Map<String, MultiblockPatternKey> displayKeys = Maps.newHashMap();
		keys.forEach((key, entry) -> displayKeys.put(key, entry.toMultiblockPatternKey()));
		displayKeys.put(" ", MultiblockPatternKey.block(" ", Blocks.AIR));
		return new MultiblockPattern(blockPattern, displayKeys, pattern, true);
	}

	private static Map<String, PatternKeyEntry> keysFromJson(JsonObject object) {
		if (object == null) throw new JsonSyntaxException("Missing pattern key");
		Map<String, PatternKeyEntry> keys = Maps.newHashMap();
		for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
			String symbol = entry.getKey();
			if (symbol.length() != 1 || " ".equals(symbol)) {
				throw new JsonSyntaxException("Pattern key must be one non-space character: '" + symbol + "'");
			}
			keys.put(symbol, PatternKeyEntry.fromJson(symbol, entry.getValue().getAsJsonObject()));
		}
		return keys;
	}

	private static String[][] patternFromJson(JsonArray array) {
		if (array == null || array.isEmpty()) throw new JsonSyntaxException("Pattern cannot be empty");
		List<String[]> aisles = new ArrayList<>();
		for (JsonElement element : array) {
			String[] aisle = GSON.fromJson(element, String[].class);
			if (aisle.length == 0) throw new JsonSyntaxException("Pattern aisle cannot be empty");
			aisles.add(aisle);
		}
		return aisles.toArray(String[][]::new);
	}

	private static void validateSymbols(String[][] pattern, Map<String, PatternKeyEntry> keys) {
		int height = pattern[0].length;
		int width = pattern[0][0].length();
		for (String[] aisle : pattern) {
			if (aisle.length != height) throw new JsonSyntaxException("Pattern aisles must share a height");
			for (String row : aisle) {
				if (row.length() != width) throw new JsonSyntaxException("Pattern rows must share a width");
				for (int index = 0; index < row.length(); index++) {
					String symbol = String.valueOf(row.charAt(index));
					if (!" ".equals(symbol) && !keys.containsKey(symbol)) {
						throw new JsonSyntaxException("Pattern uses undefined key '" + symbol + "'");
					}
				}
			}
		}
	}

	private static BlockPattern generateBlockPattern(Map<String, PatternKeyEntry> keys, String[][] pattern) {
		BlockPatternBuilder builder = BlockPatternBuilder.start();
		for (String[] aisle : pattern) {
			builder.aisle(aisle);
			for (String row : aisle) {
				for (int index = 0; index < row.length(); index++) {
					String symbol = String.valueOf(row.charAt(index));
					if (" ".equals(symbol)) continue;
					Predicate<BlockInWorld> predicate = keys.get(symbol)::matches;
					builder.where(symbol.charAt(0), predicate);
				}
			}
		}
		return builder.build();
	}
}

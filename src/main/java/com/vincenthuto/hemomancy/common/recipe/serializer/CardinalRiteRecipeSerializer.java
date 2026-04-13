package com.vincenthuto.hemomancy.common.recipe.serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hutoslib.math.MultiblockPattern;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraftforge.registries.ForgeRegistries;

public class CardinalRiteRecipeSerializer implements RecipeSerializer<CardinalRiteRecipe> {
	private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private static Block blockFromJson(JsonObject pItemObject) {
		String s = GsonHelper.getAsString(pItemObject, "block");
		Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(s));

		if (block == Blocks.AIR) {
			throw new JsonSyntaxException("Invalid block: " + s);
		} else {
			return block;
		}
	}

	private static Predicate<BlockInWorld> blockPredFromHash(Map<String, Block> symbolList, String string) {
		Block target = symbolList.get(string);
		// Only match the block type, ignoring block state properties (facing, half, shape, etc.)
		// BlockStatePredicate.forBlock() matches ALL properties against the default state,
		// which causes blocks like stairs, gourds, and engrams to fail matching when placed
		// in the world with non-default state values.
		return BlockInWorld.hasState(state -> state.getBlock() == target);
	}

	// Structure Helpers
	private static BlockPattern generateBlockPatternFromArray(Map<String, Block> symbolList, String[][] schematic) {
		BlockPatternBuilder builder = BlockPatternBuilder.start();
		for (String[] element : schematic) {
			builder.aisle(element);
			for (String element3 : element) {
				List<String> distinct = getDistinctChars(element3);
				for (String element2 : distinct) {
					// Skip the space character — leave it as the default "any block" wildcard.
					// Without this, spaces are bound to Blocks.AIR which forces every empty
					// position in the structure to be exactly minecraft:air. Blocks like
					// cave_air, attached stems, grass, water, etc. would cause pattern failure.
					if (" ".equals(element2)) continue;
					builder.where(element2.toCharArray()[0], blockPredFromHash(symbolList, element2));
				}
			}
		}
		BlockPattern pattern = builder.build();
		return pattern;
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
			if (entry.getKey().length() != 1) {
				throw new JsonSyntaxException(
						"Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
			}
			if (" ".equals(entry.getKey())) {
				throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
			}

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
		String[][] matrix = new String[pattern.size()][];
		matrix = pattern.toArray(matrix);

		return matrix;
	}

	// Serialization
	@Override
	public CardinalRiteRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
		// Deserialization
		float cost = GsonHelper.getAsFloat(pJson, "bloodCost");
		String riteTypeName = GsonHelper.getAsString(pJson, "riteType");
		CardinalRiteType riteType = CardinalRiteType.byName(riteTypeName);
		String riteName = GsonHelper.getAsString(pJson, "riteName", "");
		String riteDescription = GsonHelper.getAsString(pJson, "riteDescription", "");
		String[][] pattern = patternFromJson(GsonHelper.getAsJsonArray(pJson, "pattern"));
		Map<String, Block> keyMap = keyFromJson(GsonHelper.getAsJsonObject(pJson, "key"));
		ItemStack result = ItemStack.EMPTY;
		if (pJson.has("result")) {
			result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));
		}
		// Building
		BlockPattern bp = generateBlockPatternFromArray(keyMap, pattern);
		MultiblockPattern mbPattern = new MultiblockPattern(bp, keyMap, pattern);
		int requiredDegree = GsonHelper.getAsInt(pJson, "requiredDegree", -1);
		CardinalRiteRecipe recipe = new CardinalRiteRecipe(pRecipeId, cost, riteType, mbPattern, result, riteName,
				riteDescription, requiredDegree);

		return recipe;
	}

	@Override
	public CardinalRiteRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
		float cost = pBuffer.readFloat();
		String riteTypeName = pBuffer.readUtf();
		CardinalRiteType riteType = CardinalRiteType.byName(riteTypeName);
		String riteName = pBuffer.readUtf();
		String riteDescription = pBuffer.readUtf();
		// Pattern reading
		int length = pBuffer.readInt();
		List<String[]> patternList = new ArrayList<>();
		for (int i = 0; i < length; i++) {
			List<String> row = new ArrayList<>();
			int width = pBuffer.readInt();
			for (int j = 0; j < width; j++) {
				row.add(pBuffer.readUtf());
			}
			String[] rowArray = new String[row.size()];
			rowArray = row.toArray(rowArray);
			patternList.add(rowArray);
		}
		String[][] pattern = new String[patternList.size()][];
		pattern = patternList.toArray(pattern);
		// Reading symbol key
		Map<String, Block> map = Maps.newHashMap();
		int symbolListLength = pBuffer.readInt();
		for (int i = 0; i < symbolListLength; i++) {
			String key = pBuffer.readUtf();
			ResourceLocation blockLoc = pBuffer.readResourceLocation();
			Block block = ForgeRegistries.BLOCKS.getValue(blockLoc);

			map.put(key, block);
		}
		BlockPattern bp = generateBlockPatternFromArray(map, pattern);
		MultiblockPattern mbPattern = new MultiblockPattern(bp, map, pattern);
		ItemStack result = pBuffer.readItem();
		int requiredDegree = pBuffer.readInt();

		CardinalRiteRecipe recipe = new CardinalRiteRecipe(pRecipeId, cost, riteType, mbPattern, result, riteName,
				riteDescription, requiredDegree);

		return recipe;
	}

	@Override
	public void toNetwork(FriendlyByteBuf pBuffer, CardinalRiteRecipe pRecipe) {
		pBuffer.writeFloat(pRecipe.getBloodCost());
		pBuffer.writeUtf(pRecipe.getRiteType().getSerializedName());
		pBuffer.writeUtf(pRecipe.getRiteName());
		pBuffer.writeUtf(pRecipe.getRiteDescription());
		// Pattern writing
		pBuffer.writeInt(pRecipe.getPattern().getPatternArray().length);
		for (int i = 0; i < pRecipe.getPattern().getPatternArray().length; i++) {
			pBuffer.writeInt(pRecipe.getPattern().getPatternArray()[i].length);
			for (int j = 0; j < pRecipe.getPattern().getPatternArray()[i].length; j++) {
				pBuffer.writeUtf(pRecipe.getPattern().getPatternArray()[i][j]);
			}
		}
		// Writing symbol key
		pBuffer.writeInt(pRecipe.getPattern().getSymbolList().size());
		pRecipe.getPattern().getSymbolList().forEach((k, v) -> {
			pBuffer.writeUtf(k);
			pBuffer.writeResourceLocation(ForgeRegistries.BLOCKS.getKey(v));
		});
		pBuffer.writeItem(pRecipe.getResult());
		pBuffer.writeInt(pRecipe.getRequiredDegree());
	}
}

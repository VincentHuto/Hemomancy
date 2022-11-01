package com.vincenthuto.hemomancy.recipe.serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.vincenthuto.hemomancy.recipe.BloodStructureRecipe;
import com.vincenthuto.hutoslib.client.render.block.MultiblockPattern;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraftforge.registries.ForgeRegistries;

public class BloodStructureRecipeSerializer implements RecipeSerializer<BloodStructureRecipe> {
	private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	// Serialization
	@Override
	public BloodStructureRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
		// Deserialization
		float cost = GsonHelper.getAsFloat(pJson, "bloodCost");
		ItemStack heldItem = getItemFromJson(GsonHelper.getAsString(pJson, "heldItem"));
		Block hitBlock = blockFromString(GsonHelper.getAsString(pJson, "hitBlock"));
		String[][] pattern = patternFromJson(GsonHelper.getAsJsonArray(pJson, "pattern"));
		Map<String, Block> keyMap = keyFromJson(GsonHelper.getAsJsonObject(pJson, "key"));
		ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));
		// Building
		BlockPattern bp = generateBlockPatternFromArray(keyMap, pattern);
		MultiblockPattern mbPattern = new MultiblockPattern(bp, keyMap, pattern);
		BloodStructureRecipe recipe = new BloodStructureRecipe(pRecipeId, cost, mbPattern, heldItem, hitBlock, result);

		return recipe;
	}

	@Override
	public BloodStructureRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
		float cost = pBuffer.readFloat();
		ItemStack heldItem = pBuffer.readItem();
		Block hitBlock = Block.byItem(pBuffer.readItem().getItem());
		// Pattern reading
		int length = pBuffer.readInt();
		List<String[]> patternList = new ArrayList<String[]>();
		for (int i = 0; i < length; i++) {
			List<String> row = new ArrayList<String>();
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
			Block block = Registry.BLOCK.getOptional(blockLoc).orElseThrow(() -> {
				return new JsonSyntaxException("Unknown block '" + blockLoc + "'");
			});
			map.put(key, block);
		}
		BlockPattern bp = generateBlockPatternFromArray(map, pattern);
		MultiblockPattern mbPattern = new MultiblockPattern(bp, map, pattern);
		ItemStack result = pBuffer.readItem();

		BloodStructureRecipe recipe = new BloodStructureRecipe(pRecipeId, cost, mbPattern, heldItem, hitBlock, result);

		return recipe;
	}

	@Override
	public void toNetwork(FriendlyByteBuf pBuffer, BloodStructureRecipe pRecipe) {
		pBuffer.writeFloat(pRecipe.getBloodCost());
		pBuffer.writeItem(pRecipe.getHeldItem());
		pBuffer.writeItem(new ItemStack(pRecipe.getHitBlock().asItem()));
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

	}
	// Json Helpers

	private static ItemStack getItemFromJson(String itemName) {
		ResourceLocation itemKey = new ResourceLocation(itemName);
		if (!ForgeRegistries.ITEMS.containsKey(itemKey))
			throw new JsonSyntaxException("Unknown item '" + itemName + "'");
		Item item = ForgeRegistries.ITEMS.getValue(itemKey);
		return new ItemStack(Objects.requireNonNull(item));
	}

	private static Block blockFromString(String s) {
		Block block = Registry.BLOCK.getOptional(new ResourceLocation(s)).orElseThrow(() -> {
			return new JsonSyntaxException("Unknown block '" + s + "'");
		});
		if (block == Blocks.AIR) {
			throw new JsonSyntaxException("Invalid block: " + s);
		} else {
			return block;
		}
	}

	private static Block blockFromJson(JsonObject pItemObject) {
		String s = GsonHelper.getAsString(pItemObject, "block");
		Block block = Registry.BLOCK.getOptional(new ResourceLocation(s)).orElseThrow(() -> {
			return new JsonSyntaxException("Unknown block '" + s + "'");
		});
		if (block == Blocks.AIR) {
			throw new JsonSyntaxException("Invalid block: " + s);
		} else {
			return block;
		}
	}

	private static Map<String, Block> keyFromJson(JsonObject pKeyEntry) {
		Map<String, Block> map = Maps.newHashMap();
		for (Entry<String, JsonElement> entry : pKeyEntry.entrySet()) {
			if (entry.getKey().length() != 1) {
				throw new JsonSyntaxException("Invalid key entry: '" + (String) entry.getKey()
						+ "' is an invalid symbol (must be 1 character only).");
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
		List<String[]> pattern = new ArrayList<String[]>();
		for (int i = 0; i < pPatternArray.size(); i++) {
			String[] row = GSON.fromJson(pPatternArray.get(i), String[].class);
			pattern.add(row);
		}
		String[][] matrix = new String[pattern.size()][];
		matrix = pattern.toArray(matrix);

		return matrix;
	}

	// Structure Helpers
	private static BlockPattern generateBlockPatternFromArray(Map<String, Block> symbolList, String[][] schematic) {
		BlockPatternBuilder builder = null;
		if (builder == null) {
			builder = BlockPatternBuilder.start();
			for (int aisle = 0; aisle < schematic.length; aisle++) {
				builder.aisle(schematic[aisle]);
				for (int z = 0; z < schematic[aisle].length; z++) {
					List<String> distinct = getDistinctChars(schematic[aisle][z]);
					for (int c = 0; c < distinct.size(); c++) {
						builder.where(distinct.get(c).toCharArray()[0], blockPredFromHash(symbolList, distinct.get(c)));
					}
				}
			}
		}
		BlockPattern pattern = builder.build();
		return pattern;
	}

	private static Predicate<BlockInWorld> blockPredFromHash(Map<String, Block> symbolList, String string) {
		return (BlockInWorld.hasState(BlockStatePredicate.forBlock(symbolList.get(string))));
	}

	private static List<String> getDistinctChars(String chars) {
		List<String> distinct = new ArrayList<String>();
		for (int i = 0; i < chars.length(); i++) {
			if (!distinct.contains(String.valueOf(chars.charAt(i)))) {
				distinct.add(String.valueOf(chars.charAt(i)));
			}
		}
		return distinct;

	}
}

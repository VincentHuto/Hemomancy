package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.serialization.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import com.vincenthuto.hutoslib.math.MultiblockPatternKey;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

	private static Predicate<BlockInWorld> keyEntryPredFromHash(Map<String, PatternKeyEntry> symbolList, String string) {
		PatternKeyEntry entry = symbolList.get(string);
		return entry::matches;
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

	private static BlockPattern generateBlockPatternFromKeyEntries(Map<String, PatternKeyEntry> symbolList,
			String[][] schematic) {
		BlockPatternBuilder builder = BlockPatternBuilder.start();
		for (String[] element : schematic) {
			builder.aisle(element);
			for (String element3 : element) {
				List<String> distinct = getDistinctChars(element3);
				for (String element2 : distinct) {
					if (" ".equals(element2)) continue;
					builder.where(element2.toCharArray()[0], keyEntryPredFromHash(symbolList, element2));
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

	private static Map<String, PatternKeyEntry> keyEntriesFromJson(JsonObject pKeyEntry) {
		Map<String, PatternKeyEntry> map = Maps.newHashMap();
		for (Entry<String, JsonElement> entry : pKeyEntry.entrySet()) {
			if (entry.getKey().length() != 1)
				throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' must be 1 character.");
			if (" ".equals(entry.getKey()))
				throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
			map.put(entry.getKey(), PatternKeyEntry.fromJson(entry.getKey(), entry.getValue().getAsJsonObject()));
		}
		return map;
	}

	private static Map<String, MultiblockPatternKey> displayKeyMapFromKeyEntries(Map<String, PatternKeyEntry> keyEntries) {
		Map<String, MultiblockPatternKey> map = Maps.newHashMap();
		keyEntries.forEach((key, entry) -> map.put(key, entry.toMultiblockPatternKey()));
		map.put(" ", MultiblockPatternKey.block(" ", Blocks.AIR));
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

	private static int requiredDegreeFromJson(JsonObject pJson) {
		if (pJson.has("required_degree")) {
			return GsonHelper.getAsInt(pJson, "required_degree", 0);
		}
		return GsonHelper.getAsInt(pJson, "requiredDegree", 0);
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
		Map<String, PatternKeyEntry> keyEntries = keyEntriesFromJson(GsonHelper.getAsJsonObject(pJson, "key"));
		ItemStack result = ItemStack.EMPTY;
		if (pJson.has("result")) {
			result = RecipeResultStackParser.parseResultStack(pJson, "result");
		}
		BlockPattern bp = generateBlockPatternFromKeyEntries(keyEntries, pattern);
		MultiblockPattern mbPattern = new MultiblockPattern(bp, displayKeyMapFromKeyEntries(keyEntries), pattern, true);
		int requiredDegree = requiredDegreeFromJson(pJson);
		boolean breakBlocksOnCreation = GsonHelper.getAsBoolean(pJson, "breakBlocksOnCreation", true);
		boolean unstained = GsonHelper.getAsBoolean(pJson, "unstained", false);
		boolean rankup = GsonHelper.getAsBoolean(pJson, "rankup", false);
		CardinalRiteRecipe recipe = new CardinalRiteRecipe(pRecipeId, cost, riteType, mbPattern, result, riteName,
				riteDescription, requiredDegree, breakBlocksOnCreation, unstained, rankup);
		recipe.setRequiredPurity(GsonHelper.getAsFloat(pJson, "required_purity", -1.0f));
		recipe.setRequiredClarity(GsonHelper.getAsFloat(pJson, "required_clarity", -1.0f));
		return recipe;
	}

	// ---- RecipeSerializer 1.21.1 API ----

	private static final MapCodec<CardinalRiteRecipe> CODEC = new MapCodec<CardinalRiteRecipe>() {
		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of("id", "bloodCost", "riteType", "riteName", "riteDescription", "pattern", "key",
					"result", "required_degree", "required_purity", "required_clarity",
					"breakBlocksOnCreation", "unstained", "rankup").map(ops::createString);
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
			prefix.add("required_degree", ops.createInt(recipe.getRequiredDegree()));
			if (recipe.getRequiredPurity() >= 0.0f) prefix.add("required_purity", ops.createFloat(recipe.getRequiredPurity()));
			if (recipe.getRequiredClarity() >= 0.0f) prefix.add("required_clarity", ops.createFloat(recipe.getRequiredClarity()));
			prefix.add("breakBlocksOnCreation", ops.createBoolean(recipe.shouldBreakBlocksOnCreation()));
			prefix.add("unstained", ops.createBoolean(recipe.isUnstained()));
			prefix.add("rankup", ops.createBoolean(recipe.isRankup()));
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
		Map<String, MultiblockPatternKey> displayKeys = Maps.newHashMap();
		int symbolListLength = pBuffer.readInt();
		for (int i = 0; i < symbolListLength; i++) {
			String key = pBuffer.readUtf();
			boolean tagged = pBuffer.readBoolean();
			Block fallback = BuiltInRegistries.BLOCK.get(pBuffer.readResourceLocation());
			ResourceLocation tagId = tagged ? pBuffer.readResourceLocation() : null;
			List<Block> displayBlocks = readDisplayBlocks(pBuffer);
			map.put(key, fallback);
			if (tagged) {
				displayKeys.put(key, MultiblockPatternKey.tag(key, tagId, fallback, displayBlocks));
			} else {
				displayKeys.put(key, MultiblockPatternKey.block(key, fallback));
			}
		}
		BlockPattern bp = generateBlockPatternFromArray(map, pattern);
		MultiblockPattern mbPattern = new MultiblockPattern(bp, displayKeys, pattern, true);
		boolean hasResult = pBuffer.readBoolean();
		ItemStack result = hasResult ? ItemStack.STREAM_CODEC.decode(pBuffer) : ItemStack.EMPTY;
		int requiredDegree = pBuffer.readInt();
		float requiredPurity = pBuffer.readFloat();
		float requiredClarity = pBuffer.readFloat();
		boolean breakBlocksOnCreation = pBuffer.readBoolean();
		boolean unstained = pBuffer.readBoolean();
		boolean rankup = pBuffer.readBoolean();
		CardinalRiteRecipe recipe = new CardinalRiteRecipe(id, cost, riteType, mbPattern, result, riteName,
				riteDescription, requiredDegree, breakBlocksOnCreation, unstained, rankup);
		recipe.setRequiredPurity(requiredPurity);
		recipe.setRequiredClarity(requiredClarity);
		return recipe;
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
		pBuffer.writeInt(pRecipe.getPattern().getKeyList().size());
		pRecipe.getPattern().getKeyList().forEach((k, v) -> {
			pBuffer.writeUtf(k);
			pBuffer.writeBoolean(v.isTag());
			pBuffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(v.fallbackBlock()));
			if (v.isTag()) {
				pBuffer.writeResourceLocation(v.tagId());
			}
			writeDisplayBlocks(pBuffer, displayBlocksForNetwork(pBuffer, v));
		});
		ItemStack result = pRecipe.getResult();
		boolean hasResult = result != null && !result.isEmpty();
		pBuffer.writeBoolean(hasResult);
		if (hasResult) {
			ItemStack.STREAM_CODEC.encode(pBuffer, result);
		}
		pBuffer.writeInt(pRecipe.getRequiredDegree());
		pBuffer.writeFloat(pRecipe.getRequiredPurity());
		pBuffer.writeFloat(pRecipe.getRequiredClarity());
		pBuffer.writeBoolean(pRecipe.shouldBreakBlocksOnCreation());
		pBuffer.writeBoolean(pRecipe.isUnstained());
		pBuffer.writeBoolean(pRecipe.isRankup());
	}

	private static List<Block> readDisplayBlocks(RegistryFriendlyByteBuf pBuffer) {
		int displayBlockCount = pBuffer.readInt();
		List<Block> displayBlocks = new ArrayList<>();
		for (int i = 0; i < displayBlockCount; i++) {
			Block block = BuiltInRegistries.BLOCK.get(pBuffer.readResourceLocation());
			if (block != Blocks.AIR) {
				displayBlocks.add(block);
			}
		}
		return displayBlocks;
	}

	private static void writeDisplayBlocks(RegistryFriendlyByteBuf pBuffer, List<Block> displayBlocks) {
		pBuffer.writeInt(displayBlocks.size());
		for (Block block : displayBlocks) {
			pBuffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(block));
		}
	}

	private static List<Block> displayBlocksForNetwork(RegistryFriendlyByteBuf pBuffer, MultiblockPatternKey key) {
		if (!key.isTag()) {
			return key.displayBlocks();
		}
		Registry<Block> blockRegistry = pBuffer.registryAccess().registryOrThrow(Registries.BLOCK);
		TagKey<Block> tag = TagKey.create(Registries.BLOCK, key.tagId());
		return blockRegistry.getTag(tag)
				.map(named -> {
					List<Block> blocks = new ArrayList<>();
					for (Holder<Block> holder : named) {
						Block block = holder.value();
						if (block != Blocks.AIR) {
							blocks.add(block);
						}
					}
					return blocks;
				})
				.filter(blocks -> !blocks.isEmpty())
				.orElseGet(key::displayBlocks);
	}
}

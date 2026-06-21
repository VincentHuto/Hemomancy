package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.serialization.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureOffering;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
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
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BloodStructureRecipeSerializer implements RecipeSerializer<BloodStructureRecipe> {
	private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private static Block blockFromJson(JsonObject pItemObject) {
		String s = GsonHelper.getAsString(pItemObject, "block");
		Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(s));
		if (block == Blocks.AIR) throw new JsonSyntaxException("Invalid block: " + s);
		return block;
	}

	private static Block blockFromString(String s) {
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
		BlockPatternBuilder builder = null;
		if (builder == null) {
			builder = BlockPatternBuilder.start();
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

	private static ItemStack getItemFromJson(String itemName) {
		ResourceLocation itemKey = ResourceLocation.parse(itemName);
		if (!BuiltInRegistries.ITEM.containsKey(itemKey))
			throw new JsonSyntaxException("Unknown item '" + itemName + "'");
		Item item = BuiltInRegistries.ITEM.get(itemKey);
		return new ItemStack(Objects.requireNonNull(item));
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

	private static List<BloodStructureOffering> offeringsFromJson(JsonObject json) {
		if (!json.has("offerings")) {
			return List.of();
		}
		JsonArray array = GsonHelper.getAsJsonArray(json, "offerings");
		List<BloodStructureOffering> offerings = new ArrayList<>();
		for (int i = 0; i < array.size(); i++) {
			JsonObject entry = GsonHelper.convertToJsonObject(array.get(i), "offerings[" + i + "]");
			Ingredient ingredient = Ingredient.CODEC_NONEMPTY.parse(JsonOps.INSTANCE,
					GsonHelper.getNonNull(entry, "ingredient"))
					.getOrThrow(err -> new JsonSyntaxException("Invalid blood structure offering ingredient: " + err));
			int count = GsonHelper.getAsInt(entry, "count", 1);
			offerings.add(new BloodStructureOffering(ingredient, count));
		}
		return offerings;
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

	private static BloodStructureRecipe fromJsonObject(ResourceLocation pRecipeId, JsonObject pJson) {
		double cost = GsonHelper.getAsFloat(pJson, "bloodCost");
		ItemStack heldItem = getItemFromJson(GsonHelper.getAsString(pJson, "heldItem"));
		Block hitBlock = blockFromString(GsonHelper.getAsString(pJson, "hitBlock"));
		String[][] pattern = patternFromJson(GsonHelper.getAsJsonArray(pJson, "pattern"));
		Map<String, PatternKeyEntry> keyEntries = keyEntriesFromJson(GsonHelper.getAsJsonObject(pJson, "key"));
		ItemStack result = RecipeResultStackParser.parseResultStack(pJson, "result");
		BlockPattern bp = generateBlockPatternFromKeyEntries(keyEntries, pattern);
		MultiblockPattern mbPattern = new MultiblockPattern(bp, displayKeyMapFromKeyEntries(keyEntries), pattern, true);
		boolean unstained = GsonHelper.getAsBoolean(pJson, "unstained", false);
		int requiredDegree = requiredDegreeFromJson(pJson);
		List<BloodStructureOffering> offerings = offeringsFromJson(pJson);
		return new BloodStructureRecipe(pRecipeId, cost, mbPattern, heldItem, hitBlock, result, unstained, requiredDegree,
				offerings);
	}

	// ---- RecipeSerializer 1.21.1 API ----

	private static final MapCodec<BloodStructureRecipe> CODEC = new MapCodec<BloodStructureRecipe>() {
		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of("id", "bloodCost", "heldItem", "hitBlock", "pattern", "key", "result", "unstained", "required_degree", "offerings")
					.map(ops::createString);
		}

		@Override
		public <T> DataResult<BloodStructureRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
			try {
				JsonObject json = toJsonObject(ops, input);
				ResourceLocation id = json.has("id")
						? ResourceLocation.parse(json.get("id").getAsString())
						: Hemomancy.rloc("blood_structure/unknown");
				return DataResult.success(fromJsonObject(id, json));
			} catch (Exception e) {
				return DataResult.error(() -> "Failed to decode BloodStructureRecipe: " + e.getMessage());
			}
		}

		@Override
		public <T> RecordBuilder<T> encode(BloodStructureRecipe recipe, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			prefix.add("id", ops.createString(recipe.getId().toString()));
			prefix.add("bloodCost", ops.createDouble(recipe.getBloodCost()));
			ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, recipe.getHeldItem()).result()
					.ifPresent(e -> prefix.add("heldItem", JsonOps.INSTANCE.convertTo(ops, e)));
			prefix.add("hitBlock", ops.createString(BuiltInRegistries.BLOCK.getKey(recipe.getHitBlock()).toString()));
			// pattern / key are complex — skip encode for now (server-to-client via stream codec)
			ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, recipe.getResult()).result()
					.ifPresent(e -> prefix.add("result", JsonOps.INSTANCE.convertTo(ops, e)));
			prefix.add("unstained", ops.createBoolean(recipe.isUnstained()));
			prefix.add("required_degree", ops.createInt(recipe.getRequiredDegree()));
			JsonArray offerings = new JsonArray();
			for (BloodStructureOffering offering : recipe.getOfferings()) {
				JsonObject offeringJson = new JsonObject();
				Ingredient.CODEC_NONEMPTY.encodeStart(JsonOps.INSTANCE, offering.ingredient()).result()
						.ifPresent(element -> offeringJson.add("ingredient", element));
				offeringJson.addProperty("count", offering.count());
				offerings.add(offeringJson);
			}
			prefix.add("offerings", JsonOps.INSTANCE.convertTo(ops, offerings));
			return prefix;
		}
	};

	private static final StreamCodec<RegistryFriendlyByteBuf, BloodStructureRecipe> STREAM_CODEC = StreamCodec.of(
			BloodStructureRecipeSerializer::toNetwork,
			BloodStructureRecipeSerializer::fromNetwork);

	@Override
	public MapCodec<BloodStructureRecipe> codec() { return CODEC; }

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, BloodStructureRecipe> streamCodec() { return STREAM_CODEC; }

	private static BloodStructureRecipe fromNetwork(RegistryFriendlyByteBuf pBuffer) {
		ResourceLocation id = pBuffer.readResourceLocation();
		double cost = pBuffer.readDouble();
		ItemStack heldItem = ItemStack.STREAM_CODEC.decode(pBuffer);
		Block hitBlock = BuiltInRegistries.BLOCK.get(pBuffer.readResourceLocation());
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
		boolean unstained = pBuffer.readBoolean();
		int requiredDegree = pBuffer.readInt();
		int offeringCount = pBuffer.readVarInt();
		List<BloodStructureOffering> offerings = new ArrayList<>();
		for (int i = 0; i < offeringCount; i++) {
			Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(pBuffer);
			int count = pBuffer.readVarInt();
			offerings.add(new BloodStructureOffering(ingredient, count));
		}
		return new BloodStructureRecipe(id, cost, mbPattern, heldItem, hitBlock, result, unstained, requiredDegree,
				offerings);
	}

	private static void toNetwork(RegistryFriendlyByteBuf pBuffer, BloodStructureRecipe pRecipe) {
		pBuffer.writeResourceLocation(pRecipe.getId());
		pBuffer.writeDouble(pRecipe.getBloodCost());
		ItemStack.STREAM_CODEC.encode(pBuffer, pRecipe.getHeldItem());
		pBuffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(pRecipe.getHitBlock()));
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
		pBuffer.writeBoolean(pRecipe.isUnstained());
		pBuffer.writeInt(pRecipe.getRequiredDegree());
		pBuffer.writeVarInt(pRecipe.getOfferings().size());
		for (BloodStructureOffering offering : pRecipe.getOfferings()) {
			Ingredient.CONTENTS_STREAM_CODEC.encode(pBuffer, offering.ingredient());
			pBuffer.writeVarInt(offering.count());
		}
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

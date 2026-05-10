package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.serialization.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.PuppeteerTrialRecipe;
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

public class PuppeteerTrialRecipeSerializer implements RecipeSerializer<PuppeteerTrialRecipe> {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

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

	private static ItemStack getItemFromJson(String itemName) {
		ResourceLocation itemKey = ResourceLocation.parse(itemName);
		if (!BuiltInRegistries.ITEM.containsKey(itemKey)) {
			throw new JsonSyntaxException("Unknown item '" + itemName + "'");
		}
		Item item = BuiltInRegistries.ITEM.get(itemKey);
		return new ItemStack(Objects.requireNonNull(item));
	}

	private static Block blockFromString(String id) {
		Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));
		if (block == Blocks.AIR) {
			throw new JsonSyntaxException("Invalid block: " + id);
		}
		return block;
	}

	private static Map<String, PatternKeyEntry> keyEntriesFromJson(JsonObject keyEntry) {
		Map<String, PatternKeyEntry> map = Maps.newHashMap();
		for (Entry<String, JsonElement> entry : keyEntry.entrySet()) {
			if (entry.getKey().length() != 1) {
				throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' must be 1 character.");
			}
			if (" ".equals(entry.getKey())) {
				throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
			}
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

	private static String[][] patternFromJson(JsonArray patternArray) {
		List<String[]> pattern = new ArrayList<>();
		for (int i = 0; i < patternArray.size(); i++) {
			String[] row = GSON.fromJson(patternArray.get(i), String[].class);
			pattern.add(row);
		}
		return pattern.toArray(new String[0][]);
	}

	private static int requiredDegreeFromJson(JsonObject json) {
		if (json.has("required_degree")) {
			return GsonHelper.getAsInt(json, "required_degree", 0);
		}
		return GsonHelper.getAsInt(json, "requiredDegree", 0);
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

	private static PuppeteerTrialRecipe fromJsonObject(ResourceLocation recipeId, JsonObject json) {
		double cost = GsonHelper.getAsFloat(json, "bloodCost");
		String summon = GsonHelper.getAsString(json, "summon");
		ItemStack heldItem = getItemFromJson(GsonHelper.getAsString(json, "heldItem"));
		Block hitBlock = blockFromString(GsonHelper.getAsString(json, "hitBlock"));
		String[][] pattern = patternFromJson(GsonHelper.getAsJsonArray(json, "pattern"));
		Map<String, PatternKeyEntry> keyEntries = keyEntriesFromJson(GsonHelper.getAsJsonObject(json, "key"));
		BlockPattern bp = generateBlockPatternFromKeyEntries(keyEntries, pattern);
		MultiblockPattern mbPattern = new MultiblockPattern(bp, displayKeyMapFromKeyEntries(keyEntries), pattern, true);
		boolean consumePattern = GsonHelper.getAsBoolean(json, "consume_pattern",
				GsonHelper.getAsBoolean(json, "consumePattern", true));
		int requiredDegree = requiredDegreeFromJson(json);
		return new PuppeteerTrialRecipe(recipeId, cost, mbPattern, heldItem, hitBlock,
				summon, consumePattern, requiredDegree);
	}

	private static final MapCodec<PuppeteerTrialRecipe> CODEC = new MapCodec<PuppeteerTrialRecipe>() {
		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of("id", "bloodCost", "summon", "heldItem", "hitBlock", "pattern", "key",
					"consume_pattern", "required_degree").map(ops::createString);
		}

		@Override
		public <T> DataResult<PuppeteerTrialRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
			try {
				JsonObject json = toJsonObject(ops, input);
				ResourceLocation id = json.has("id")
						? ResourceLocation.parse(json.get("id").getAsString())
						: Hemomancy.rloc("puppeteer_trial/unknown");
				return DataResult.success(fromJsonObject(id, json));
			} catch (Exception e) {
				return DataResult.error(() -> "Failed to decode PuppeteerTrialRecipe: " + e.getMessage());
			}
		}

		@Override
		public <T> RecordBuilder<T> encode(PuppeteerTrialRecipe recipe, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			prefix.add("id", ops.createString(recipe.getId().toString()));
			prefix.add("bloodCost", ops.createDouble(recipe.getBloodCost()));
			prefix.add("summon", ops.createString(recipe.getSummonName()));
			ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, recipe.getHeldItem()).result()
					.ifPresent(e -> prefix.add("heldItem", JsonOps.INSTANCE.convertTo(ops, e)));
			prefix.add("hitBlock", ops.createString(BuiltInRegistries.BLOCK.getKey(recipe.getHitBlock()).toString()));
			prefix.add("consume_pattern", ops.createBoolean(recipe.shouldConsumePattern()));
			prefix.add("required_degree", ops.createInt(recipe.getRequiredDegree()));
			return prefix;
		}
	};

	private static final StreamCodec<RegistryFriendlyByteBuf, PuppeteerTrialRecipe> STREAM_CODEC = StreamCodec.of(
			PuppeteerTrialRecipeSerializer::toNetwork,
			PuppeteerTrialRecipeSerializer::fromNetwork);

	@Override
	public MapCodec<PuppeteerTrialRecipe> codec() {
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, PuppeteerTrialRecipe> streamCodec() {
		return STREAM_CODEC;
	}

	private static PuppeteerTrialRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
		ResourceLocation id = buffer.readResourceLocation();
		double cost = buffer.readDouble();
		String summon = buffer.readUtf();
		ItemStack heldItem = ItemStack.STREAM_CODEC.decode(buffer);
		Block hitBlock = BuiltInRegistries.BLOCK.get(buffer.readResourceLocation());
		int length = buffer.readInt();
		List<String[]> patternList = new ArrayList<>();
		for (int i = 0; i < length; i++) {
			int width = buffer.readInt();
			String[] row = new String[width];
			for (int j = 0; j < width; j++) {
				row[j] = buffer.readUtf();
			}
			patternList.add(row);
		}
		String[][] pattern = patternList.toArray(new String[0][]);
		Map<String, Block> map = Maps.newHashMap();
		Map<String, MultiblockPatternKey> displayKeys = Maps.newHashMap();
		int symbolListLength = buffer.readInt();
		for (int i = 0; i < symbolListLength; i++) {
			String key = buffer.readUtf();
			boolean tagged = buffer.readBoolean();
			Block fallback = BuiltInRegistries.BLOCK.get(buffer.readResourceLocation());
			ResourceLocation tagId = tagged ? buffer.readResourceLocation() : null;
			List<Block> displayBlocks = readDisplayBlocks(buffer);
			map.put(key, fallback);
			if (tagged) {
				displayKeys.put(key, MultiblockPatternKey.tag(key, tagId, fallback, displayBlocks));
			} else {
				displayKeys.put(key, MultiblockPatternKey.block(key, fallback));
			}
		}
		BlockPattern bp = generateBlockPatternFromArray(map, pattern);
		MultiblockPattern mbPattern = new MultiblockPattern(bp, displayKeys, pattern, true);
		boolean consumePattern = buffer.readBoolean();
		int requiredDegree = buffer.readInt();
		return new PuppeteerTrialRecipe(id, cost, mbPattern, heldItem, hitBlock,
				summon, consumePattern, requiredDegree);
	}

	private static void toNetwork(RegistryFriendlyByteBuf buffer, PuppeteerTrialRecipe recipe) {
		buffer.writeResourceLocation(recipe.getId());
		buffer.writeDouble(recipe.getBloodCost());
		buffer.writeUtf(recipe.getSummonName());
		ItemStack.STREAM_CODEC.encode(buffer, recipe.getHeldItem());
		buffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(recipe.getHitBlock()));
		buffer.writeInt(recipe.getPattern().getPatternArray().length);
		for (String[] row : recipe.getPattern().getPatternArray()) {
			buffer.writeInt(row.length);
			for (String cell : row) {
				buffer.writeUtf(cell);
			}
		}
		buffer.writeInt(recipe.getPattern().getKeyList().size());
		recipe.getPattern().getKeyList().forEach((key, value) -> {
			buffer.writeUtf(key);
			buffer.writeBoolean(value.isTag());
			buffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(value.fallbackBlock()));
			if (value.isTag()) {
				buffer.writeResourceLocation(value.tagId());
			}
			writeDisplayBlocks(buffer, displayBlocksForNetwork(buffer, value));
		});
		buffer.writeBoolean(recipe.shouldConsumePattern());
		buffer.writeInt(recipe.getRequiredDegree());
	}

	private static List<Block> readDisplayBlocks(RegistryFriendlyByteBuf buffer) {
		int displayBlockCount = buffer.readInt();
		List<Block> displayBlocks = new ArrayList<>();
		for (int i = 0; i < displayBlockCount; i++) {
			Block block = BuiltInRegistries.BLOCK.get(buffer.readResourceLocation());
			if (block != Blocks.AIR) {
				displayBlocks.add(block);
			}
		}
		return displayBlocks;
	}

	private static void writeDisplayBlocks(RegistryFriendlyByteBuf buffer, List<Block> displayBlocks) {
		buffer.writeInt(displayBlocks.size());
		for (Block block : displayBlocks) {
			buffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(block));
		}
	}

	private static List<Block> displayBlocksForNetwork(RegistryFriendlyByteBuf buffer, MultiblockPatternKey key) {
		if (!key.isTag()) {
			return key.displayBlocks();
		}
		Registry<Block> blockRegistry = buffer.registryAccess().registryOrThrow(Registries.BLOCK);
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

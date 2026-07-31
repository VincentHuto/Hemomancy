package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.serialization.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyCatalog;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyProfile;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteRingTuning;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteOfferingConsumptionRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteProgressionPolicy;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import com.vincenthuto.hutoslib.math.MultiblockPatternKey;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
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

	private static CardinalRiteCeremonyDefinition ceremonyFromJson(JsonObject json, ResourceLocation recipeId,
			CardinalRiteType riteType, int degree) {
		if (!json.has("ceremony")) {
			throw new JsonSyntaxException("Harbinger cardinal rite " + recipeId + " is missing required ceremony data");
		}
		JsonObject ceremony = GsonHelper.getAsJsonObject(json, "ceremony");
		CardinalRiteCeremonyProfile profile = CardinalRiteCeremonyProfile.byName(
				GsonHelper.getAsString(ceremony, "profile"));
		List<CardinalRiteCeremonyDefinition.Anchor> anchors = new ArrayList<>();
		if (ceremony.has("anchors")) {
			for (JsonElement element : ceremony.getAsJsonArray("anchors")) {
				JsonObject anchor = element.getAsJsonObject();
				int ring = GsonHelper.getAsInt(anchor, "ring", 0);
				int order = GsonHelper.getAsInt(anchor, "order", anchors.size());
				anchors.add(CardinalRiteRingTuning.anchor(
						ring, order, GsonHelper.getAsInt(anchor, "y", 1)));
			}
		} else if (ceremony.has("layout")) {
			CardinalRiteCeremonyCatalog.Layout layout = CardinalRiteCeremonyCatalog.Layout.byName(
					GsonHelper.getAsString(ceremony, "layout", "cardinal"));
			int rotation = GsonHelper.getAsInt(ceremony, "rotation", 0);
			anchors.addAll(CardinalRiteCeremonyDefinition.anchorsForLayout(degree, rotation, layout));
		} else {
			throw new JsonSyntaxException("Harbinger cardinal rite " + recipeId
					+ " ceremony must declare anchors or layout");
		}
		requireCeremonyField(ceremony, recipeId, "support_sockets");
		requireCeremonyField(ceremony, recipeId, "waves");
		requireCeremonyField(ceremony, recipeId, "signature");
		requireCeremonyField(ceremony, recipeId, "fragile_offsets");
		List<CardinalRiteCeremonyDefinition.SupportSocket> sockets = new ArrayList<>();
		for (JsonElement element : ceremony.getAsJsonArray("support_sockets")) {
			JsonObject socket = element.getAsJsonObject();
			sockets.add(new CardinalRiteCeremonyDefinition.SupportSocket(
					GsonHelper.getAsInt(socket, "x"), GsonHelper.getAsInt(socket, "y", 0),
					GsonHelper.getAsInt(socket, "z"),
					GsonHelper.getAsString(socket, "suggested_sigil", ""),
					GsonHelper.getAsBoolean(socket, "required")));
		}
		List<String> waves = stringList(ceremony, "waves");
		List<String> guaranteed = stringList(ceremony, "guaranteed_waves");
		String handler = GsonHelper.getAsString(ceremony, "signature");
		List<BlockPos> fragile = new ArrayList<>();
		for (JsonElement element : ceremony.getAsJsonArray("fragile_offsets")) {
			JsonArray offset = element.getAsJsonArray();
			if (offset.size() != 3) {
				throw new JsonSyntaxException("Harbinger cardinal rite " + recipeId
						+ " has a fragile offset that is not [x,y,z]");
			}
			fragile.add(new BlockPos(offset.get(0).getAsInt(), offset.get(1).getAsInt(),
					offset.get(2).getAsInt()));
		}
		requireCeremonyField(ceremony, recipeId, "target_duration_ticks");
		requireCeremonyField(ceremony, recipeId, "focus");
		requireCeremonyField(ceremony, recipeId, "required_helpers");
		requireCeremonyField(ceremony, recipeId, "helper_roles");
		requireCeremonyField(ceremony, recipeId, "still_interval_ticks");
		requireCeremonyField(ceremony, recipeId, "atmosphere");
		requireCeremonyField(ceremony, recipeId, "failure");
		JsonObject atmosphere = ceremony.getAsJsonObject("atmosphere");
		requireCeremonyField(atmosphere, recipeId, "fog");
		requireCeremonyField(atmosphere, recipeId, "lightning");
		requireCeremonyField(atmosphere, recipeId, "dome");
		return new CardinalRiteCeremonyDefinition(profile, anchors, sockets, waves, guaranteed, handler, fragile,
				GsonHelper.getAsInt(ceremony, "target_duration_ticks"),
				GsonHelper.getAsString(ceremony, "focus"),
				GsonHelper.getAsInt(ceremony, "required_helpers"),
				stringList(ceremony, "helper_roles"),
				GsonHelper.getAsInt(ceremony, "still_interval_ticks"),
				new CardinalRiteCeremonyDefinition.Atmosphere(
						GsonHelper.getAsString(atmosphere, "fog"),
						GsonHelper.getAsBoolean(atmosphere, "lightning"),
						GsonHelper.getAsBoolean(atmosphere, "dome")),
				GsonHelper.getAsString(ceremony, "failure"));
	}

	private static void requireCeremonyField(JsonObject ceremony, ResourceLocation recipeId, String field) {
		if (!ceremony.has(field)) {
			throw new JsonSyntaxException("Harbinger cardinal rite " + recipeId
					+ " ceremony is missing required field " + field);
		}
	}

	private static List<String> stringList(JsonObject json, String key) {
		if (!json.has(key)) return List.of();
		List<String> values = new ArrayList<>();
		for (JsonElement element : json.getAsJsonArray(key)) values.add(element.getAsString());
		return values;
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
		ItemStack result = ItemStack.EMPTY;
		if (pJson.has("result")) {
			result = RecipeResultStackParser.parseResultStack(pJson, "result");
		}
		int requiredDegree = requiredDegreeFromJson(pJson);
		boolean unstained = GsonHelper.getAsBoolean(pJson, "unstained", false);
		boolean layered = pJson.has("floor");
		MultiblockPattern mbPattern = layered ? null : CardinalRitePatternJson.parse(pJson);
		boolean breakBlocksOnCreation = GsonHelper.getAsBoolean(pJson, "breakBlocksOnCreation", !layered);
		boolean rankup = GsonHelper.getAsBoolean(pJson, "rankup", false);
		CardinalRiteRecipe recipe = new CardinalRiteRecipe(pRecipeId, cost, riteType, mbPattern, result, riteName,
				riteDescription, requiredDegree, breakBlocksOnCreation, unstained, rankup);
		recipe.setRequiredPurity(GsonHelper.getAsFloat(pJson, "required_purity", -1.0f));
		recipe.setRequiredClarity(GsonHelper.getAsFloat(pJson, "required_clarity", -1.0f));
		if (layered) {
			recipe.setFloorId(ResourceLocation.parse(GsonHelper.getAsString(pJson, "floor")));
			if (pJson.has("required_structure")) {
				JsonObject structure = GsonHelper.getAsJsonObject(pJson, "required_structure");
				recipe.setRequiredStructure(CardinalRitePatternJson.parse(structure));
				recipe.setConsumeRequiredStructure(
						GsonHelper.getAsBoolean(structure, "consume_on_success", false));
			}
			recipe.setBrazierSignature(brazierSignatureFromJson(pJson));
		}
		if (!unstained) {
			recipe.setCeremony(ceremonyFromJson(pJson, pRecipeId, riteType, requiredDegree));
			int offeringCount = recipe.getBrazierSignature().stream()
					.mapToInt(CardinalRiteRecipe.BrazierRequirement::count).sum();
			List<String> violations = CardinalRiteProgressionPolicy.violations(
					pRecipeId.getPath(), requiredDegree, recipe.getCeremony(), offeringCount);
			if (!violations.isEmpty()) {
				throw new JsonSyntaxException("Harbinger cardinal rite " + pRecipeId
						+ " exceeds its degree " + requiredDegree + " ceremony ceiling: "
						+ String.join("; ", violations));
			}
		}
		return recipe;
	}

	private static List<CardinalRiteRecipe.BrazierRequirement> brazierSignatureFromJson(JsonObject json) {
		if (!json.has("brazier_signature")) return List.of();
		List<CardinalRiteRecipe.BrazierRequirement> result = new ArrayList<>();
		for (JsonElement element : json.getAsJsonArray("brazier_signature")) {
			JsonObject entry = element.getAsJsonObject();
			Ingredient ingredient = Ingredient.CODEC_NONEMPTY.parse(JsonOps.INSTANCE, entry.get("ingredient"))
					.getOrThrow(message -> new JsonSyntaxException("Invalid brazier ingredient: " + message));
			result.add(new CardinalRiteRecipe.BrazierRequirement(
					ingredient, GsonHelper.getAsInt(entry, "count", 1),
					CardinalRiteOfferingConsumptionRules.fromNullable(entry.has("consume_on_success")
							? entry.get("consume_on_success").getAsBoolean() : null)));
		}
		return List.copyOf(result);
	}

	// ---- RecipeSerializer 1.21.1 API ----

	private static final MapCodec<CardinalRiteRecipe> CODEC = new MapCodec<CardinalRiteRecipe>() {
		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of("id", "bloodCost", "riteType", "riteName", "riteDescription", "pattern", "key",
					"result", "required_degree", "required_purity", "required_clarity",
					"breakBlocksOnCreation", "unstained", "rankup", "ceremony", "floor",
					"required_structure", "brazier_signature").map(ops::createString);
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
		MultiblockPattern mbPattern = pBuffer.readBoolean() ? readPattern(pBuffer) : null;
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
		if (pBuffer.readBoolean()) {
			recipe.setCeremony(readCeremony(pBuffer));
		}
		if (pBuffer.readBoolean()) {
			recipe.setFloorId(pBuffer.readResourceLocation());
			if (pBuffer.readBoolean()) recipe.setRequiredStructure(readPattern(pBuffer));
			recipe.setConsumeRequiredStructure(pBuffer.readBoolean());
			List<CardinalRiteRecipe.BrazierRequirement> requirements = new ArrayList<>();
			for (int i = 0, count = pBuffer.readVarInt(); i < count; i++) {
				requirements.add(new CardinalRiteRecipe.BrazierRequirement(
						Ingredient.CONTENTS_STREAM_CODEC.decode(pBuffer), pBuffer.readVarInt(), pBuffer.readBoolean()));
			}
			recipe.setBrazierSignature(requirements);
		}
		return recipe;
	}

	private static MultiblockPattern readPattern(RegistryFriendlyByteBuf pBuffer) {
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
		Map<String, PatternKeyEntry> predicateKeys = Maps.newHashMap();
		Map<String, MultiblockPatternKey> displayKeys = Maps.newHashMap();
		int symbolListLength = pBuffer.readInt();
		for (int i = 0; i < symbolListLength; i++) {
			String key = pBuffer.readUtf();
			boolean tagged = pBuffer.readBoolean();
			Block fallback = BuiltInRegistries.BLOCK.get(pBuffer.readResourceLocation());
			ResourceLocation tagId = tagged ? pBuffer.readResourceLocation() : null;
			List<Block> displayBlocks = readDisplayBlocks(pBuffer);
			map.put(key, fallback);
			ResourceLocation fallbackId = BuiltInRegistries.BLOCK.getKey(fallback);
			predicateKeys.put(key, PatternKeyEntry.fromNetwork(key,
					tagged ? null : fallbackId, tagId, fallbackId));
			if (tagged) {
				displayKeys.put(key, MultiblockPatternKey.tag(key, tagId, fallback, displayBlocks));
			} else {
				displayKeys.put(key, MultiblockPatternKey.block(key, fallback));
			}
		}
		BlockPattern bp = generateBlockPatternFromKeyEntries(predicateKeys, pattern);
		return new MultiblockPattern(bp, displayKeys, pattern, true);
	}

	private static void toNetwork(RegistryFriendlyByteBuf pBuffer, CardinalRiteRecipe pRecipe) {
		pBuffer.writeResourceLocation(pRecipe.getId());
		pBuffer.writeDouble(pRecipe.getBloodCost());
		pBuffer.writeUtf(pRecipe.getRiteType().getSerializedName());
		pBuffer.writeUtf(pRecipe.getRiteName());
		pBuffer.writeUtf(pRecipe.getRiteDescription());
		pBuffer.writeBoolean(pRecipe.getPattern() != null);
		if (pRecipe.getPattern() != null) writePattern(pBuffer, pRecipe.getPattern());
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
		pBuffer.writeBoolean(pRecipe.getCeremony() != null);
		if (pRecipe.getCeremony() != null) {
			writeCeremony(pBuffer, pRecipe.getCeremony());
		}
		pBuffer.writeBoolean(pRecipe.hasLayeredStation());
		if (pRecipe.hasLayeredStation()) {
			pBuffer.writeResourceLocation(pRecipe.getFloorId());
			pBuffer.writeBoolean(pRecipe.getRequiredStructure() != null);
			if (pRecipe.getRequiredStructure() != null) writePattern(pBuffer, pRecipe.getRequiredStructure());
			pBuffer.writeBoolean(pRecipe.shouldConsumeRequiredStructure());
			pBuffer.writeVarInt(pRecipe.getBrazierSignature().size());
			for (CardinalRiteRecipe.BrazierRequirement requirement : pRecipe.getBrazierSignature()) {
				Ingredient.CONTENTS_STREAM_CODEC.encode(pBuffer, requirement.ingredient());
				pBuffer.writeVarInt(requirement.count());
				pBuffer.writeBoolean(requirement.consumeOnSuccess());
			}
		}
	}

	private static void writePattern(RegistryFriendlyByteBuf pBuffer, MultiblockPattern pattern) {
		pBuffer.writeInt(pattern.getPatternArray().length);
		for (String[] row : pattern.getPatternArray()) {
			pBuffer.writeInt(row.length);
			for (String cell : row) pBuffer.writeUtf(cell);
		}
		pBuffer.writeInt(pattern.getKeyList().size());
		pattern.getKeyList().forEach((k, v) -> {
			pBuffer.writeUtf(k);
			pBuffer.writeBoolean(v.isTag());
			pBuffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(v.fallbackBlock()));
			if (v.isTag()) {
				pBuffer.writeResourceLocation(v.tagId());
			}
			writeDisplayBlocks(pBuffer, displayBlocksForNetwork(pBuffer, v));
		});
	}

	private static CardinalRiteCeremonyDefinition readCeremony(RegistryFriendlyByteBuf buffer) {
		CardinalRiteCeremonyProfile profile = buffer.readEnum(CardinalRiteCeremonyProfile.class);
		List<CardinalRiteCeremonyDefinition.Anchor> anchors = new ArrayList<>();
		for (int i = 0, count = buffer.readVarInt(); i < count; i++) {
			anchors.add(new CardinalRiteCeremonyDefinition.Anchor(buffer.readVarInt(), buffer.readVarInt(),
					buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()));
		}
		List<CardinalRiteCeremonyDefinition.SupportSocket> sockets = new ArrayList<>();
		for (int i = 0, count = buffer.readVarInt(); i < count; i++) {
			sockets.add(new CardinalRiteCeremonyDefinition.SupportSocket(buffer.readVarInt(), buffer.readVarInt(),
					buffer.readVarInt(), buffer.readUtf(), buffer.readBoolean()));
		}
		List<String> waves = readStrings(buffer);
		List<String> guaranteed = readStrings(buffer);
		String handler = buffer.readUtf();
		List<BlockPos> fragile = new ArrayList<>();
		for (int i = 0, count = buffer.readVarInt(); i < count; i++) fragile.add(buffer.readBlockPos());
		int targetDurationTicks = buffer.readVarInt();
		String focusMode = buffer.readUtf();
		int requiredHelpers = buffer.readVarInt();
		List<String> helperRoles = readStrings(buffer);
		int stillIntervalTicks = buffer.readVarInt();
		CardinalRiteCeremonyDefinition.Atmosphere atmosphere =
				new CardinalRiteCeremonyDefinition.Atmosphere(
						buffer.readUtf(), buffer.readBoolean(), buffer.readBoolean());
		String failureProfile = buffer.readUtf();
		return new CardinalRiteCeremonyDefinition(profile, anchors, sockets, waves, guaranteed, handler, fragile,
				targetDurationTicks, focusMode, requiredHelpers, helperRoles, stillIntervalTicks,
				atmosphere, failureProfile);
	}

	private static void writeCeremony(RegistryFriendlyByteBuf buffer, CardinalRiteCeremonyDefinition ceremony) {
		buffer.writeEnum(ceremony.profile());
		buffer.writeVarInt(ceremony.anchors().size());
		for (CardinalRiteCeremonyDefinition.Anchor anchor : ceremony.anchors()) {
			buffer.writeVarInt(anchor.x());
			buffer.writeVarInt(anchor.y());
			buffer.writeVarInt(anchor.z());
			buffer.writeVarInt(anchor.ring());
			buffer.writeVarInt(anchor.order());
		}
		buffer.writeVarInt(ceremony.supportSockets().size());
		for (CardinalRiteCeremonyDefinition.SupportSocket socket : ceremony.supportSockets()) {
			buffer.writeVarInt(socket.x());
			buffer.writeVarInt(socket.y());
			buffer.writeVarInt(socket.z());
			buffer.writeUtf(socket.suggestedSigil());
			buffer.writeBoolean(socket.required());
		}
		writeStrings(buffer, ceremony.waves());
		writeStrings(buffer, ceremony.guaranteedWaves());
		buffer.writeUtf(ceremony.signatureHandler());
		buffer.writeVarInt(ceremony.fragileOffsets().size());
		for (BlockPos offset : ceremony.fragileOffsets()) buffer.writeBlockPos(offset);
		buffer.writeVarInt(ceremony.targetDurationTicks());
		buffer.writeUtf(ceremony.focusMode());
		buffer.writeVarInt(ceremony.requiredHelpers());
		writeStrings(buffer, ceremony.helperRoles());
		buffer.writeVarInt(ceremony.stillIntervalTicks());
		buffer.writeUtf(ceremony.atmosphere().fog());
		buffer.writeBoolean(ceremony.atmosphere().lightning());
		buffer.writeBoolean(ceremony.atmosphere().dome());
		buffer.writeUtf(ceremony.failureProfile());
	}

	private static List<String> readStrings(RegistryFriendlyByteBuf buffer) {
		List<String> values = new ArrayList<>();
		for (int i = 0, count = buffer.readVarInt(); i < count; i++) values.add(buffer.readUtf());
		return values;
	}

	private static void writeStrings(RegistryFriendlyByteBuf buffer, List<String> values) {
		buffer.writeVarInt(values.size());
		for (String value : values) buffer.writeUtf(value);
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

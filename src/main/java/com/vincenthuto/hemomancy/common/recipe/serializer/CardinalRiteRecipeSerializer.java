package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.serialization.*;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hemomancy.common.rite.*;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import com.vincenthuto.hutoslib.math.MultiblockPatternKey;
import net.minecraft.core.BlockPos;
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

public class CardinalRiteRecipeSerializer implements RecipeSerializer<CardinalRiteRecipe> {
	private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private static Predicate<BlockInWorld> keyEntryPredFromHash(Map<String, PatternKeyEntry> symbolList, String string) {
		PatternKeyEntry entry = symbolList.get(string);
		return entry::matches;
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
				int y = GsonHelper.getAsInt(anchor, "y", 1);
				// The codec writes each anchor's resolved x and z, so a layout's anchors read back where they stood.
				anchors.add(anchor.has("x") && anchor.has("z")
						? new CardinalRiteCeremonyDefinition.Anchor(GsonHelper.getAsInt(anchor, "x"), y,
								GsonHelper.getAsInt(anchor, "z"), ring, order)
						: CardinalRiteRingTuning.anchor(ring, order, y));
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
		requireCeremonyField(ceremony, recipeId, "fragile_offsets");
		List<CardinalRiteCeremonyDefinition.SupportSocket> sockets = new ArrayList<>();
		for (JsonElement element : ceremony.getAsJsonArray("support_sockets")) {
			JsonObject socket = element.getAsJsonObject();
			requireCeremonyField(socket, recipeId, "required");
			sockets.add(new CardinalRiteCeremonyDefinition.SupportSocket(
					GsonHelper.getAsInt(socket, "x"), GsonHelper.getAsInt(socket, "y", 0),
					GsonHelper.getAsInt(socket, "z"),
					GsonHelper.getAsString(socket, "suggested_sigil", ""),
					RecipeCodecJson.booleanField(socket, "required", false)));
		}
		List<String> waves = stringList(ceremony, "waves");
		List<String> guaranteed = stringList(ceremony, "guaranteed_waves");
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
		String failure = GsonHelper.getAsString(ceremony, "failure");
		if (!java.util.Set.of("safe_retry", "offering_loss", "fragile_damage", "collapse").contains(failure)) {
			throw new JsonSyntaxException("Harbinger cardinal rite " + recipeId
					+ " names unknown failure profile '" + failure + "'");
		}
		String culmination = GsonHelper.getAsString(ceremony, "culmination", "");
		if (!culmination.isBlank()
				&& com.vincenthuto.hemomancy.common.rite.CardinalRiteCulmination.byId(culmination) == null) {
			throw new JsonSyntaxException("Harbinger cardinal rite " + recipeId
					+ " names unknown culmination '" + culmination + "'");
		}
		return new CardinalRiteCeremonyDefinition(profile, anchors, sockets, waves, guaranteed, fragile,
				GsonHelper.getAsInt(ceremony, "target_duration_ticks"),
				GsonHelper.getAsString(ceremony, "focus"),
				GsonHelper.getAsInt(ceremony, "required_helpers"),
				stringList(ceremony, "helper_roles"),
				GsonHelper.getAsInt(ceremony, "still_interval_ticks"),
				new CardinalRiteCeremonyDefinition.Atmosphere(
						GsonHelper.getAsString(atmosphere, "fog"),
						RecipeCodecJson.booleanField(atmosphere, "lightning", false),
						RecipeCodecJson.booleanField(atmosphere, "dome", false)),
				GsonHelper.getAsString(ceremony, "failure"),
				GsonHelper.getAsInt(ceremony, "wave_count", -1),
				GsonHelper.getAsString(ceremony, "culmination", ""));
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

	private static CardinalRiteRecipe fromJsonObject(ResourceLocation pRecipeId, JsonObject pJson, DynamicOps<JsonElement> jsonOps) {
		double cost = GsonHelper.getAsFloat(pJson, "bloodCost");
		String riteTypeName = GsonHelper.getAsString(pJson, "riteType");
		CardinalRiteType riteType = CardinalRiteType.byName(riteTypeName);
		String riteName = GsonHelper.getAsString(pJson, "riteName", "");
		String riteDescription = GsonHelper.getAsString(pJson, "riteDescription", "");
		ItemStack result = ItemStack.EMPTY;
		if (pJson.has("result")) {
			result = RecipeResultStackParser.parseResultStack(pJson, "result", jsonOps);
		}
		int requiredDegree = requiredDegreeFromJson(pJson);
		boolean unstained = RecipeCodecJson.booleanField(pJson, "unstained", false);
		boolean layered = pJson.has("floor");
		MultiblockPattern mbPattern = layered ? null : CardinalRitePatternJson.parse(pJson);
		boolean breakBlocksOnCreation = RecipeCodecJson.booleanField(pJson, "breakBlocksOnCreation", !layered);
		boolean rankup = RecipeCodecJson.booleanField(pJson, "rankup", false);
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
						RecipeCodecJson.booleanField(structure, "consume_on_success", false));
			}
			recipe.setBrazierSignature(brazierSignatureFromJson(pJson, jsonOps));
			recipe.setOptionalOfferings(optionalOfferingsFromJson(pJson, jsonOps));
			if (!recipe.getOptionalOfferings().isEmpty() && !recipe.getBrazierSignature().isEmpty()) {
				throw new JsonSyntaxException("Cardinal rite " + pRecipeId
						+ " declares both a brazier signature and optional offerings");
			}
			if (pJson.has("subject")) {
				JsonObject subject = GsonHelper.getAsJsonObject(pJson, "subject");
				recipe.setSubject(CardinalRitePatternJson.parse(subject), GsonHelper.getAsInt(subject, "feed"));
			}
		}
		CardinalRitePurpose purpose = CardinalRitePurpose.byId(GsonHelper.getAsString(pJson, "purpose", ""));
		boolean special = RecipeCodecJson.booleanField(pJson, "special", false);
		if (!unstained) {
			String violation = CardinalRitePurpose.violation(purpose, special,
					recipe.getFloorId() == null ? "" : recipe.getFloorId().getPath());
			if (violation != null) {
				throw new JsonSyntaxException("Harbinger cardinal rite " + pRecipeId + " " + violation);
			}
		}
		recipe.setPurpose(purpose);
		recipe.setSpecial(special);
		String worldFinale = pJson.has("world_finale") ? GsonHelper.getAsString(pJson, "world_finale") : null;
		String finaleViolation = CardinalRiteDaemonFinale.violation(purpose, worldFinale);
		if (finaleViolation != null) {
			throw new JsonSyntaxException("Cardinal rite " + pRecipeId + " " + finaleViolation);
		}
		recipe.setDaemonFinale(CardinalRiteDaemonFinale.resolve(purpose, worldFinale));
		if (pJson.has("completion_effect")) {
			recipe.setCompletionEffect(ResourceLocation.parse(GsonHelper.getAsString(pJson, "completion_effect")));
		}
		recipe.setMedium(mediumFromJson(pJson, pRecipeId, jsonOps));
		recipe.setConsumeMediumOnSuccess(CardinalRiteMediumRules.consumeOnSuccessFromNullable(
				pJson.has("consume_medium_on_success")
						? RecipeCodecJson.booleanField(pJson, "consume_medium_on_success", false) : null));
		if (!unstained) {
			recipe.setCeremony(ceremonyFromJson(pJson, pRecipeId, riteType, requiredDegree));
			int offeringCount = recipe.getBrazierSignature().stream()
					.mapToInt(CardinalRiteRecipe.BrazierRequirement::count).sum()
					+ recipe.getOptionalOfferings().size()
					// A Crimson Troupe ordeal's key, the professor's own vial (OrdealKey), is a token, not an offering.
					- (pJson.has("puppeteer_trial") ? 1 : 0);
			// Station upgrade rites declare their "id" so the codec path is real here.
			String policyPath = pRecipeId.getPath();
			List<String> violations = CardinalRiteProgressionPolicy.violations(
					policyPath, requiredDegree, recipe.getCeremony(), offeringCount, recipe.isWorkingCraft());
			if (!violations.isEmpty()) {
				throw new JsonSyntaxException("Harbinger cardinal rite " + pRecipeId
						+ " exceeds its degree " + requiredDegree + " ceremony ceiling: "
						+ String.join("; ", violations));
			}
		}
		if (recipe.isWorkingCraft()) {
			String violation = WorkingCraftRecipeRules.violation(purpose,
					recipe.getFloorId() == null ? "" : recipe.getFloorId().getPath(),
					recipe.getSubject().getPatternArray(), recipe.getSubjectFeedMl(),
					recipe.getCeremony() == null ? 0 : recipe.getCeremony().anchors().size(), (int) recipe.getBloodCost());
			if (violation != null) throw new JsonSyntaxException("Working craft " + pRecipeId + " " + violation);
		}
		if (pJson.has("puppeteer_trial")) {
			JsonObject trial = GsonHelper.getAsJsonObject(pJson, "puppeteer_trial");
			String summonName = GsonHelper.getAsString(trial, "summon");
			validatePuppeteerTrial(pRecipeId, recipe, summonName);
			recipe.setPuppeteerTrial(new CardinalRiteRecipe.PuppeteerTrial(summonName));
		}
		return recipe;
	}

	private static void validatePuppeteerTrial(ResourceLocation recipeId, CardinalRiteRecipe recipe,
			String summonName) {
		if (!recipe.hasLayeredStation() || recipe.isUnstained()) {
			throw new JsonSyntaxException("Puppeteer trial " + recipeId + " must be a layered Harbinger rite");
		}
		if (PuppeteerSummonDefinitions.byName(summonName).isEmpty()) {
			throw new JsonSyntaxException("Unknown puppeteer trial summon '" + summonName + "' in " + recipeId);
		}
		if (!recipe.hasMedium()
				|| !recipe.getMedium().test(new ItemStack(ItemInit.marionette_crossbar.get()))) {
			throw new JsonSyntaxException("Puppeteer trial " + recipeId + " must require a Marionette Crossbar medium");
		}
		if (recipe.getCeremony() == null || !"living_staff".equals(recipe.getCeremony().focusMode())) {
			throw new JsonSyntaxException("Puppeteer trial " + recipeId + " must use a Living Staff ceremony");
		}
	}

	private static Ingredient mediumFromJson(JsonObject json, ResourceLocation recipeId, DynamicOps<JsonElement> jsonOps) {
		if (!json.has("medium")) return Ingredient.EMPTY;
		return Ingredient.CODEC_NONEMPTY.parse(jsonOps, json.get("medium"))
				.getOrThrow(message -> new JsonSyntaxException(
						"Invalid medium for Cardinal Rite " + recipeId + ": " + message));
	}

	private static List<CardinalRiteRecipe.BrazierRequirement> brazierSignatureFromJson(JsonObject json, DynamicOps<JsonElement> jsonOps) {
		if (!json.has("brazier_signature")) return List.of();
		List<CardinalRiteRecipe.BrazierRequirement> result = new ArrayList<>();
		for (JsonElement element : json.getAsJsonArray("brazier_signature")) {
			JsonObject entry = element.getAsJsonObject();
			Ingredient ingredient = Ingredient.CODEC_NONEMPTY.parse(jsonOps, entry.get("ingredient"))
					.getOrThrow(message -> new JsonSyntaxException("Invalid brazier ingredient: " + message));
			result.add(new CardinalRiteRecipe.BrazierRequirement(
					ingredient, GsonHelper.getAsInt(entry, "count", 1),
					CardinalRiteOfferingConsumptionRules.fromNullable(entry.has("consume_on_success")
							? RecipeCodecJson.booleanField(entry, "consume_on_success", true) : null)));
		}
		return List.copyOf(result);
	}

	private static List<CardinalRiteRecipe.OptionalOffering> optionalOfferingsFromJson(JsonObject json, DynamicOps<JsonElement> jsonOps) {
		if (!json.has("optional_offerings")) return List.of();
		List<CardinalRiteRecipe.OptionalOffering> result = new ArrayList<>();
		for (JsonElement element : json.getAsJsonArray("optional_offerings")) {
			JsonObject entry = element.getAsJsonObject();
			Ingredient ingredient = Ingredient.CODEC_NONEMPTY.parse(jsonOps, entry.get("ingredient"))
					.getOrThrow(message -> new JsonSyntaxException("Invalid optional offering ingredient: " + message));
			result.add(new CardinalRiteRecipe.OptionalOffering(ingredient, GsonHelper.getAsString(entry, "effect")));
		}
		return List.copyOf(result);
	}

	// ---- RecipeSerializer 1.21.1 API ----

	private static final MapCodec<CardinalRiteRecipe> CODEC = RecipeCodecJson.bridge("CardinalRiteRecipe", "cardinal_rite/unknown",
			CardinalRiteRecipeSerializer::fromJsonObject, CardinalRiteRecipeSerializer::encode,
			"id", "bloodCost", "riteType", "riteName", "riteDescription", "pattern", "key", "result", "required_degree", "required_purity", "required_clarity", "breakBlocksOnCreation", "unstained", "rankup", "ceremony", "floor", "required_structure", "brazier_signature", "optional_offerings", "medium", "consume_medium_on_success", "puppeteer_trial", "purpose", "special", "world_finale", "completion_effect", "subject");

	private static <T> RecordBuilder<T> encode(CardinalRiteRecipe recipe, DynamicOps<T> ops, RecordBuilder<T> prefix) {
		prefix.add("id", ops.createString(recipe.getId().toString()));
		prefix.add("bloodCost", ops.createDouble(recipe.getBloodCost()));
		prefix.add("riteType", ops.createString(recipe.getRiteType().getSerializedName()));
		prefix.add("riteName", ops.createString(recipe.getRiteName()));
		prefix.add("riteDescription", ops.createString(recipe.getRiteDescription()));
		if (recipe.getPattern() != null) {
			prefix.add("pattern", JsonOps.INSTANCE.convertTo(ops, patternJson(recipe.getPattern())));
			prefix.add("key", JsonOps.INSTANCE.convertTo(ops, keyJson(recipe.getPattern())));
		}
		DynamicOps<JsonElement> jsonOps = RecipeCodecJson.jsonOps(ops);
		if (recipe.hasLayeredStation()) {
			prefix.add("floor", ops.createString(recipe.getFloorId().toString()));
			if (recipe.getRequiredStructure() != null) {
				JsonObject structure = patternOwnerJson(recipe.getRequiredStructure());
				structure.addProperty("consume_on_success", recipe.shouldConsumeRequiredStructure());
				prefix.add("required_structure", JsonOps.INSTANCE.convertTo(ops, structure));
			}
			if (!recipe.getBrazierSignature().isEmpty()) {
				JsonArray braziers = new JsonArray();
				for (CardinalRiteRecipe.BrazierRequirement requirement : recipe.getBrazierSignature()) {
					JsonObject entry = new JsonObject();
					entry.add("ingredient", Ingredient.CODEC_NONEMPTY.encodeStart(jsonOps, requirement.ingredient()).getOrThrow());
					entry.addProperty("count", requirement.count());
					entry.addProperty("consume_on_success", requirement.consumeOnSuccess());
					braziers.add(entry);
				}
				prefix.add("brazier_signature", JsonOps.INSTANCE.convertTo(ops, braziers));
			}
			if (!recipe.getOptionalOfferings().isEmpty()) {
				JsonArray optional = new JsonArray();
				for (CardinalRiteRecipe.OptionalOffering offering : recipe.getOptionalOfferings()) {
					JsonObject entry = new JsonObject();
					entry.add("ingredient", Ingredient.CODEC_NONEMPTY.encodeStart(jsonOps, offering.ingredient()).getOrThrow());
					entry.addProperty("effect", offering.effect());
					optional.add(entry);
				}
				prefix.add("optional_offerings", JsonOps.INSTANCE.convertTo(ops, optional));
			}
			if (recipe.isWorkingCraft()) {
				JsonObject subject = patternOwnerJson(recipe.getSubject());
				subject.addProperty("feed", recipe.getSubjectFeedMl());
				prefix.add("subject", JsonOps.INSTANCE.convertTo(ops, subject));
			}
		}
		if (recipe.getCeremony() != null) {
			prefix.add("ceremony", JsonOps.INSTANCE.convertTo(ops, ceremonyJson(recipe.getCeremony())));
		}
		if (!recipe.getResult().isEmpty()) {
			prefix.add("result", ItemStack.CODEC.encodeStart(ops, recipe.getResult()));
		}
		prefix.add("required_degree", ops.createInt(recipe.getRequiredDegree()));
		if (recipe.getRequiredPurity() >= 0.0f) prefix.add("required_purity", ops.createFloat(recipe.getRequiredPurity()));
		if (recipe.getRequiredClarity() >= 0.0f) prefix.add("required_clarity", ops.createFloat(recipe.getRequiredClarity()));
		prefix.add("breakBlocksOnCreation", ops.createBoolean(recipe.shouldBreakBlocksOnCreation()));
		prefix.add("unstained", ops.createBoolean(recipe.isUnstained()));
		prefix.add("rankup", ops.createBoolean(recipe.isRankup()));
		if (recipe.getPurpose() != null) prefix.add("purpose", ops.createString(recipe.getPurpose().id()));
		if (recipe.isSpecial()) prefix.add("special", ops.createBoolean(true));
		if (recipe.getDaemonFinale() == CardinalRiteDaemonFinale.SKY) prefix.add("world_finale", ops.createString("sky"));
		if (recipe.getCompletionEffect() != null) {
			prefix.add("completion_effect", ops.createString(recipe.getCompletionEffect().toString()));
		}
		if (recipe.hasMedium()) {
			prefix.add("medium", Ingredient.CODEC.encodeStart(ops, recipe.getMedium()));
		}
		prefix.add("consume_medium_on_success", ops.createBoolean(recipe.shouldConsumeMediumOnSuccess()));
		if (recipe.isPuppeteerTrial()) {
			JsonObject trial = new JsonObject();
			trial.addProperty("summon", recipe.getPuppeteerTrial().summonName());
			prefix.add("puppeteer_trial", JsonOps.INSTANCE.convertTo(ops, trial));
		}
		return prefix;
	}

	private static JsonObject patternOwnerJson(MultiblockPattern pattern) {
		JsonObject owner = new JsonObject();
		owner.add("pattern", patternJson(pattern));
		owner.add("key", keyJson(pattern));
		return owner;
	}

	private static JsonArray patternJson(MultiblockPattern pattern) {
		JsonArray aisles = new JsonArray();
		for (String[] aisle : pattern.getPatternArray()) {
			JsonArray rows = new JsonArray();
			for (String row : aisle) rows.add(row);
			aisles.add(rows);
		}
		return aisles;
	}

	private static JsonObject keyJson(MultiblockPattern pattern) {
		JsonObject keys = new JsonObject();
		pattern.getKeyList().forEach((symbol, key) -> {
			if (" ".equals(symbol)) return;
			JsonObject entry = new JsonObject();
			String fallback = BuiltInRegistries.BLOCK.getKey(key.fallbackBlock()).toString();
			if (key.isTag()) {
				entry.addProperty("tag", key.tagId().toString());
				entry.addProperty("fallback", fallback);
			} else {
				entry.addProperty("block", fallback);
			}
			keys.add(symbol, entry);
		});
		return keys;
	}

	private static JsonObject ceremonyJson(CardinalRiteCeremonyDefinition ceremony) {
		JsonObject json = new JsonObject();
		json.addProperty("profile", ceremony.profile().name().toLowerCase(java.util.Locale.ROOT));
		JsonArray anchors = new JsonArray();
		for (CardinalRiteCeremonyDefinition.Anchor anchor : ceremony.anchors()) {
			JsonObject entry = new JsonObject();
			entry.addProperty("x", anchor.x());
			entry.addProperty("y", anchor.y());
			entry.addProperty("z", anchor.z());
			entry.addProperty("ring", anchor.ring());
			entry.addProperty("order", anchor.order());
			anchors.add(entry);
		}
		json.add("anchors", anchors);
		JsonArray sockets = new JsonArray();
		for (CardinalRiteCeremonyDefinition.SupportSocket socket : ceremony.supportSockets()) {
			JsonObject entry = new JsonObject();
			entry.addProperty("x", socket.x());
			entry.addProperty("y", socket.y());
			entry.addProperty("z", socket.z());
			entry.addProperty("suggested_sigil", socket.suggestedSigil());
			entry.addProperty("required", socket.required());
			sockets.add(entry);
		}
		json.add("support_sockets", sockets);
		json.add("waves", stringArray(ceremony.waves()));
		json.add("guaranteed_waves", stringArray(ceremony.guaranteedWaves()));
		JsonArray fragile = new JsonArray();
		for (BlockPos offset : ceremony.fragileOffsets()) {
			JsonArray cell = new JsonArray();
			cell.add(offset.getX());
			cell.add(offset.getY());
			cell.add(offset.getZ());
			fragile.add(cell);
		}
		json.add("fragile_offsets", fragile);
		json.addProperty("target_duration_ticks", ceremony.targetDurationTicks());
		json.addProperty("focus", ceremony.focusMode());
		json.addProperty("required_helpers", ceremony.requiredHelpers());
		json.add("helper_roles", stringArray(ceremony.helperRoles()));
		json.addProperty("still_interval_ticks", ceremony.stillIntervalTicks());
		JsonObject atmosphere = new JsonObject();
		atmosphere.addProperty("fog", ceremony.atmosphere().fog());
		atmosphere.addProperty("lightning", ceremony.atmosphere().lightning());
		atmosphere.addProperty("dome", ceremony.atmosphere().dome());
		json.add("atmosphere", atmosphere);
		json.addProperty("failure", ceremony.failureProfile());
		json.addProperty("wave_count", ceremony.waveCount());
		json.addProperty("culmination", ceremony.culmination());
		return json;
	}

	private static JsonArray stringArray(List<String> values) {
		JsonArray array = new JsonArray();
		for (String value : values) array.add(value);
		return array;
	}

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
		recipe.setPurpose(CardinalRitePurpose.byId(pBuffer.readUtf()));
		recipe.setSpecial(pBuffer.readBoolean());
		recipe.setDaemonFinale(pBuffer.readEnum(CardinalRiteDaemonFinale.class));
		if (pBuffer.readBoolean()) recipe.setCompletionEffect(pBuffer.readResourceLocation());
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
			List<CardinalRiteRecipe.OptionalOffering> optional = new ArrayList<>();
			for (int i = 0, count = pBuffer.readVarInt(); i < count; i++) {
				optional.add(new CardinalRiteRecipe.OptionalOffering(
						Ingredient.CONTENTS_STREAM_CODEC.decode(pBuffer), pBuffer.readUtf()));
			}
			recipe.setOptionalOfferings(optional);
			if (pBuffer.readBoolean()) {
				MultiblockPattern subject = readPattern(pBuffer);
				recipe.setSubject(subject, pBuffer.readVarInt());
			}
		}
		if (pBuffer.readBoolean()) {
			recipe.setMedium(Ingredient.CONTENTS_STREAM_CODEC.decode(pBuffer));
		}
		recipe.setConsumeMediumOnSuccess(pBuffer.readBoolean());
		if (pBuffer.readBoolean()) {
			recipe.setPuppeteerTrial(new CardinalRiteRecipe.PuppeteerTrial(pBuffer.readUtf()));
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
		pBuffer.writeUtf(pRecipe.getPurpose() == null ? "" : pRecipe.getPurpose().id());
		pBuffer.writeBoolean(pRecipe.isSpecial());
		pBuffer.writeEnum(pRecipe.getDaemonFinale());
		pBuffer.writeBoolean(pRecipe.getCompletionEffect() != null);
		if (pRecipe.getCompletionEffect() != null) pBuffer.writeResourceLocation(pRecipe.getCompletionEffect());
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
			pBuffer.writeVarInt(pRecipe.getOptionalOfferings().size());
			for (CardinalRiteRecipe.OptionalOffering offering : pRecipe.getOptionalOfferings()) {
				Ingredient.CONTENTS_STREAM_CODEC.encode(pBuffer, offering.ingredient());
				pBuffer.writeUtf(offering.effect());
			}
			pBuffer.writeBoolean(pRecipe.isWorkingCraft());
			if (pRecipe.isWorkingCraft()) {
				writePattern(pBuffer, pRecipe.getSubject());
				pBuffer.writeVarInt(pRecipe.getSubjectFeedMl());
			}
		}
		pBuffer.writeBoolean(pRecipe.hasMedium());
		if (pRecipe.hasMedium()) {
			Ingredient.CONTENTS_STREAM_CODEC.encode(pBuffer, pRecipe.getMedium());
		}
		pBuffer.writeBoolean(pRecipe.shouldConsumeMediumOnSuccess());
		pBuffer.writeBoolean(pRecipe.isPuppeteerTrial());
		if (pRecipe.isPuppeteerTrial()) pBuffer.writeUtf(pRecipe.getPuppeteerTrial().summonName());
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
		int waveCount = buffer.readVarInt() - 1;
		String culmination = buffer.readUtf();
		return new CardinalRiteCeremonyDefinition(profile, anchors, sockets, waves, guaranteed, fragile,
				targetDurationTicks, focusMode, requiredHelpers, helperRoles, stillIntervalTicks,
				atmosphere, failureProfile, waveCount, culmination);
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
		buffer.writeVarInt(ceremony.waveCount() + 1);
		buffer.writeUtf(ceremony.culmination());
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

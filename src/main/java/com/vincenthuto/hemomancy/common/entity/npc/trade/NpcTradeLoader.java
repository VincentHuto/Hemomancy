package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.vincenthuto.hemomancy.common.data.JsonResourceReloadListener;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtHospitality;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSides.Side;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.BloodMemoryItem;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.HematicMemoryItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * Loads NPC want cards from {@code data/<ns>/npc_trade/<speaker>/<offer>.json} (schema: docs/npc_trade/02). A bad file
 * is logged, counted and skipped; the rest load. Rules that span files (two cards on one speaker wanting the same
 * thing, the {@code never_sold} tag) are checked by {@link NpcTradeRegistry}, again once tags are bound.
 */
public final class NpcTradeLoader extends JsonResourceReloadListener<NpcTradeOffer> {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final String FOLDER = "npc_trade";
	/** Harbinger degree and Unstained stage both run 0-8. */
	static final int MAX_LEVEL = 8;
	static final int MAX_WANT_COUNT = 64;

	private static final Set<String> TOP_LEVEL = Set.of("title", "line", "order", "requires", "want", "give",
			"give_fine", "rapport", "stock", "show_locked", "opens", "rotation");
	private static final Set<String> REQUIRES = Set.of("path", "degree_at_least", "stage_at_least",
			"rapport_at_least", "advancement", "blood_active");
	private static final Set<String> ITEMS_WANT = Set.of("kind", "ingredient", "count", "appraisal");
	private static final Set<String> GIVE = Set.of("item", "count", "components");
	private static final Set<String> STOCK = Set.of("uses", "restock_days");
	private static final Set<String> ROTATION = Set.of("pool", "weight");
	private static final String APPRAISAL_PREFIX = "appraisal_";

	/** What the loader asks of the game; the live one reads the registries, tests pass their own. */
	public interface Environment {
		boolean itemExists(ResourceLocation item);

		/** Whether the item is a manipulation memory by class, beside {@link NpcTradeGiveRules}' name rule. */
		boolean isMemoryItem(ResourceLocation item);

		/** Fails a give whose components don't decode or whose count passes the item's stack size. */
		void checkGive(NpcTradeGive give);

		static Environment live(HolderLookup.Provider registries) {
			return new Environment() {
				@Override
				public boolean itemExists(ResourceLocation item) {
					return BuiltInRegistries.ITEM.containsKey(item);
				}

				@Override
				public boolean isMemoryItem(ResourceLocation item) {
					Item value = BuiltInRegistries.ITEM.get(item);
					return value instanceof BloodMemoryItem || value instanceof HematicMemoryItem;
				}

				@Override
				public void checkGive(NpcTradeGive give) {
					ItemStack stack = give.toStack(registries);
					if (give.count() > stack.getMaxStackSize()) throw new JsonSyntaxException("Give count " + give.count() + " passes the stack size of " + give.item());
				}
			};
		}
	}

	public NpcTradeLoader(HolderLookup.Provider registries) {
		this(Environment.live(registries), new AtomicInteger(), NpcTradeRegistry::install);
	}

	/** {@code publisher} gets the parsed offers and how many files failed to parse. */
	NpcTradeLoader(Environment environment, AtomicInteger errors,
			BiConsumer<Map<ResourceLocation, NpcTradeOffer>, Integer> publisher) {
		super(FOLDER, "NPC trade", LOGGER, (id, root) -> {
			try {
				return parse(id, root, environment);
			} catch (RuntimeException exception) {
				errors.incrementAndGet();
				throw exception;
			}
		}, loaded -> publisher.accept(loaded, errors.getAndSet(0)));
	}

	// one file

	public static NpcTradeOffer parse(ResourceLocation id, JsonObject root, Environment environment) {
		String[] segments = id.getPath().split("/");
		if (segments.length < 2) throw new JsonSyntaxException("Expected npc_trade/<speaker>/<offer>.json");
		String speaker = segments[0];
		Side side = NpcTradeSides.side(speaker).orElseThrow(() -> new JsonSyntaxException("Unknown speaker '" + speaker + "'; add it to NpcTradeSides"));
		onlyKeys(root, TOP_LEVEL, "offer");

		String title = langKey(root, "title");
		String line = langKey(root, "line");
		int order = GsonHelper.getAsInt(root, "order", 0);
		NpcTradeRequirements requirements = requirements(GsonHelper.getAsJsonObject(root, "requires", new JsonObject()), side);
		NpcTradeWant want = want(GsonHelper.getAsJsonObject(root, "want"), side, speaker, environment);
		if (want instanceof NpcTradeWant.Blood) requirements = requirements.withBloodActive();

		List<NpcTradeGive> give = gives(root, "give", environment);
		List<NpcTradeGive> giveFine = gives(root, "give_fine", environment);
		boolean custom = want instanceof NpcTradeWant.Custom;
		boolean act = want instanceof NpcTradeWant.Act;
		// Q-1: the Court keeps its gift economy, so a Court card asks for the house's custom and never a price.
		if (side == Side.COURT && !custom) throw new JsonSyntaxException("A Brined Court card takes only its house's custom; the Court sets no prices (Q-1)");
		if (act && give.isEmpty()) throw new JsonSyntaxException("'give' needs at least one stack");
		if (act && !giveFine.isEmpty()) throw new JsonSyntaxException("An act is never graded; 'give_fine' can't apply");
		if (custom) {
			if (!give.isEmpty() || !giveFine.isEmpty()) throw new JsonSyntaxException("A custom card earns standing; it gives no goods");
			if (root.has("stock")) throw new JsonSyntaxException("A custom card has no stock");
		} else if (!act) {
			if (give.isEmpty()) throw new JsonSyntaxException("'give' needs at least one stack");
			if (!giveFine.isEmpty() && !canGradeFine(want)) throw new JsonSyntaxException("'give_fine' is set but no grader on this want can grade fine");
			if (root.has("opens")) throw new JsonSyntaxException("'opens' is only for custom cards");
		}
		if (act && root.has("opens")) throw new JsonSyntaxException("'opens' is only for custom cards");
		Optional<String> opens = root.has("opens") ? Optional.of(langKey(root, "opens")) : Optional.empty();

		int rapport = GsonHelper.getAsInt(root, "rapport", custom || act ? 2 : 1);
		if (rapport < 0) throw new JsonSyntaxException("'rapport' must not be negative");
		NpcTradeStock stock = stock(GsonHelper.getAsJsonObject(root, "stock", null));
		// An act card is the way to the act, so it shows while the player has none banked.
		boolean showLocked = GsonHelper.getAsBoolean(root, "show_locked", act);
		Optional<NpcTradeRotation.Entry> rotation = rotation(GsonHelper.getAsJsonObject(root, "rotation", null));
		if (rotation.isPresent() && (custom || act)) throw new JsonSyntaxException("Only items, bundle and blood cards rotate");
		return new NpcTradeOffer(id, speaker, side, title, line, order, requirements, want, give, giveFine, rapport,
				stock, showLocked, opens, rotation);
	}

	// rotation

	private static Optional<NpcTradeRotation.Entry> rotation(JsonObject json) {
		if (json == null) return Optional.empty();
		onlyKeys(json, ROTATION, "rotation");
		String pool = GsonHelper.getAsString(json, "pool");
		if (!pool.matches("[a-z0-9_]+")) throw new JsonSyntaxException("'rotation.pool' must be a lowercase id");
		int weight = GsonHelper.getAsInt(json, "weight", 1);
		if (weight < 1 || weight > NpcTradeRotation.MAX_WEIGHT) throw new JsonSyntaxException("'rotation.weight' must be 1-" + NpcTradeRotation.MAX_WEIGHT);
		return Optional.of(new NpcTradeRotation.Entry(pool, weight));
	}

	// requires

	private static NpcTradeRequirements requirements(JsonObject json, Side side) {
		onlyKeys(json, REQUIRES, "requires");
		NpcTradeRequirements.Path path = enumValue(NpcTradeRequirements.Path.class, GsonHelper.getAsString(json, "path", "any"), "requires.path");
		int degree = level(json, "degree_at_least");
		int stage = level(json, "stage_at_least");
		int rapport = GsonHelper.getAsInt(json, "rapport_at_least", 0);
		if (rapport < 0) throw new JsonSyntaxException("'rapport_at_least' must not be negative");
		Optional<ResourceLocation> advancement = json.has("advancement")
				? Optional.of(location(GsonHelper.getAsString(json, "advancement"))) : Optional.empty();
		boolean bloodActive = GsonHelper.getAsBoolean(json, "blood_active", false);
		// Q-2: an Unstained NPC refuses a Harbinger outright, so its cards can't ask for one.
		if (side == Side.UNSTAINED && (path == NpcTradeRequirements.Path.HARBINGER || degree > 0)) throw new JsonSyntaxException("An Unstained card can't require the Harbinger path; Unstained NPCs refuse Harbingers");
		return new NpcTradeRequirements(path, degree, stage, rapport, advancement, bloodActive);
	}

	private static int level(JsonObject json, String field) {
		int value = GsonHelper.getAsInt(json, field, 0);
		if (value < 0 || value > MAX_LEVEL) throw new JsonSyntaxException("'" + field + "' must be 0-" + MAX_LEVEL);
		return value;
	}

	// want

	private static NpcTradeWant want(JsonObject json, Side side, String speaker, Environment environment) {
		String kind = GsonHelper.getAsString(json, "kind");
		return switch (kind) {
			case "items" -> items(json, environment);
			case "blood" -> blood(json, side);
			case "bundle" -> bundle(json, environment);
			case "custom" -> custom(json, side, speaker);
			case "act" -> act(json, side);
			default -> throw new JsonSyntaxException("Unknown want kind '" + kind + "' (items, blood, bundle, custom, act)");
		};
	}

	private static NpcTradeWant.Items items(JsonObject json, Environment environment) {
		for (String key : json.keySet()) {
			if (!ITEMS_WANT.contains(key) && !key.startsWith(APPRAISAL_PREFIX)) throw new JsonSyntaxException("Unknown field '" + key + "' in an items want");
		}
		if (json.has("kind") && !GsonHelper.getAsString(json, "kind").equals("items")) throw new JsonSyntaxException("A bundle part must be an items want");
		NpcTradeMatch match = match(json.get("ingredient"), environment);
		int count = GsonHelper.getAsInt(json, "count", 1);
		if (count < 1 || count > MAX_WANT_COUNT) throw new JsonSyntaxException("Want count must be 1-" + MAX_WANT_COUNT);
		return new NpcTradeWant.Items(match, count, appraisal(json));
	}

	private static NpcTradeAppraisal appraisal(JsonObject json) {
		String grader = GsonHelper.getAsString(json, "appraisal", NpcTradeAppraisals.NONE);
		NpcTradeAppraisals.Spec spec = NpcTradeAppraisals.spec(grader).orElseThrow(() -> new JsonSyntaxException("Unknown appraisal '" + grader + "' " + NpcTradeAppraisals.graders()));
		Map<String, String> params = new HashMap<>();
		for (String key : json.keySet()) {
			if (!key.startsWith(APPRAISAL_PREFIX)) continue;
			String param = key.substring(APPRAISAL_PREFIX.length());
			if (!spec.params().contains(param)) throw new JsonSyntaxException("Appraisal '" + grader + "' does not read '" + key + "'");
			params.put(param, GsonHelper.getAsString(json, key));
		}
		try {
			spec.check().accept(params);
		} catch (IllegalArgumentException exception) {
			throw new JsonSyntaxException("Appraisal '" + grader + "': " + exception.getMessage());
		}
		return new NpcTradeAppraisal(grader, params);
	}

	private static NpcTradeMatch match(JsonElement json, Environment environment) {
		if (json == null) throw new JsonSyntaxException("Missing 'ingredient'");
		List<JsonElement> entries = new ArrayList<>();
		if (json.isJsonArray()) json.getAsJsonArray().forEach(entries::add);
		else entries.add(json);
		if (entries.isEmpty()) throw new JsonSyntaxException("'ingredient' is empty");
		List<ResourceLocation> items = new ArrayList<>();
		List<ResourceLocation> tags = new ArrayList<>();
		for (JsonElement element : entries) {
			if (!element.isJsonObject()) throw new JsonSyntaxException("An ingredient entry must be an object");
			JsonObject entry = element.getAsJsonObject();
			if (entry.has("type")) throw new JsonSyntaxException("Only item and tag ingredients are read");
			if (entry.has("item") == entry.has("tag")) throw new JsonSyntaxException("An ingredient entry needs exactly one of 'item' or 'tag'");
			if (entry.has("item")) {
				ResourceLocation item = location(GsonHelper.getAsString(entry, "item"));
				if (!environment.itemExists(item)) throw new JsonSyntaxException("Unknown item " + item);
				items.add(item);
			} else {
				tags.add(location(GsonHelper.getAsString(entry, "tag")));
			}
		}
		if (new HashSet<>(items).size() != items.size() || new HashSet<>(tags).size() != tags.size()) throw new JsonSyntaxException("An ingredient lists the same entry twice");
		return new NpcTradeMatch(items, tags);
	}

	private static NpcTradeWant.Blood blood(JsonObject json, Side side) {
		onlyKeys(json, Set.of("kind", "amount", "source"), "want(blood)");
		if (side != Side.HARBINGER) throw new JsonSyntaxException("A blood want is only for Harbinger speakers (Q-3)");
		int amount = GsonHelper.getAsInt(json, "amount");
		if (amount < 1) throw new JsonSyntaxException("Blood 'amount' must be at least 1 mB");
		String source = GsonHelper.getAsString(json, "source", NpcTradeWant.Blood.TITHE);
		if (!NpcTradeWant.Blood.SOURCES.contains(source)) throw new JsonSyntaxException("Unknown blood source '" + source + "' " + NpcTradeWant.Blood.SOURCES);
		return new NpcTradeWant.Blood(amount, source);
	}

	private static NpcTradeWant.Bundle bundle(JsonObject json, Environment environment) {
		onlyKeys(json, Set.of("kind", "parts"), "want(bundle)");
		JsonArray array = GsonHelper.getAsJsonArray(json, "parts");
		if (array.size() < NpcTradeWant.Bundle.MIN_PARTS || array.size() > NpcTradeWant.Bundle.MAX_PARTS) throw new JsonSyntaxException("A bundle takes " + NpcTradeWant.Bundle.MIN_PARTS + "-" + NpcTradeWant.Bundle.MAX_PARTS + " parts");
		List<NpcTradeWant.Items> parts = new ArrayList<>();
		for (JsonElement element : array) parts.add(items(GsonHelper.convertToJsonObject(element, "parts[]"), environment));
		return new NpcTradeWant.Bundle(parts);
	}

	private static NpcTradeWant.Custom custom(JsonObject json, Side side, String speaker) {
		onlyKeys(json, Set.of("kind", "act"), "want(custom)");
		if (side != Side.COURT) throw new JsonSyntaxException("A custom want is only for Brined Court speakers (Q-1)");
		String act = GsonHelper.getAsString(json, "act");
		String houseCustom = CourtHospitality.customAct(courtRole(speaker).house());
		if (!act.equals(houseCustom)) throw new JsonSyntaxException("Custom '" + act + "' is not the custom of " + speaker + "'s house (" + houseCustom + ")");
		return new NpcTradeWant.Custom(act);
	}

	private static NpcTradeWant.Act act(JsonObject json, Side side) {
		onlyKeys(json, Set.of("kind", "act"), "want(act)");
		if (side != Side.CIRCUS) throw new JsonSyntaxException("An act want is only for Circus speakers");
		String act = GsonHelper.getAsString(json, "act");
		if (!NpcTradeWant.Act.ACTS.contains(act)) throw new JsonSyntaxException("Unknown act '" + act + "' " + NpcTradeWant.Act.ACTS);
		return new NpcTradeWant.Act(act);
	}

	private static CourtRole courtRole(String speaker) {
		for (CourtRole role : CourtRole.values()) {
			if (role.entityId().equals(speaker)) return role;
		}
		throw new JsonSyntaxException("No Court role speaks as " + speaker);
	}

	private static boolean canGradeFine(NpcTradeWant want) {
		return want.itemParts().stream().anyMatch(part -> NpcTradeAppraisals.spec(part.appraisal().grader())
				.map(NpcTradeAppraisals.Spec::canGradeFine).orElse(false));
	}

	// give

	private static List<NpcTradeGive> gives(JsonObject root, String field, Environment environment) {
		if (!root.has(field)) return List.of();
		List<NpcTradeGive> gives = new ArrayList<>();
		for (JsonElement element : GsonHelper.getAsJsonArray(root, field)) {
			JsonObject json = GsonHelper.convertToJsonObject(element, field);
			onlyKeys(json, GIVE, field);
			ResourceLocation item = location(GsonHelper.getAsString(json, "item"));
			if (!environment.itemExists(item)) throw new JsonSyntaxException("Unknown item " + item);
			if (NpcTradeGiveRules.isBannedById(item) || environment.isMemoryItem(item)) throw new JsonSyntaxException("A trade may not give " + item + " (memories and station upgrades keep their own gates)");
			int count = GsonHelper.getAsInt(json, "count", 1);
			if (count < 1) throw new JsonSyntaxException("Give count must be at least 1");
			Optional<String> components = json.has("components")
					? Optional.of(GsonHelper.getAsJsonObject(json, "components").toString()) : Optional.empty();
			NpcTradeGive give = new NpcTradeGive(item, count, components);
			environment.checkGive(give);
			gives.add(give);
		}
		return List.copyOf(gives);
	}

	// stock

	private static NpcTradeStock stock(JsonObject json) {
		if (json == null) return NpcTradeStock.UNLIMITED_STOCK;
		onlyKeys(json, STOCK, "stock");
		int uses = GsonHelper.getAsInt(json, "uses", NpcTradeStock.UNLIMITED);
		int days = GsonHelper.getAsInt(json, "restock_days", 1);
		if (uses < 0) throw new JsonSyntaxException("'stock.uses' must not be negative (0 is unlimited)");
		if (days < 1) throw new JsonSyntaxException("'stock.restock_days' must be at least 1");
		return new NpcTradeStock(uses, days);
	}

	// helpers

	private static void onlyKeys(JsonObject json, Set<String> allowed, String where) {
		for (String key : json.keySet()) {
			if (!allowed.contains(key)) throw new JsonSyntaxException("Unknown field '" + key + "' in " + where);
		}
	}

	/** A lang key: records hold keys, never English. */
	private static String langKey(JsonObject json, String field) {
		String key = GsonHelper.getAsString(json, field).strip();
		if (key.isEmpty() || key.chars().anyMatch(Character::isWhitespace) || !key.contains(".")) throw new JsonSyntaxException("'" + field + "' must be a lang key, not text");
		return key;
	}

	private static ResourceLocation location(String value) {
		ResourceLocation location = ResourceLocation.tryParse(value);
		if (location == null) throw new JsonSyntaxException("Bad resource location '" + value + "'");
		return location;
	}

	private static <E extends Enum<E>> E enumValue(Class<E> type, String value, String field) {
		try {
			return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new JsonSyntaxException("Unknown '" + field + "' value '" + value + "'");
		}
	}
}

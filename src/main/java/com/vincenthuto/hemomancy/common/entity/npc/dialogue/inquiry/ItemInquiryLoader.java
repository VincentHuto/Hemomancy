package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import com.google.gson.*;
import com.vincenthuto.hemomancy.common.data.JsonResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Loads item-inquiry dialogue entries from datapack JSON files and populates
 * {@link ItemInquiryRegistry}.
 *
 * <h3>File location</h3>
 * <pre>
 * data/&lt;namespace&gt;/dialogue_inquiry/&lt;npc_id&gt;/&lt;item_namespace&gt;/&lt;item_path&gt;.json
 * </pre>
 * Where {@code npc_id} is one of: {@code vicar}, {@code alchemist},
 * {@code zealot}, {@code guardian} (or any custom NPC identifier).
 * The item's full registry ID is reconstructed as
 * {@code &lt;item_namespace&gt;:&lt;item_path&gt;}.
 *
 * <h3>Simple format</h3>
 * <pre>{@code
 * { "lines": ["translation.key.line1", "translation.key.line2"] }
 * }</pre>
 *
 * <h3>Conditional format</h3>
 * Branches are evaluated top-to-bottom; the first matching branch is used.
 * Every branch must have a {@code lines} field.  Omitting a condition field
 * means that direction is unconstrained (always passes).
 * <pre>{@code
 * {
 *   "conditions": [
 *     { "max_degree": 2, "lines": ["key.low"] },
 *     { "max_degree": 4, "lines": ["key.mid.1", "key.mid.2"] },
 *     { "lines": ["key.high.1", "key.high.2"] }
 *   ]
 * }
 * }</pre>
 * Supported condition keys: {@code min_degree}, {@code max_degree},
 * {@code min_purity}, {@code max_purity}.
 */
public class ItemInquiryLoader extends JsonResourceReloadListener<ItemInquiryLoader.Inquiry> {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final String FOLDER = "dialogue_inquiry";

	/** One file: what an NPC says about an item. */
	record Inquiry(String npcId, ResourceLocation itemId, ItemInquiryEntry entry) {
	}

	public ItemInquiryLoader() {
		super(FOLDER, "item inquiry", LOGGER, ItemInquiryLoader::parse, ItemInquiryLoader::publish);
	}

	// Expected path: dialogue_inquiry/<npc_id>/<item_ns>/<item_path>.json; item_path may hold slashes.
	private static Inquiry parse(ResourceLocation id, JsonObject root) {
		String[] segments = id.getPath().split("/");
		if (segments.length < 3) {
			throw new IllegalArgumentException("Malformed path (need npc_id/item_ns/item_path)");
		}
		ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(segments[1],
				String.join("/", Arrays.copyOfRange(segments, 2, segments.length)));
		ResourceLocation source = ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
				FOLDER + "/" + id.getPath() + ".json");
		ItemInquiryEntry entry = parseEntry(root, source);
		if (entry == null) {
			throw new IllegalArgumentException("No inquiry lines");
		}
		return new Inquiry(segments[0], itemId, entry);
	}

	private static void publish(Map<ResourceLocation, Inquiry> loaded) {
		Map<String, Map<ResourceLocation, ItemInquiryEntry>> tables = new HashMap<>();
		loaded.values().forEach(inquiry -> tables.computeIfAbsent(inquiry.npcId(), npc -> new HashMap<>())
				.put(inquiry.itemId(), inquiry.entry()));
		ItemInquiryRegistry.INSTANCE.reload(tables);
	}

	// ── JSON parsing helpers ──────────────────────────────────────────────────

	private static ItemInquiryEntry parseEntry(JsonObject root, ResourceLocation src) {
		// Conditional format: { "conditions": [ { ... }, ... ] }
		if (root.has("conditions")) {
			JsonArray arr = root.getAsJsonArray("conditions");
			List<ItemInquiryCondition> branches = new ArrayList<>(arr.size());
			for (JsonElement el : arr) {
				ItemInquiryCondition branch = parseBranch(el.getAsJsonObject(), src);
				if (branch != null) branches.add(branch);
			}
			if (branches.isEmpty()) {
				LOGGER.warn("[ItemInquiryLoader] No valid branches in conditional entry: {}", src);
				return null;
			}
			return new ItemInquiryEntry(branches);
		}

		// Simple format: { "lines": [ "key1", "key2" ] }
		if (root.has("lines")) {
			List<String> lines = parseLines(root.getAsJsonArray("lines"));
			if (lines.isEmpty()) {
				LOGGER.warn("[ItemInquiryLoader] Empty lines array in: {}", src);
				return null;
			}
			return new ItemInquiryEntry(List.of(ItemInquiryCondition.unconditional(lines)));
		}

		LOGGER.warn("[ItemInquiryLoader] Entry has neither 'lines' nor 'conditions': {}", src);
		return null;
	}

	private static ItemInquiryCondition parseBranch(JsonObject obj, ResourceLocation src) {
		if (!obj.has("lines")) {
			LOGGER.warn("[ItemInquiryLoader] Condition branch missing 'lines' in: {}", src);
			return null;
		}
		List<String> lines = parseLines(obj.getAsJsonArray("lines"));
		if (lines.isEmpty()) {
			LOGGER.warn("[ItemInquiryLoader] Condition branch has empty 'lines' in: {}", src);
			return null;
		}

		int minDegree = parseIntField(obj, "min_degree", -1, src);
		int maxDegree = parseIntField(obj, "max_degree", -1, src);
		float minPurity = parseFloatField(obj, "min_purity", -1f, src);
		float maxPurity = parseFloatField(obj, "max_purity", -1f, src);
		float minClarity = parseFloatField(obj, "min_clarity", -1f, src);
		float maxClarity = parseFloatField(obj, "max_clarity", -1f, src);
		Boolean clarityUnlocked = parseBooleanField(obj, "clarity_unlocked", src);
		Boolean requiresActiveBlood = parseBooleanField(obj, "requires_active_blood", src);
		Boolean requiresPurifying = parseBooleanField(obj, "requires_purifying", src);

		if (!validRange(minDegree, maxDegree) || !validRange(minPurity, maxPurity)
				|| !validRange(minClarity, maxClarity) || !validPercentage(minPurity)
				|| !validPercentage(maxPurity) || !validPercentage(minClarity)
				|| !validPercentage(maxClarity)) {
			LOGGER.warn("[ItemInquiryLoader] Invalid condition range in: {}", src);
			return null;
		}

		return new ItemInquiryCondition(minDegree, maxDegree, minPurity, maxPurity, minClarity, maxClarity,
				clarityUnlocked, requiresActiveBlood, requiresPurifying, lines);
	}

	private static int parseIntField(JsonObject obj, String field, int def, ResourceLocation src) {
		if (!obj.has(field)) return def;
		try {
			return obj.get(field).getAsInt();
		} catch (Exception ex) {
			LOGGER.warn("[ItemInquiryLoader] Field '{}' is not an integer in: {} — using default {}",
					field, src, def);
			return def;
		}
	}

	private static float parseFloatField(JsonObject obj, String field, float def, ResourceLocation src) {
		if (!obj.has(field)) return def;
		try {
			return obj.get(field).getAsFloat();
		} catch (Exception ex) {
			LOGGER.warn("[ItemInquiryLoader] Field '{}' is not a number in: {} — using default {}",
					field, src, def);
			return def;
		}
	}

	private static Boolean parseBooleanField(JsonObject obj, String field, ResourceLocation src) {
		if (!obj.has(field)) return null;
		try {
			return obj.get(field).getAsBoolean();
		} catch (Exception ex) {
			LOGGER.warn("[ItemInquiryLoader] Field '{}' is not a boolean in: {}", field, src);
			return null;
		}
	}

	private static boolean validRange(double min, double max) {
		return min < 0 || max < 0 || min <= max;
	}

	private static boolean validPercentage(float value) {
		return value < 0 || value <= 100F;
	}

	private static List<String> parseLines(JsonArray arr) {
		List<String> lines = new ArrayList<>(arr.size());
		for (JsonElement el : arr) {
			String key = el.getAsString().trim();
			if (!key.isEmpty()) lines.add(key);
		}
		return lines;
	}

}

package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

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
public class ItemInquiryLoader
		extends SimplePreparableReloadListener<Map<String, Map<ResourceLocation, ItemInquiryEntry>>> {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new GsonBuilder().create();
	private static final String FOLDER = "dialogue_inquiry";

	@Override
	protected Map<String, Map<ResourceLocation, ItemInquiryEntry>> prepare(
			ResourceManager manager, ProfilerFiller profiler) {

		Map<String, Map<ResourceLocation, ItemInquiryEntry>> result = new HashMap<>();

		// Scan all resources whose path begins with "dialogue_inquiry/"
		Map<ResourceLocation, Resource> resources = manager.listResources(
				FOLDER, loc -> loc.getPath().endsWith(".json"));

		for (Map.Entry<ResourceLocation, Resource> e : resources.entrySet()) {
			ResourceLocation fileLocation = e.getKey();

			// Expected path: dialogue_inquiry/<npc_id>/<item_ns>/<item_path>.json
			// item_path may itself contain slashes for nested registry paths.
			String path = fileLocation.getPath(); // e.g. dialogue_inquiry/vicar/hemomancy/bloody_vial.json
			String afterFolder = path.substring(FOLDER.length() + 1); // vicar/hemomancy/bloody_vial.json
			String[] segments = afterFolder.split("/");
			if (segments.length < 3) {
				LOGGER.warn("[ItemInquiryLoader] Skipping malformed path (need npc_id/item_ns/item_path): {}", fileLocation);
				continue;
			}
			String npcId = segments[0];   // vicar
			String itemNs = segments[1];  // hemomancy
			// Rejoin any remaining segments as the item path (supports nested paths)
			String rawItemPath = String.join("/", Arrays.copyOfRange(segments, 2, segments.length));
			String itemPath = rawItemPath.endsWith(".json")
					? rawItemPath.substring(0, rawItemPath.length() - ".json".length())
					: rawItemPath;

			ResourceLocation itemId;
			try {
				itemId = ResourceLocation.fromNamespaceAndPath(itemNs, itemPath);
			} catch (Exception ex) {
				LOGGER.warn("[ItemInquiryLoader] Skipping invalid item ID '{}/{}' in: {} — {}",
						itemNs, itemPath, fileLocation, ex.getMessage());
				continue;
			}

			try (InputStream stream = e.getValue().open();
				 InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {

				JsonObject root = GSON.fromJson(reader, JsonObject.class);
				ItemInquiryEntry entry = parseEntry(root, fileLocation);
				if (entry != null) {
					result.computeIfAbsent(npcId, k -> new HashMap<>()).put(itemId, entry);
				}

			} catch (IOException ex) {
				LOGGER.error("[ItemInquiryLoader] Failed to read {}: {}", fileLocation, ex.getMessage());
			} catch (Exception ex) {
				LOGGER.error("[ItemInquiryLoader] Failed to parse {}: {}", fileLocation, ex.getMessage());
			}
		}

		LOGGER.info("[ItemInquiryLoader] Loaded {} NPC inquiry tables", result.size());
		return result;
	}

	@Override
	protected void apply(Map<String, Map<ResourceLocation, ItemInquiryEntry>> prepared,
			ResourceManager manager, ProfilerFiller profiler) {
		ItemInquiryRegistry.INSTANCE.reload(prepared);
		int total = prepared.values().stream().mapToInt(Map::size).sum();
		LOGGER.info("[ItemInquiryLoader] Applied {} item-inquiry entries across {} NPC types",
				total, prepared.size());
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

		int minDegree   = parseIntField(obj, "min_degree", -1, src);
		int maxDegree   = parseIntField(obj, "max_degree", -1, src);
		float minPurity = parseFloatField(obj, "min_purity", -1f, src);
		float maxPurity = parseFloatField(obj, "max_purity", -1f, src);

		return new ItemInquiryCondition(minDegree, maxDegree, minPurity, maxPurity, lines);
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

	private static List<String> parseLines(JsonArray arr) {
		List<String> lines = new ArrayList<>(arr.size());
		for (JsonElement el : arr) {
			String key = el.getAsString().trim();
			if (!key.isEmpty()) lines.add(key);
		}
		return lines;
	}

}

package com.vincenthuto.hemomancy.common.rite.floor;

import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CardinalRiteFloorRegistry {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final int MAX_SYNCED_FLOORS = 256;
	public static final int MAX_SYNCED_JSON_LENGTH = 32767;
	private static volatile Map<ResourceLocation, CardinalRiteFloorDefinition> definitions = Map.of();
	private static volatile Map<ResourceLocation, String> sources = Map.of();

	private CardinalRiteFloorRegistry() {
	}

	static void reload(Map<ResourceLocation, CardinalRiteFloorDefinition> loaded) {
		definitions = Map.copyOf(loaded);
	}

	static void reloadSourced(Map<ResourceLocation, CardinalRiteFloorLoader.Sourced> loaded) {
		Map<ResourceLocation, CardinalRiteFloorDefinition> parsed = new HashMap<>();
		Map<ResourceLocation, String> json = new HashMap<>();
		loaded.forEach((id, sourced) -> {
			parsed.put(id, sourced.definition());
			json.put(id, sourced.json());
		});
		sources = Map.copyOf(json);
		reload(parsed);
	}

	/** The server's floor JSON, sent to remote clients so their blueprints and floor visuals agree. */
	public static Map<ResourceLocation, String> syncSnapshot() {
		return sources;
	}

	/** Replaces this client's floors with the server's. Malformed entries are skipped, not trusted. */
	public static void receiveSync(Map<ResourceLocation, String> synced) {
		Map<ResourceLocation, CardinalRiteFloorDefinition> parsed = new HashMap<>();
		synced.forEach((id, json) -> {
			try {
				parsed.put(id, CardinalRiteFloorLoader.parse(id, JsonParser.parseString(json).getAsJsonObject()));
			} catch (RuntimeException exception) {
				LOGGER.warn("Ignoring synced Cardinal Rite floor {}: {}", id, exception.getMessage());
			}
		});
		sources = Map.copyOf(synced);
		reload(parsed);
	}

	public static Optional<CardinalRiteFloorDefinition> get(ResourceLocation id) {
		return Optional.ofNullable(definitions.get(id));
	}

	public static List<CardinalRiteFloorDefinition> highestTierFirst() {
		return definitions.values().stream()
				.sorted(Comparator.comparingInt((CardinalRiteFloorDefinition floor) ->
						floor.tier().ordinal()).reversed().thenComparing(floor -> floor.id().toString()))
				.toList();
	}

	public static Map<ResourceLocation, CardinalRiteFloorDefinition> all() {
		return definitions;
	}
}

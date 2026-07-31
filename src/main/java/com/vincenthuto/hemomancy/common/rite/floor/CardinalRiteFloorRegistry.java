package com.vincenthuto.hemomancy.common.rite.floor;

import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CardinalRiteFloorRegistry {
	private static volatile Map<ResourceLocation, CardinalRiteFloorDefinition> definitions = Map.of();

	private CardinalRiteFloorRegistry() {
	}

	static void reload(Map<ResourceLocation, CardinalRiteFloorDefinition> loaded) {
		definitions = Map.copyOf(loaded);
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

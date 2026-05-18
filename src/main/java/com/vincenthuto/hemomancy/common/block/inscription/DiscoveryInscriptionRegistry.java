package com.vincenthuto.hemomancy.common.block.inscription;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DiscoveryInscriptionRegistry {
	private static final Map<ResourceLocation, DiscoveryInscriptionDefinition> DEFINITIONS = new HashMap<>();

	private DiscoveryInscriptionRegistry() {}

	public static void reload(Map<ResourceLocation, DiscoveryInscriptionDefinition> definitions) {
		DEFINITIONS.clear();
		DEFINITIONS.putAll(definitions);
	}

	public static Optional<DiscoveryInscriptionDefinition> get(ResourceLocation id) {
		return Optional.ofNullable(DEFINITIONS.get(id));
	}
}

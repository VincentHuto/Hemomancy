package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class IchorianSigilRegistry {
	private static volatile Map<ResourceLocation, IchorianSigilDefinition> definitions = Map.of();

	private IchorianSigilRegistry() {
	}

	public static IchorianSigilDefinition get(ResourceLocation id) {
		return definitions.get(id);
	}

	public static Collection<IchorianSigilDefinition> all() {
		return definitions.values();
	}

	public static Map<ResourceLocation, IchorianSigilDefinition> snapshot() {
		return definitions;
	}

	public static void reload(Map<ResourceLocation, IchorianSigilDefinition> loaded) {
		definitions = Map.copyOf(new ConcurrentHashMap<>(loaded));
	}
}

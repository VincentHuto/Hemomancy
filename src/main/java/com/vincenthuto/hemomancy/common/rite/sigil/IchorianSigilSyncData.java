package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record IchorianSigilSyncData(Map<ResourceLocation, IchorianSigilDefinition> definitions) {
	public IchorianSigilSyncData {
		definitions = Map.copyOf(definitions);
	}

	public static IchorianSigilSyncData capture() {
		return new IchorianSigilSyncData(IchorianSigilRegistry.snapshot());
	}
}

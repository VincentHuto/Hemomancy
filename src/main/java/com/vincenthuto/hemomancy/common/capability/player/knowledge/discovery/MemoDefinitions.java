package com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.resources.ResourceLocation;

public final class MemoDefinitions {
	private static final Map<ResourceLocation, MemoDefinition> DEFINITIONS = new LinkedHashMap<>();

	public static final MemoDefinition FIRST_RITE_NOTES = register(new MemoDefinition(
			Hemomancy.rloc("first_rite_notes"),
			LiberEntryDefinitions.FIRST_RITE_NOTES,
			MemoDefinition.MemoPath.HARBINGER));

	private MemoDefinitions() {
	}

	public static Optional<MemoDefinition> get(ResourceLocation id) {
		return Optional.ofNullable(DEFINITIONS.get(id));
	}

	public static Collection<MemoDefinition> all() {
		return DEFINITIONS.values();
	}

	private static MemoDefinition register(MemoDefinition definition) {
		DEFINITIONS.put(definition.id(), definition);
		return definition;
	}
}

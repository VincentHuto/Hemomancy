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

	public static final MemoDefinition PALE_LADY_NOTES = register(new MemoDefinition(
			Hemomancy.rloc("pale_lady_notes"),
			LiberEntryDefinitions.IMMACULATUS_SHE_WHO_LISTENS,
			MemoDefinition.MemoPath.UNSTAINED));

	public static final MemoDefinition FUNGAL_WHISPER_ADEPT = register(new MemoDefinition(
			Hemomancy.rloc("fungal_whisper_adept"),
			LiberEntryDefinitions.HYPHAE,
			MemoDefinition.MemoPath.HARBINGER));

	public static final MemoDefinition FUNGAL_WHISPER_ILLUMINATUS = register(new MemoDefinition(
			Hemomancy.rloc("fungal_whisper_illuminatus"),
			LiberEntryDefinitions.HYPHAE,
			MemoDefinition.MemoPath.HARBINGER));

	public static final MemoDefinition FUNGAL_WHISPER_SANCTIFIED = register(new MemoDefinition(
			Hemomancy.rloc("fungal_whisper_sanctified"),
			LiberEntryDefinitions.ENTITY,
			MemoDefinition.MemoPath.HARBINGER));

	public static final MemoDefinition FUNGAL_WHISPER_ARCHON = register(new MemoDefinition(
			Hemomancy.rloc("fungal_whisper_archon"),
			LiberEntryDefinitions.ENTITY,
			MemoDefinition.MemoPath.HARBINGER));

	public static final MemoDefinition FUNGAL_WHISPER_TRUTH = register(new MemoDefinition(
			Hemomancy.rloc("fungal_whisper_truth"),
			LiberEntryDefinitions.TRUTH,
			MemoDefinition.MemoPath.HARBINGER));

	public static final MemoDefinition QLIPHOTH_COMMUNION = register(new MemoDefinition(
			Hemomancy.rloc("qliphoth_communion"),
			LiberEntryDefinitions.QLIPHOTH,
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

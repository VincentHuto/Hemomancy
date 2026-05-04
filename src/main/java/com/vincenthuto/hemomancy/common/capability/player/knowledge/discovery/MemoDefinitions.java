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

	public static final MemoDefinition PALE_LADY_TAINTED = register(new MemoDefinition(
			Hemomancy.rloc("pale_lady_tainted"),
			LiberEntryDefinitions.IMMACULATUS_SHE_WHO_LISTENS,
			MemoDefinition.MemoPath.UNSTAINED));

	public static final MemoDefinition PALE_LADY_CLEANSING = register(new MemoDefinition(
			Hemomancy.rloc("pale_lady_cleansing"),
			LiberEntryDefinitions.IMMACULATUS_SHE_WHO_LISTENS,
			MemoDefinition.MemoPath.UNSTAINED));

	public static final MemoDefinition PALE_LADY_ABSOLVED = register(new MemoDefinition(
			Hemomancy.rloc("pale_lady_absolved"),
			LiberEntryDefinitions.IMMACULATUS_SHE_WHO_LISTENS,
			MemoDefinition.MemoPath.UNSTAINED));

	public static final MemoDefinition PALE_LADY_PURIFIED = register(new MemoDefinition(
			Hemomancy.rloc("pale_lady_purified"),
			LiberEntryDefinitions.IMMACULATUS_SHE_WHO_LISTENS,
			MemoDefinition.MemoPath.UNSTAINED));

	public static final MemoDefinition PALE_LADY_DISCERNING = register(new MemoDefinition(
			Hemomancy.rloc("pale_lady_discerning"),
			LiberEntryDefinitions.IMMACULATUS_CLARITY_PRICE,
			MemoDefinition.MemoPath.UNSTAINED));

	public static final MemoDefinition PALE_LADY_VIGILANT = register(new MemoDefinition(
			Hemomancy.rloc("pale_lady_vigilant"),
			LiberEntryDefinitions.IMMACULATUS_CLARITY_PRICE,
			MemoDefinition.MemoPath.UNSTAINED));

	public static final MemoDefinition PALE_LADY_RESOLUTE = register(new MemoDefinition(
			Hemomancy.rloc("pale_lady_resolute"),
			LiberEntryDefinitions.IMMACULATUS_CLARITY_PRICE,
			MemoDefinition.MemoPath.UNSTAINED));

	public static final MemoDefinition PALE_LADY_ENLIGHTENED = register(new MemoDefinition(
			Hemomancy.rloc("pale_lady_enlightened"),
			LiberEntryDefinitions.IMMACULATUS_CLARITY_PRICE,
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

	public static final MemoDefinition ANNETTA_INSECT_OBSERVATION = register(new MemoDefinition(
			Hemomancy.rloc("annetta_insect_observation"),
			LiberEntryDefinitions.ANNETTA_KNOWLES_GEODE,
			MemoDefinition.MemoPath.HARBINGER));

	public static final MemoDefinition ANNETTA_INSECT_OBSERVATION_IMMACULATUS = register(new MemoDefinition(
			Hemomancy.rloc("annetta_insect_observation_immaculatus"),
			LiberEntryDefinitions.IMMACULATUS_ANNETTA_GEODE,
			MemoDefinition.MemoPath.UNSTAINED));

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

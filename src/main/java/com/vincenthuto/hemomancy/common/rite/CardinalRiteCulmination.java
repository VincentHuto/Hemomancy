package com.vincenthuto.hemomancy.common.rite;

import java.util.Locale;

/**
 * How a completed rite visibly resolves. Each family of rites ends with its own
 * beat instead of one shared burst at the caster.
 */
public enum CardinalRiteCulmination {
	/** Rank rites: a blood pillar rises through the caster as the daemon merges. */
	ASCENSION,
	/** Terrain rites: the pulse runs through the floor and out into the ground. */
	GROUNDING,
	/** Bloodline and covenant rites: vessels run from the Focus to each participant. */
	BINDING,
	/** Vessel and station rites: the result condenses out of the daemon onto the Focus. */
	FORGING,
	/** Territorial rites: the boundary swells out to the zone edge, then sinks. */
	DOMAIN;

	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static CardinalRiteCulmination byId(String id) {
		if (id == null || id.isBlank()) return null;
		for (CardinalRiteCulmination value : values()) {
			if (value.id().equals(id.trim().toLowerCase(Locale.ROOT))) return value;
		}
		return null;
	}

	/**
	 * Resolves the authored culmination, then rank rites ascend, then the rite's
	 * purpose decides. Rites without a purpose (Unstained, legacy) keep the
	 * item and floor-style fallback.
	 */
	public static CardinalRiteCulmination resolve(String authored, boolean rankup, CardinalRitePurpose purpose,
			boolean producesItem, String floorStyle) {
		CardinalRiteCulmination explicit = byId(authored);
		if (explicit != null) return explicit;
		if (rankup) return ASCENSION;
		if (purpose != null) return purpose.defaultCulmination();
		return resolve("", false, producesItem, floorStyle);
	}

	/**
	 * Resolves the authored culmination, falling back to one implied by the rite:
	 * rank rites ascend, item-producing rites forge, then the floor style decides.
	 */
	public static CardinalRiteCulmination resolve(String authored, boolean rankup, boolean producesItem,
			String floorStyle) {
		CardinalRiteCulmination explicit = byId(authored);
		if (explicit != null) return explicit;
		if (rankup) return ASCENSION;
		if (producesItem) return FORGING;
		if (floorStyle == null) return FORGING;
		return switch (floorStyle) {
			case "dominion" -> DOMAIN;
			case "communion" -> BINDING;
			case "working" -> GROUNDING;
			default -> ASCENSION;
		};
	}
}

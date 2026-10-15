package com.vincenthuto.hemomancy.common.rite;

import java.util.Locale;

/**
 * Where the daemon goes when a rite culminates. Most rites draw it back into
 * the caster. A world rite sends it out into the world instead: up into the
 * sky, which flashes red where it strikes, or up and then down into the ground,
 * which cracks open in blazing red and black. Working crafts dive it into
 * their structure (see {@code WorkingCraftDaemonPath}).
 */
public enum CardinalRiteDaemonFinale {
	MERGE,
	SKY,
	GROUND;

	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	/** A world rite's authored {@code world_finale}, defaulting to the ground; every other rite merges. */
	public static CardinalRiteDaemonFinale resolve(CardinalRitePurpose purpose, String authored) {
		if (purpose != CardinalRitePurpose.WORLD) return MERGE;
		return authored != null && SKY.id().equals(authored.trim().toLowerCase(Locale.ROOT)) ? SKY : GROUND;
	}

	/** @return why an authored {@code world_finale} is invalid, or null when it is fine or absent */
	public static String violation(CardinalRitePurpose purpose, String authored) {
		if (authored == null) return null;
		String id = authored.trim().toLowerCase(Locale.ROOT);
		if (!id.equals(SKY.id()) && !id.equals(GROUND.id())) return "names world_finale '" + authored + "'; use sky or ground";
		return purpose == CardinalRitePurpose.WORLD ? null : "names a world_finale but is not a world rite";
	}
}

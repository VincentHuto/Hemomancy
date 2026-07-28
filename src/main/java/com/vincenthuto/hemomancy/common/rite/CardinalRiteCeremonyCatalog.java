package com.vincenthuto.hemomancy.common.rite;

import java.util.HashMap;
import java.util.Map;

/**
 * Explicit authored setup direction for every built-in Harbinger rite. The
 * catalog keeps legacy datapacks compatible while ensuring built-in rites do
 * not silently collapse to one visually identical cardinal cross.
 */
public final class CardinalRiteCeremonyCatalog {
	public enum Layout {
		CARDINAL,
		DIAGONAL,
		CROOKED,
		SERPENTINE;

		public static Layout byName(String name) {
			if (name == null) return CARDINAL;
			try {
				return valueOf(name.trim().toUpperCase(java.util.Locale.ROOT));
			} catch (IllegalArgumentException ignored) {
				return CARDINAL;
			}
		}
	}
	public record Spec(Layout layout, int rotation, CardinalRiteCeremonyProfile profile) {}

	private static final Map<String, Spec> SPECS = new HashMap<>();

	static {
		full(Layout.CARDINAL, 0, "sanguine_initiation", "hematic_unbinding", "sanguine_fervor",
				"eternal_covenant", "ancestral_communion", "apotheos_rite");
		full(Layout.CARDINAL, 1, "votary_rite", "exsanguination", "sanguine_dominion", "archon_rite");
		full(Layout.DIAGONAL, 0, "initiate_rite", "hungering_earth", "scarlet_summons",
				"bloom_of_qliphoth");
		full(Layout.DIAGONAL, 1, "sanguine_brotherhood", "bloodline_founding", "sanguine_eclipse",
				"sanctified_rite");
		full(Layout.CROOKED, 0, "illuminatus_rite", "founding_fane", "pallid_shadow");
		full(Layout.CROOKED, 2, "chamber_of_will", "pruning_of_qliphoth");
		abbreviated(Layout.SERPENTINE, 0, "sanguine_attunement", "crimson_beacon",
				"vascular_mending", "bloodline_recall", "pallid_vessel_rite");
		abbreviated(Layout.SERPENTINE, 1, "hematic_fortification", "crimson_vessel_rite",
				"ashen_vessel_rite", "horn_of_culmination_rite");
	}

	private CardinalRiteCeremonyCatalog() {}

	public static boolean hasAuthoredSpec(String path) {
		return SPECS.containsKey(shortPath(path));
	}

	public static Spec spec(String path) {
		return SPECS.get(shortPath(path));
	}

	private static void full(Layout layout, int rotation, String... paths) {
		add(layout, rotation, CardinalRiteCeremonyProfile.FULL, paths);
	}

	private static void abbreviated(Layout layout, int rotation, String... paths) {
		add(layout, rotation, CardinalRiteCeremonyProfile.ABBREVIATED, paths);
	}

	private static void add(Layout layout, int rotation, CardinalRiteCeremonyProfile profile, String... paths) {
		for (String path : paths) SPECS.put(path, new Spec(layout, rotation, profile));
	}

	private static String shortPath(String path) {
		int slash = path == null ? -1 : path.lastIndexOf('/');
		return slash < 0 ? path : path.substring(slash + 1);
	}
}

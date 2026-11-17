package com.vincenthuto.hemomancy.common.entity.mob;

import java.util.Map;
import java.util.Optional;

/**
 * The Gardens dimension's roster at D7 to D8 strength (CR-2, NQ-06-3). Only mobs that spawn naturally in the
 * dimension take these numbers (NQ-06-2): the Overworld biome, the savanna mounds and the Mycophant's brood keep
 * their own.
 */
public final class GardensRosterRules {
	public record Stats(double maxHealth, double armor, double attackDamage) {
	}

	private static final Map<String, Stats> ROSTER = Map.of(
			"fungling", new Stats(16.0D, 0.0D, 4.0D),
			"chthonian", new Stats(30.0D, 6.0D, 6.0D),
			"fargone", new Stats(36.0D, 0.0D, 7.0D),
			"thirster", new Stats(44.0D, 0.0D, 8.0D),
			"lump_of_thought", new Stats(52.0D, 0.0D, 9.0D),
			"hunter_fungus", new Stats(56.0D, 4.0D, 10.0D),
			"erythromycelium_eruptus", new Stats(64.0D, 4.0D, 11.0D),
			"abhorrent_thought", new Stats(90.0D, 8.0D, 13.0D));

	private GardensRosterRules() {
	}

	/** The raised numbers for a Hemomancy entity id path, or empty when the mob is not on the roster. */
	public static Optional<Stats> stats(String entityPath) {
		return Optional.ofNullable(ROSTER.get(entityPath));
	}

	public static boolean appliesTo(boolean inGardensDimension, boolean naturalSpawn) {
		return inGardensDimension && naturalSpawn;
	}
}

package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WillCombatRulesTest {
	private static final String RULES = "src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillCombatRules.java";
	private static final String[] SCHOOLS = {
			"ANIMUS", "FLAMMEUS", "DUCTILIS", "LUX", "MORTEM", "CONGEATIO", "FERRIC", "TENEBRIS"
	};
	private static final String[] REQUIRED_KIT_IDS = {
			"blood_shot", "blood_needle", "blood_aneurysm", "blood_cloud",
			"crimson_flame_conjuration", "sanguine_ignition", "scalding_updraft", "vitric_combustion",
			"deadly_gaze", "hemolymphal_pulse", "activation_potential",
			"crimson_sight", "prismatic_reproof", "unclosing_eye",
			"hemorrhage", "exsanguinate", "bloom_of_rot",
			"cryogenic_pulse", "glacial_grasp", "glacial_bastion", "glacial_rampart",
			"pyretic_forge", "void_shroud", "umbral_step", "blood_eclipse", "blood_eclipse_mantle"
	};

	private WillCombatRulesTest() {
	}

	public static void main(String[] args) throws IOException {
		String source = Files.readString(Path.of(RULES));
		brokenStatsAreFixedByTier(source);
		sentStatsScaleFromTargetSnapshot(source);
		schoolKitsCoverEveryTendency(source);
		counterSchoolCoversEveryTendency(source);
		falterAndCadenceConstantsAreStable(source);
	}

	private static void brokenStatsAreFixedByTier(String source) {
		assertContains("fixed broken health", source, "BROKEN_HEALTH = { 20.0D, 30.0D, 34.0D, 38.0D }");
		assertContains("fixed broken damage", source, "BROKEN_DAMAGE = { 3.0D, 5.0D, 6.0D, 6.0D }");
		assertContains("fixed broken speed", source, "BROKEN_SPEED = { 0.24D, 0.26D, 0.27D, 0.28D }");
		assertContains("tier clamps low and high", source, "Math.max(1, Math.min(4, tier))");
	}

	private static void sentStatsScaleFromTargetSnapshot(String source) {
		assertContains("sent stats method exists", source, "sentStats(int playerDegree, double playerMaxHealth)");
		assertContains("sent degree floors at six", source, "Math.max(6, playerDegree)");
		assertContains("sent health scales with player max health", source, "playerMaxHealth * 1.2D");
		assertContains("sent damage scales by degree", source, "Math.max(0, degree - 6) * 1.25D");
	}

	private static void schoolKitsCoverEveryTendency(String source) {
		for (String school : SCHOOLS) {
			assertContains("kit covers " + school, source, "EnumBloodTendency." + school);
		}
		for (String id : REQUIRED_KIT_IDS) {
			assertContains("kit id " + id + " is authored", source, "\"" + id + "\"");
		}
		assertContains("kits return hemomancy ids", source, "map(Hemomancy::rloc)");
	}

	private static void counterSchoolCoversEveryTendency(String source) {
		for (String school : SCHOOLS) {
			int occurrences = count(source, "EnumBloodTendency." + school);
			assertTrue("counter and kit mention " + school, occurrences >= 2);
		}
		assertContains("counter fallback remains stable", source,
				"COUNTERS.getOrDefault(school, EnumBloodTendency.MORTEM)");
	}

	private static void falterAndCadenceConstantsAreStable(String source) {
		assertContains("falter fraction", source, "FALTER_FRACTION = 0.25D");
		assertContains("falter window", source, "FALTER_WINDOW_TICKS = 100");
		assertContains("tier one cadence", source, "case 1 -> 100");
		assertContains("tier two cadence", source, "case 2 -> 80");
		assertContains("tier three cadence", source, "case 3 -> 70");
		assertContains("tier four cadence", source, "default -> 60");
	}

	private static int count(String source, String needle) {
		int found = 0;
		int index = 0;
		while ((index = source.indexOf(needle, index)) >= 0) {
			found++;
			index += needle.length();
		}
		return found;
	}

	private static void assertContains(String label, String source, String expected) {
		if (!source.contains(expected)) {
			throw new AssertionError(label + ": missing `" + expected + "`");
		}
	}

	private static void assertTrue(String label, boolean condition) {
		if (!condition) {
			throw new AssertionError(label);
		}
	}
}

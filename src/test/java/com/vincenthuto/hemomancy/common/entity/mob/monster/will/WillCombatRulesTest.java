package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;

public final class WillCombatRulesTest {
	private static final String RULES = "src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillCombatRules.java";
	private static final String[] SCHOOLS = {
			"ANIMUS", "FLAMMEUS", "DUCTILIS", "LUX", "MORTEM", "CONGEATIO", "FERRIC", "TENEBRIS"
	};
	private static final String[] REQUIRED_KIT_IDS = {
			"blood_shot", "blood_needle", "blood_aneurysm", "blood_cloud",
			"crimson_flame_conjuration", "sanguine_ignition", "scalding_updraft", "vitric_combustion",
			"deadly_gaze", "hemolymphal_pulse", "activation_potential",
			"hematic_flare", "hematic_beacon", "prismatic_reproof",
			"hemorrhage", "exsanguinate", "grave_debt",
			"cryogenic_pulse", "glacial_grasp", "glacial_rampart", "iron_retort",
			"pyretic_forge", "void_shroud", "gloam_laceration", "umbral_step", "blood_eclipse"
	};

	private WillCombatRulesTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String source = Files.readString(Path.of(RULES));
		brokenStatsAreFixedByTier(source);
		sentStatsScaleFromTargetSnapshot(source);
		schoolKitsCoverEveryTemper(source);
		luxAndTenebrisKitsCarryParityOffense(source);
		theSaintGiftsAreNotInAnyKit(source);
		counterSchoolCoversEveryTemper(source);
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
		assertContains("sent health scales with player max health", source, "playerMaxHealth * 3.0D");
		assertContains("sent damage scales by degree", source, "Math.max(0, degree - 6) * 2.0D");
	}

	private static void schoolKitsCoverEveryTemper(String source) {
		for (String school : SCHOOLS) {
			assertContains("kit covers " + school, source, "EnumBloodTemper." + school);
		}
		for (String id : REQUIRED_KIT_IDS) {
			assertContains("kit id " + id + " is authored", source, "\"" + id + "\"");
		}
		assertContains("kits return hemomancy ids", source, "map(Hemomancy::rloc)");
	}

	private static void luxAndTenebrisKitsCarryParityOffense(String source) {
		assertContains("lux kit starts with a Humilis attack", source,
				"EnumBloodTemper.LUX, ids(\"hematic_flare\", \"prismatic_reproof\", \"hematic_beacon\")");
		assertContains("tenebris kit keeps stealth but gains an ambush slash", source,
				"EnumBloodTemper.TENEBRIS, ids(\"void_shroud\", \"gloam_laceration\", \"umbral_step\", \"blood_eclipse\")");
	}

	/** D-1, NQ-08-5: the Saint gifts are player only, so no Will casts the Unclosing Eye. */
	private static void theSaintGiftsAreNotInAnyKit(String source) {
		assertTrue("no Will casts the Unclosing Eye", !source.contains("\"unclosing_eye\"") && !source.contains("\"crimson_tithe\""));
	}

	private static void counterSchoolCoversEveryTemper(String source) {
		for (String school : SCHOOLS) {
			int occurrences = count(source, "EnumBloodTemper." + school);
			assertTrue("counter and kit mention " + school, occurrences >= 2);
		}
		assertContains("counter fallback remains stable", source,
				"COUNTERS.getOrDefault(school, EnumBloodTemper.MORTEM)");
	}

	private static void falterAndCadenceConstantsAreStable(String source) {
		assertContains("falter burst fraction", source, "FALTER_BURST_FRACTION = 0.25D");
		assertContains("falter burst window", source, "FALTER_BURST_WINDOW_TICKS = 80");
		assertContains("falter window", source, "FALTER_WINDOW_TICKS = 100");
		assertNotContains("old hp threshold fraction is removed", source, "FALTER_FRACTION");
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

	private static void assertTrue(String label, boolean condition) {
		if (!condition) {
			throw new AssertionError(label);
		}
	}
}

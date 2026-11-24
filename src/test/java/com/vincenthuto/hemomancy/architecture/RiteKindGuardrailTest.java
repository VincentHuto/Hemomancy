package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step 11 (roadmap Phase 10, L-6 and N-8): the shared Cardinal Rite tick, its ending and its completion ask the rite's
 * {@code RiteKind} where kinds differ instead of testing for a kind; a rite is built only by
 * {@code CardinalRiteActivation}; and the blood crafting key packet only delegates.
 */
class RiteKindGuardrailTest {
	private static final List<String> SHARED_COURSE = List.of(
			"common/rite/harbinger/HarbingerCardinalRiteEvents.java",
			"common/rite/harbinger/CardinalRiteTermination.java",
			"common/rite/harbinger/CardinalRiteCompletion.java",
			"common/rite/harbinger/CardinalRiteStructure.java",
			"common/rite/harbinger/CardinalRiteOrdealEngine.java",
			"common/rite/ActiveCardinalRite.java",
			"common/rite/CardinalRiteEscalation.java");
	/** The shared tick and the ending ask the kind whether a rite binds and speaks in blood, not the recipe's path. */
	private static final List<String> BLOOD_RITE_COURSE = List.of(
			"common/rite/harbinger/HarbingerCardinalRiteEvents.java",
			"common/rite/harbinger/CardinalRiteTermination.java");
	private static final List<String> KIND_CHECKS = List.of("StationUpgradeRites.isRite(", "WorkingCraftRites.isRite(",
			"SuccessionRites.is(", ".isPuppeteerTrial()", ".isWorkingCraft()");
	private static final Pattern REFLECTED_COMPLETION = Pattern.compile("getDeclaredMethod\\(\\s*\"completeRite\"");
	private static final Pattern BUILDS_A_RITE = Pattern.compile(
			"new ActiveCardinalRite\\(|ActiveCardinalRite\\.(?:interactive|puppeteerTrial|workingCraft)\\(");

	@Test
	void theSharedCourseAsksTheKind() {
		for (String file : SHARED_COURSE) {
			String source = SourceAssertions.readMainJava(file);
			for (String check : KIND_CHECKS) {
				SourceAssertions.assertNotContains(file + " must ask RiteKind instead of " + check, source, check);
			}
		}
	}

	@Test
	void theSharedTickAsksTheKindWhetherARiteIsInBlood() {
		for (String file : BLOOD_RITE_COURSE) {
			SourceAssertions.assertNotContains(file + " must ask RiteKind.bindsInBlood() instead of the recipe's path",
					SourceAssertions.readMainJava(file), ".isUnstained()");
		}
	}

	/** The rite tick runs rites; floorless crafts and the Bloom's client sync keep their own ticks. */
	@Test
	void theRiteTickRunsOnlyRites() {
		String riteTick = SourceAssertions.methodBody(
				SourceAssertions.readMainJava("common/rite/harbinger/HarbingerCardinalRiteEvents.java"),
				"public static void onLevelTick(");
		String structureTick = SourceAssertions.methodBody(
				SourceAssertions.readMainJava("common/event/BloodStructureTickEvents.java"),
				"public static void onLevelTick(");
		for (String manager : List.of("PendingBloodCraftManager.tick(", "BloodStructureFeedManager.tick(",
				"BloodInfusionManager.tick(")) {
			SourceAssertions.assertNotContains("the rite tick leaves " + manager, riteTick, manager);
			SourceAssertions.assertContains("floorless crafts tick " + manager, structureTick, manager);
		}
		SourceAssertions.assertNotContains("the rite tick leaves the Bloom sync", riteTick, "syncQliphothBlooms(");
		SourceAssertions.assertContains("the Bloom's own tick syncs it", SourceAssertions.methodBody(
				SourceAssertions.readMainJava("common/rite/harbinger/QliphothBloomEvents.java"),
				"public static void onLevelTick("), "syncQliphothBlooms(");
	}

	/** An ordeal wave's behaviour is a row of {@code CardinalRiteWave}, not a string compared in each caller. */
	@Test
	void theOrdealAsksTheWaveTable() {
		for (String file : List.of("common/rite/harbinger/CardinalRiteOrdealEngine.java",
				"common/rite/harbinger/CardinalRiteInteractionHandler.java", "common/rite/CardinalRiteChecklist.java",
				"common/rite/CardinalRiteWaveRules.java")) {
			String source = SourceAssertions.readMainJava(file);
			for (String id : List.of("\"bloodlicker_siphon\"", "\"fargone_dive\"", "\"rogue_will\"", "\"false_omens\"",
					"\"response_sigil\"", "\"covenant_pressure\"", "\"discover_\"")) {
				SourceAssertions.assertNotContains(file + " asks CardinalRiteWave instead of naming " + id, source, id);
			}
		}
	}

	/** A rite's completion effect is found by the id its recipe names, never by the recipe's file path. */
	@Test
	void completionEffectsAreKeyedByTheirOwnIds() {
		String effects = SourceAssertions.readMainJava("common/rite/harbinger/CardinalRiteCompletionEffects.java");
		SourceAssertions.assertNotContains("no recipe path in the effect table", effects, "\"cardinal_rite/");
		SourceAssertions.assertNotContains("no lookup by recipe path", effects, "ctx.path()");
		SourceAssertions.assertNotContains("no pallid vessel path key", effects, "PallidVesselAssignment.RITE_PATH");
	}

	/** Activation is an ordered list of named gates, then build, escrow and start; not one long method. */
	@Test
	void activationChecksItsGatesInOrder() {
		String activation = SourceAssertions.readMainJava("common/rite/harbinger/CardinalRiteActivation.java");
		SourceAssertions.assertContains("the gates are one ordered list", activation,
				"private static final List<Gate> GATES = List.of(");
		String tryStart = SourceAssertions.methodBody(activation, "public static CardinalRiteActivationRules.ActivationAttempt tryStart(");
		long lines = tryStart.lines().count();
		assertEquals(true, lines <= 80, "tryStart finds the station and hands it to the gates; it is " + lines + " lines");
	}

	@Test
	void onlyActivationBuildsRites() throws IOException {
		Map<String, Integer> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String shown = RepoPaths.display(file);
				if (shown.endsWith("/common/rite/ActiveCardinalRite.java")) continue;
				Matcher matcher = BUILDS_A_RITE.matcher(SourceAssertions.read(file));
				int count = 0;
				while (matcher.find()) count++;
				if (count > 0) found.put(shown, count);
			}
		}
		assertEquals(Map.of("src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteActivation.java", 4),
				found, "start rites through CardinalRiteActivation.tryStart");
	}

	@Test
	void theBloodCraftingKeyOnlyDelegates() {
		String handle = SourceAssertions.methodBody(SourceAssertions.readMainJava(
				"common/network/capa/harbinger/BloodCraftingKeyPressPacket.java"), "public static void handle(");
		SourceAssertions.assertContains("structures are crafted by BloodStructureCrafting", handle,
				"BloodStructureCrafting.tryCraft(");
		SourceAssertions.assertContains("rites are started by CardinalRiteActivation", handle, "CardinalRiteActivation.tryStart(");
		for (String gameplay : List.of("CardinalRiteSavedData", "BloodAccounting", "RecipeDegreeGates",
				"PendingBloodCraftManager")) {
			SourceAssertions.assertNotContains("the key packet keeps no gameplay: " + gameplay, handle, gameplay);
		}
	}

	@Test
	void riteCompletionIsCalledNotReflected() throws IOException {
		try (Stream<Path> files = Files.walk(RepoPaths.GAMETEST_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				assertEquals(false, REFLECTED_COMPLETION.matcher(SourceAssertions.read(file)).find(),
						RepoPaths.display(file) + " must call CardinalRiteCompletion.complete");
			}
		}
	}
}

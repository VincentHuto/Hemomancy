package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core-loop Track 1: what a selected manipulation makes of Projection and Absorption (its Sculpting) is attached at
 * registration with {@code setProjectionSculpting(Sculptings::x)} / {@code setAbsorptionSculpting(Sculptings::x)} (and a narrow
 * one tried ahead of it with {@code addProjectionSculpting} / {@code addAbsorptionSculpting}, round two P4), like its
 * Drudge action and entity cast. {@code SculptingDispatcher} alone reads a Sculpting and never names a manipulation, and
 * each verb chain asks it at one pinned place: after rites, Saints and Wills, before the plain verb's endpoints.
 */
class SculptingGuardrailTest {
	private static final Pattern ATTACHED = Pattern.compile("\\.(set|add)(Projection|Absorption)Sculpting\\(([^)]*)\\)");
	private static final Pattern NAMED = Pattern.compile("Sculptings::(\\w+)");

	@Test
	void theDispatcherNamesNoManipulation() {
		String dispatcher = SourceAssertions.readMainJava("common/manipulation/verb/SculptingDispatcher.java");
		SourceAssertions.assertNotContains("no switch on a manipulation", dispatcher, "switch");
		SourceAssertions.assertNotContains("no manipulation registry handle", dispatcher, "ManipulationInit.blood_");
		Matcher literal = Pattern.compile("\"[a-z_]+\"").matcher(dispatcher);
		assertTrue(!literal.find(), "SculptingDispatcher names no manipulation; attach a Sculpting in ManipulationInit instead");
	}

	/**
	 * The 2026-11-12 ruling (UN-5): strand forms are optional kits, and a held manipulation is its own
	 * {@code VerbSculpting} class with its own mechanic, never a recolour of another. The lash kit is data on its
	 * registration ({@code Strands.lash(...)} from a {@code Sculptings} factory), so nothing in the strand package names a
	 * manipulation; a lash's school is its manipulation's.
	 */
	@Test
	void theStrandPackageNamesNoManipulation() throws IOException {
		TreeSet<String> naming = new TreeSet<>();
		try (var files = Files.walk(RepoPaths.MAIN_JAVA.resolve("common/manipulation/verb/strand"))) {
			for (var file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String text = SourceAssertions.read(file);
				boolean handle = Pattern.compile("ManipulationInit\\.[a-z][a-z_]*\\b(?!\\()").matcher(text).find();
				// A blood source's id is the one underscored literal a strand may carry.
				boolean literal = Pattern.compile("\"[a-z]+_[a-z_]+\"")
						.matcher(text.replaceAll("Source\\.of\\(\"[a-z_]+\"", "")).find();
				if (handle || literal) naming.add(RepoPaths.display(file));
			}
		}
		assertEquals(new TreeSet<>(), naming, "a strand names no manipulation; build it from a StrandSpec in Sculptings");
	}

	@Test
	void onlyTheDispatcherReadsASculpting() throws IOException {
		TreeSet<String> readers = new TreeSet<>();
		try (var files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (var file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String text = SourceAssertions.read(file);
				if (text.contains(".getSculpting(") || text.contains(".getSculptings(")) readers.add(RepoPaths.display(file));
			}
		}
		assertEquals(Set.of("src/main/java/com/vincenthuto/hemomancy/common/manipulation/verb/SculptingDispatcher.java"),
				readers, "read a Sculpting through SculptingDispatcher.selected or currentClaim");
	}

	@Test
	void sculptingsAreAttachedOnlyInManipulationInitAndNamedForTheirManipulation() throws IOException {
		TreeSet<String> attachers = new TreeSet<>();
		try (var files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (var file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String display = RepoPaths.display(file);
				if (display.endsWith("common/manipulation/BloodManipulation.java")) continue;
				if (ATTACHED.matcher(SourceAssertions.read(file)).find()) attachers.add(display);
			}
		}
		assertTrue(attachers.isEmpty() || attachers.equals(Set.of(
						"src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java")),
				"attach Sculptings in ManipulationInit only: " + attachers);

		String init = SourceAssertions.readMainJava("common/init/ManipulationInit.java");
		TreeMap<String, Integer> attached = new TreeMap<>();
		TreeSet<String> notAFactory = new TreeSet<>();
		Matcher hook = ATTACHED.matcher(init);
		while (hook.find()) {
			Matcher named = NAMED.matcher(hook.group(3).trim());
			if (named.matches()) attached.merge(named.group(1), 1, Integer::sum);
			else notAFactory.add(hook.group(3));
		}
		assertEquals(new TreeSet<>(), notAFactory, "attach a Sculpting as a Sculptings::factory method reference");

		TreeMap<String, Integer> expected = new TreeMap<>();
		var sculptings = RepoPaths.MAIN_JAVA.resolve("common/manipulation/verb/Sculptings.java");
		if (Files.exists(sculptings)) {
			Matcher factory = Pattern.compile("public static VerbSculpting (\\w+)\\(\\)").matcher(SourceAssertions.read(sculptings));
			while (factory.find()) expected.put(factory.group(1), 1);
		}
		assertEquals(expected, attached, "attach each Sculptings factory to exactly one manipulation in ManipulationInit");
	}

	@Test
	void eachVerbChainAsksTheDispatcherAtItsPinnedPlace() {
		String projection = SourceAssertions.readMainJava("common/item/harbinger/tool/living/BloodProjectionItem.java");
		assertOrdered("Projection", projection, List.of(
				"EarlyInitiation.tryProject(",
				"SaintChannelRouter.tryProject(",
				"WillBloodUtilityInteractions.tryProjectBanishFalteringWill(",
				"CardinalRiteInteractionHandler.tryProject(",
				"SculptingDispatcher.trySculpt(sculptor, Verb.PROJECT)",
				"BlockBloodInteractions.tryProjectIntoLookedAtBlock(",
				"GroundNetwork.tryProject(",
				"PhlegethosomaSummoning.tryProject(",
				"ChumPools.tryProject(",
				"BloodStructureFeedManager.feedStructure(",
				"SanguineCondenserProjection.tryProject(",
				"BloodInfusionManager.feedBlock(",
				"sink.takeProjectedBlood(",
				"GroundNetwork.stain("));
		String absorption = SourceAssertions.readMainJava("common/item/harbinger/tool/living/BloodAbsorptionItem.java");
		assertOrdered("Absorption", absorption, List.of(
				"VesperBloodAbsorptionInteractions.tryAbsorb(",
				"SaintChannelRouter.tryAbsorb(",
				"CardinalRiteCancellationHandler.tryChannel(",
				"BlockBloodInteractions.tryAbsorbFromLookedAtBlock(",
				"WillBloodUtilityInteractions.tryAbsorbFalteringWill(",
				"SculptingDispatcher.trySculpt(sculptor, Verb.ABSORB)",
				"GroundNetwork.tryAbsorb(",
				"BloodAbsorptionChannelRules.canDrainLivingTarget(",
				"findBareAbsorptionTarget(player, reach)"));
		String staff = SourceAssertions.readMainJava("common/item/harbinger/tool/living/LivingStaffItem.java");
		assertOrdered("Staff Absorption", staff, List.of(
				"EngramTracing.isTracing(tracer)",
				"VesperBloodAbsorptionInteractions.tryAbsorb(",
				"SaintChannelRouter.tryAbsorb(",
				"CardinalRiteCancellationHandler.tryChannel(",
				"tryAbsorbFromLookedAtBlockWithStaff(pLevel, player, focus, reach)",
				"WillBloodUtilityInteractions.tryAbsorbFalteringWill(",
				"SculptingDispatcher.trySculpt(serverPlayer, Verb.ABSORB)",
				"GroundNetwork.tryAbsorb(",
				"absorbWithStaff(pLevel, player, pStack)"));
	}

	/**
	 * Core-loop Track 2: Sculptings are for players. Entity casts (Wills, Vesper, Drudges) never go through the
	 * dispatcher or a Sculpting, so {@code entity-cast-fingerprints.txt} is untouched by any conversion.
	 */
	@Test
	void noEntityIsRoutedThroughASculpting() {
		for (String path : List.of("common/manipulation/EntityManipulationEffects.java", "common/manipulation/EntityCasts.java",
				"common/manipulation/DrudgeActions.java", "common/manipulation/WillManipulationCaster.java",
				"common/manipulation/EntityCastableManipulation.java")) {
			String text = SourceAssertions.readMainJava(path);
			SourceAssertions.assertNotContains(path + " routes an entity through the Sculpting dispatcher", text,
					"SculptingDispatcher");
			SourceAssertions.assertNotContains(path + " reaches a Sculpting", text, "Sculptings.");
			SourceAssertions.assertNotContains(path + " reaches a Sculpting", text, "VerbSculpting");
		}
	}

	@Test
	void theStaffReadsTheVerbNotTheSelection() {
		String staff = SourceAssertions.readMainJava("common/item/harbinger/tool/living/LivingStaffItem.java");
		SourceAssertions.assertNotContains("the Staff never decides its verb from the selection", staff,
				"getSelectedMemoryRef");
		SourceAssertions.assertContains("the Staff channels the active verb", staff, "VerbChannel.activeVerb(player)");
	}

	private static void assertOrdered(String label, String text, List<String> steps) {
		int last = -1;
		for (String step : steps) {
			int at = text.indexOf(step);
			assertTrue(at >= 0, label + " chain lost the step " + step);
			assertTrue(at > last, label + " chain runs " + step + " out of order");
			last = at;
		}
	}
}

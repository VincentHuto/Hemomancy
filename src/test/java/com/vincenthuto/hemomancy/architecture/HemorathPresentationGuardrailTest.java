package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Hemorath on the Saint combat kit keeps his presentation rules: his geometry hooks sit before the shared Animus/Mortem
 * branch, no Hemorath file emits a vanilla particle or names the deleted goals, the old player-wide blood debt is never
 * read, every drain names one of his sources, and the vessel layer builds its render type once.
 */
class HemorathPresentationGuardrailTest {
	@Test
	void dedicatedGeometryUsesTheSharedFerricAndBloodPassesBeforeGenericGeometry() {
		String renderer = SourceAssertions.readMainJava("client/render/world/ManipulationVisualRenderer.java");
		assertTrue(renderer.contains("HemorathReliquaryGeometry.collect(world, poses, FLOWS"));
		assertTrue(renderer.indexOf("if(HemorathReliquaryGeometry.handles") < renderer.indexOf("if(AnimusMortemGeometry.handles(packet.form())) {"));
		assertTrue(SourceAssertions.readMainJava("client/render/world/ManipulationMotes.java")
				.contains("if(HemorathReliquaryGeometry.handles(form))return;"));
	}

	@Test
	void noHemorathFileEmitsVanillaParticlesOrNamesTheOldGoals() throws Exception {
		for (Path file : hemorathFiles()) {
			String text = SourceAssertions.read(file);
			assertFalse(text.contains("ParticleTypes"), RepoPaths.display(file));
			assertFalse(text.contains("HematicCollapseGoal") || text.contains("EmptyPulseGoal"), RepoPaths.display(file));
			assertFalse(text.contains("hitGlow("), RepoPaths.display(file) + ": motes are fine glow, never hitGlow");
		}
	}

	@Test
	void theTitheIsHisOwnLedgerNotThePlayerWideDebt() throws Exception {
		for (Path file : hemorathFiles()) {
			String text = SourceAssertions.read(file);
			for (String old : List.of("getBloodDebt(", "consumeDebt(", "resetBloodDebt(", ".addDamage(")) {
				assertFalse(text.contains(old), RepoPaths.display(file) + " still touches the old blood debt: " + old);
			}
		}
	}

	@Test
	void everyDrainNamesOneOfHisSources() throws Exception {
		Pattern pay = Pattern.compile("(?:BloodAccounting|SaintChannelRouter)\\.(?:pay|payUpkeep)\\(([^;]*);");
		List<String> unnamed = new ArrayList<>();
		for (Path file : hemorathFiles()) {
			Matcher matcher = pay.matcher(SourceAssertions.read(file));
			while (matcher.find()) {
				if (!matcher.group(1).contains("HemorathSources.") && !matcher.group(1).contains("source)")) {
					unnamed.add(RepoPaths.display(file) + ": " + matcher.group());
				}
			}
		}
		assertEquals(List.of(), unnamed);
		String sources = SourceAssertions.readMainJava("common/entity/boss/saint/hemorath/HemorathSources.java");
		for (String id : List.of("hemorath_presence", "hemorath_claw", "hemorath_siphon", "hemorath_tide", "hemorath_thirst",
				"hemorath_gorge", "hemorath_vessel_offering", "hemorath_tithe_payment", "hemorath_well_offering")) {
			SourceAssertions.assertContains("the source " + id, sources, "\"" + id + "\"");
		}
	}

	@Test
	void theVesselLayerBuildsItsRenderTypeOnce() {
		String layer = SourceAssertions.readMainJava("client/render/layer/mob/HemorathVesselLayer.java");
		SourceAssertions.assertContains("one static glow type", layer,
				"private static final RenderType GLOW = RenderType.eyes(GLOW_TEXTURE);");
		assertEquals(1, layer.split("RenderType\\.eyes\\(", -1).length - 1);
		assertFalse(Files.exists(RepoPaths.mainJava("client/render/layer/mob/HemorathOverloadLayer.java")),
				"the overload layer was replaced by the vessel layer");
	}

	private static List<Path> hemorathFiles() throws Exception {
		List<Path> files = new ArrayList<>();
		try (Stream<Path> walk = Files.walk(RepoPaths.mainJava("common/entity/boss/saint/hemorath"))) {
			walk.filter(path -> path.toString().endsWith(".java")).forEach(files::add);
		}
		try (Stream<Path> walk = Files.walk(RepoPaths.mainJava("client"))) {
			walk.filter(path -> path.getFileName().toString().startsWith("Hemorath") && path.toString().endsWith(".java"))
					.filter(path -> !path.getFileName().toString().startsWith("HemorathRib"))
					.forEach(files::add);
		}
		return files;
	}
}

package com.vincenthuto.hemomancy.common.entity.boss;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BossTelegraphsSourceTest {
	private static final Path SOURCE = Path.of("src/main/java/com/vincenthuto/hemomancy");

	@Test
	void sharedTelegraphsUseTheCustomSuiteAndVesperDelegatesToThem() throws Exception {
		String shared = read("common/entity/boss/BossTelegraphs.java");
		String vesper = read("common/entity/boss/endgame/VesperVisualEffects.java");
		assertFalse(shared.contains("ParticleTypes."));
		assertTrue(shared.contains("HemoParticleData"));
		assertTrue(shared.contains("public static void line("));
		assertTrue(shared.contains("public static void ring("));
		assertTrue(shared.contains("public static void cone("));
		assertTrue(shared.contains("public static void tendril("));
		assertTrue(vesper.contains("BossTelegraphs.line("));
		assertTrue(vesper.contains("BossTelegraphs.ring("));
		assertTrue(vesper.contains("BossTelegraphs.cone("));
	}

	@Test
	void broodFunglingsAreBoundToTheirMotherAndHerdInsteadOfWandering() throws Exception {
		String fungling = read("common/entity/mob/animal/FunglingEntity.java");
		assertTrue(fungling.contains("\"BroodMother\""));
		assertTrue(fungling.contains("public boolean isBrood()"));
		assertTrue(fungling.contains("HerdTowardMotherGoal"));
		assertTrue(fungling.contains("protected boolean shouldDropLoot()"));
	}

	private static String read(String relative) throws Exception {
		return Files.readString(SOURCE.resolve(relative));
	}
}

package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteStructureFailureSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	@Test
	void periodicStructureFailureStopsTheRiteAndDispersesEveryAnchor() throws IOException {
		String events = Files.readString(ROOT.resolve(
				"src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java"));

		assertTrue(events.contains(
				"if (sLevel.getGameTime() % 20 == 0 && !verifyRiteStructure(sLevel, rite)) {\n"
						+ "\t\t\t\t\tfailRite(sLevel, caster, rite);\n"
						+ "\t\t\t\t\ttoRemove.add(playerUUID);\n"
						+ "\t\t\t\t\tcontinue;\n"
						+ "\t\t\t\t}"),
				"a broken ceremony structure must immediately enter the rite removal path");
		assertTrue(events.contains("spawnBrokenAnchorDispersal(sLevel, rite, recipe);"),
				"structure backlash must disperse the rite's anchor points");
		assertTrue(events.contains("for (var anchor : recipe.getCeremony().anchors())"),
				"the dispersal must visit every authored anchor");
		assertTrue(events.contains("LightningTesterSpawner.spawn"),
				"anchor dispersal must use the shared lightning renderer");
		assertTrue(events.contains("BROKEN_ANCHOR_OUTER_BLACK"),
				"anchor dispersal lightning must have a black outer arc");
		assertTrue(events.contains("BROKEN_ANCHOR_INNER_PURPLE"),
				"anchor dispersal lightning must have a purple core");
		assertTrue(events.contains("DarkGlowParticleFactory.createData(ParticleColor.BLACK)"),
				"each vanished anchor must poof into black particles");
		assertTrue(events.contains("new EmberParticleData(ParticleColor.PURPLE"),
				"each vanished anchor must burst into purple particles");
	}
}

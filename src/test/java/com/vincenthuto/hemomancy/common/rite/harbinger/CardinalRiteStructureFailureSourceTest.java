package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteStructureFailureSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	@Test
	void structureBacklashDispersesEveryAnchor() throws IOException {
		String events = Files.readString(ROOT.resolve(
				"src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java"))
				.replace("\r\n", "\n");

		// That a broken structure ends the rite through this backlash is behaviour, proven by the rite_kind_validation
		// GameTests (ceremonyWhoseStructureBreaksLetsGo, craftWhoseSubjectBreaksFailsAndReturnsItsEscrow).
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
		assertTrue(events.contains("new DarkColorParticleData(HLParticleInit.dark_glow.get(), ParticleColor.BLACK)"),
				"each vanished anchor must poof into black particles");
		assertTrue(events.contains("new EmberParticleData(ParticleColor.PURPLE"),
				"each vanished anchor must burst into purple particles");
	}

	// Retiring a rite whose recipe is gone is behaviour, proven by the rite_kind_validation GameTest
	// ceremonyWhoseRecipeIsRetiredDissolves.
}

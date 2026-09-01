package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CardinalRiteThreatProtectionSourceTest {
	private static final Path JAVA = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common");

	@Test
	void passiveRitualDamageSystemsRespectTheRiteThreatMarker() throws IOException {
		assertProtected("rite/harbinger/HarbingerCardinalRiteEvents.java");
		assertProtected("event/worldevent/FoundingFaneEvents.java");
		assertProtected("rite/harbinger/SanguineDominionEvents.java");
	}

	private static void assertProtected(String relativePath) throws IOException {
		String source = Files.readString(JAVA.resolve(relativePath));
		assertTrue(source.contains("CardinalRiteThreatRules.isProtectedFromPassiveRiteDamage")
						|| source.contains("CardinalRiteThreatRules.isEligiblePassiveSacrifice"),
				relativePath + " must exempt threats spawned for an active rite");
	}
}

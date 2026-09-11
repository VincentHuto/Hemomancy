package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HarbingerParticleBoundaryTest {
	@Test
	void dedicatedRiteAndOfferingPathsDoNotLoadParticleProviders() throws Exception {
		Path root = Path.of("src/main/java/com/vincenthuto/hemomancy/common");
		for (String file : List.of("block/harbinger/rite/BrazierBlock.java",
				"block/harbinger/rite/BrazierSpecialOfferingEffects.java",
				"rite/harbinger/HarbingerCardinalRiteEvents.java",
				"network/capa/harbinger/BloodFormationKeyPressPacket.java")) {
			assertFalse(Files.readString(root.resolve(file)).contains(".particle.factory."), file);
		}
	}
}

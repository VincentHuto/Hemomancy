package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CorticalDriftSurfaceRuleSourceTest {
	private static String source() throws IOException {
		return Files.readString(Path.of("").toAbsolutePath().resolve(
				"src/main/java/com/vincenthuto/hemomancy/common/worldgen/terrablender/CorticalDriftSurfaceRuleData.java"));
	}

	@Test
	void rulesAreScopedToTheBiome() throws IOException {
		assertTrue(source().contains("SurfaceRules.isBiome(BiomeInit.CORTICAL_DRIFT)"),
				"rules must not leak into vanilla End biomes");
	}

	@Test
	void strataRunCrustToCore() throws IOException {
		String source = source();
		assertTrue(source.contains("SurfaceRules.ON_FLOOR") && source.contains("myelin_sheath"),
				"white myelin crust on the floor");
		assertTrue(source.contains("SurfaceRules.UNDER_FLOOR") && source.contains("axonal_slate"),
				"deep blue slate directly beneath");
		assertTrue(source.contains("SurfaceRules.DEEP_UNDER_FLOOR") && source.contains("neural_tissue"),
				"black neural tissue at depth");
		assertTrue(source.contains("Noises.SURFACE") && source.contains("ganglion_matter"),
				"yellow ganglion seams blended by surface noise");
	}
}

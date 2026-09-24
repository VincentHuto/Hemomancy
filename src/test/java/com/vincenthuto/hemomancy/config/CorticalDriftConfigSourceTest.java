package com.vincenthuto.hemomancy.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CorticalDriftConfigSourceTest {
	@Test
	void endRegionIsConfigurableAndRestartGated() throws IOException {
		String config = Files.readString(Path.of("").toAbsolutePath()
				.resolve("src/main/java/com/vincenthuto/hemomancy/config/HemoCommonConfig.java"));
		assertTrue(config.contains("ENABLE_CORTICAL_DRIFT_END_REGION"),
				"toggle for the Cortical Drift End region must exist");
		assertTrue(config.contains("enableCorticalDriftEndRegion"),
				"toggle must use the camelCase config key");
		assertTrue(config.contains("corticalDriftEndRegionWeight"),
				"TerraBlender weight must be configurable");
		int index = config.indexOf("enableCorticalDriftEndRegion");
		assertTrue(config.lastIndexOf("worldRestart()", index) > config.indexOf("push(\"worldgen\")"),
				"biome registration cannot change on a live world, so it must be restart-gated");
	}
}

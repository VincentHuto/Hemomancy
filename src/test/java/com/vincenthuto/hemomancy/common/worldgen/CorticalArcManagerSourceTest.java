package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CorticalArcManagerSourceTest {
	private static String source() throws IOException {
		return source("CorticalArcManager.java");
	}

	private static String source(String file) throws IOException {
		return Files.readString(Path.of("").toAbsolutePath()
				.resolve("src/main/java/com/vincenthuto/hemomancy/common/worldgen/" + file));
	}

	@Test
	void reusesTheAuthoredLightningToolkit() throws IOException {
		String source = source();
		assertTrue(source.contains("LightningTesterSpawner.spawn"),
				"AGENTS.md requires the authored lightning suite, not vanilla particles");
		assertFalse(source.contains("NerveLinkPacket") || source.contains("sendFilament"),
				"Cortical pulses must not add the straight yellow Ductilis ribbon over the purple bolt");
		assertFalse(source.contains("ParticleTypes.ELECTRIC_SPARK"),
				"vanilla particle clouds are a fallback, not the default");
	}

	@Test
	void arcsAreBudgeted() throws IOException {
		String source = source();
		assertTrue(source.contains("CorticalArcActivity.shouldScan")
				&& source("CorticalArcActivity.java").contains("ARC_INTERVAL"),
				"arcs must fire on an interval, not every tick");
		assertTrue(source.contains("MAX_ARCS_PER_TICK") || source.contains("MAX_ARCS"),
				"a per-tick cap keeps a dense network from flooding the client");
		assertTrue(source.contains("isLoaded") || source.contains("hasChunk"),
				"never scan unloaded chunks");
	}

	@Test
	void onlyRunsInTheDrift() throws IOException {
		String source = source();
		assertTrue(source.contains("Level.END"), "the manager must not tick outside the End");
		assertFalse(source.contains("getBiome(player.blockPosition())"),
				"the Mind and its bridges cross biome borders, so the player's exact biome cannot suppress arcs");
	}

	@Test
	void arcingDedupesPairs() throws IOException {
		String source = source();
		assertTrue(source.contains("consumed"),
				"mutual-nearest nodes must be tracked as consumed so a pair is arced at most once per pass; "
						+ "otherwise index i arcs to j and later index j arcs back to i, drawing the same bolt "
						+ "twice and wasting a slot of the per-pass arc budget");
	}
}

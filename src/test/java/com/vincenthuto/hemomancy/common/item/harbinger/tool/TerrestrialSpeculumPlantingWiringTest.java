package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class TerrestrialSpeculumPlantingWiringTest {
	@Test
	void cancelledPlantingsStopClientsAndRecheckEligibility() throws IOException {
		String manager = source("TerrestrialSpeculumPlantingManager.java");

		assertTrue(manager.contains("TerrestrialSpeculumItem.canManifestAt"));
		assertTrue(manager.contains("PacketCardinalRiteStaffPlanting.stop"));
		assertTrue(manager.contains("TerrestrialSpeculumPlantingSequence.isActive"));
	}

	@Test
	void failedTravelOpeningRemovesTheManifestedTemporaryVein() throws IOException {
		String item = source("TerrestrialSpeculumItem.java");

		assertTrue(item.contains("if (!EarthenVeinTravelManager.begin(player, origin))"));
		assertTrue(item.contains("level.removeBlock(origin, false)"));
	}

	private static String source(String filename) throws IOException {
		return Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool", filename));
	}
}

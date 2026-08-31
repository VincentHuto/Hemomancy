package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TeleportToVeinPacketSourceTest {
	@Test
	void validDestinationPaysThroughSharedEconomyBeforeTeleporting() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/TeleportToVeinPacket.java"));
		int destinationValidation = source.indexOf("selected.getName().equals(te.getLoc().getName())");
		int economy = source.indexOf("venousTravel.tryPerformAction");
		int teleport = source.indexOf("serverPlayer.teleportTo");

		assertTrue(destinationValidation >= 0, "missing destination validation");
		assertTrue(economy > destinationValidation, "travel charged before destination validation");
		assertTrue(teleport > economy, "travel teleported before the shared economy accepted the cast");
	}
}

package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TeleportToVeinPacketSourceTest {
	@Test
	void validatesTemporaryOriginAndDestinationBeforeChargingAndTeleporting() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/TeleportToVeinPacket.java"));
		int originValidation = source.indexOf("originVein.isTemporaryOwnedBy");
		int destinationValidation = source.indexOf("targetVein.getLoc().getUUID().equals");
		int economy = source.indexOf("blood.drain(TerrestrialSpeculumRules.BLOOD_COST)");
		int teleport = source.indexOf("player.teleportTo");

		assertTrue(originValidation >= 0, "missing temporary origin ownership validation");
		assertTrue(destinationValidation >= 0, "missing destination validation");
		assertTrue(economy > destinationValidation, "travel charged before destination validation");
		assertTrue(teleport > economy, "travel teleported before blood payment");
	}
}

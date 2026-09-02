package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TeleportToVeinPacketSourceTest {
	@Test
	void claimedVeinSyncPreservesIdentityUsedByTravelValidation() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/capability/block/vein/VeinLocation.java"));

		assertTrue(source.contains("buf.writeUUID(getUUID())"), "vein sync drops the claimed UUID");
		assertTrue(source.contains("new VeinLocation(buf.readUUID()"), "vein sync regenerates the claimed UUID");
	}

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

	@Test
	void reloadDoesNotReplacePersistedVeinIdentity() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/EarthenVeinBlockEntity.java"));
		int firstTick = source.indexOf("if (!te.hasTicked)");
		int firstTickEnd = source.indexOf("te.setHasTicked(true)", firstTick);
		String initialization = source.substring(firstTick, firstTickEnd);

		assertTrue(initialization.contains("if (te.getLoc() == VeinLocation.BLANK)"),
				"vein reload path must only create a location when no persisted location exists");
	}

	@Test
	void veinTravelUsesOneShotSoundAfterSuccessfulTeleport() throws IOException {
		String radialSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseVeinScreen.java"));
		String packetSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/TeleportToVeinPacket.java"));
		int teleport = packetSource.indexOf("player.teleportTo");
		int sound = packetSource.indexOf("SoundEvents.ENDERMAN_TELEPORT");

		assertTrue(!radialSource.contains("SoundEvents.PORTAL_TRAVEL"),
				"vein selection must not start the long looping portal travel sound");
		assertTrue(sound > teleport, "teleport sound must play only after a validated teleport");
	}
}

package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class ChamberProgressionIntegrationSourceTest {
	@Test
	void sameTickPersistencePacketAndRecoveryContracts() throws IOException {
		String manager = read("src/main/java/com/vincenthuto/hemomancy/common/worldgen/ChamberOfWillManager.java");
		String events = read("src/main/java/com/vincenthuto/hemomancy/common/event/worldevent/ChamberOfWillEvents.java");
		String packet = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncChamberOfWill.java");
		assertTrue(manager.contains("ProgressionRefresh refreshProgressionState"));
		assertTrue(manager.contains("refresh.radiusIncreased()"));
		assertTrue(events.contains("refreshProgressionNow(serverPlayer)"));
		assertTrue(manager.contains("tag.put(\"builtRadii\""));
		assertTrue(manager.contains("tag.getList(\"builtRadii\""));
		assertTrue(manager.contains("tag.put(\"skyThemeOverrides\""));
		assertTrue(manager.contains("tag.getList(\"skyThemeOverrides\""));
		assertTrue(manager.contains("tag.put(\"unrestrictedSkyThemeOverrides\""));
		assertTrue(packet.contains("buf.writeInt(msg.radius)"));
		assertTrue(packet.contains("buf.readResourceLocation(), buf.readInt(), buf.readInt(), buf.readInt()"));
		assertTrue(packet.contains("ChamberOfWillClientData.set(msg.skyTheme, msg.tier, msg.radius"));
		assertTrue(manager.contains("ChamberExpansionRules.floorBand"));
		assertTrue(manager.contains("state.is(BlockInit.sporite_crystal.get())"));
	}

	private static String read(String path) throws IOException {
		return Files.readString(Path.of(path)).replace("\r\n", "\n");
	}
}

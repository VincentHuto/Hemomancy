package com.vincenthuto.hemomancy.common.encounter;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

final class ChamberProgressionIntegrationSourceTest {
	@Test
	void sameTickPersistencePacketAndRecoveryContracts() throws IOException {
		String manager = read("src/main/java/com/vincenthuto/hemomancy/common/encounter/ChamberOfWillManager.java");
		String events = read("src/main/java/com/vincenthuto/hemomancy/common/event/worldevent/ChamberOfWillEvents.java");
		String packet = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncChamberOfWill.java");
		String clientHandler = read("src/main/java/com/vincenthuto/hemomancy/client/network/ClientboundStateHandlers.java");
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
		assertTrue(clientHandler.contains("ChamberOfWillClientData.set(msg.skyTheme(), msg.tier(), msg.radius()"));
		assertTrue(manager.contains("ChamberExpansionRules.floorBand"));
		assertTrue(manager.contains("state.is(BlockInit.sporite_crystal.get())"));
	}
}

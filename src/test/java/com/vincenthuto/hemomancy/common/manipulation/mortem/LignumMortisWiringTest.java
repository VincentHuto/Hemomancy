package com.vincenthuto.hemomancy.common.manipulation.mortem;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class LignumMortisWiringTest {
	private static final Path JAVA = Path.of("src/main/java/com/vincenthuto/hemomancy");

	@Test
	void onlyExplicitReleaseCommitsTheMarkedBlocks() throws Exception {
		String manager = read("common/manipulation/ManipulationChannelManager.java");
		String input = read("common/network/capa/harbinger/manips/UseManipKeyPacket.java");
		String manipulation = read("common/manipulation/mortem/LignumMortisManip.java");

		assertTrue(input.contains("ManipulationChannelManager.stop((net.minecraft.server.level.ServerPlayer) player, true)"));
		assertTrue(manager.contains("stop(player, false)"));
		assertTrue(manipulation.contains("if (released && sessionLevel != null"));
		assertTrue(manipulation.contains("ItemStack.EMPTY"));
		assertTrue(manipulation.contains("VeinMinerHelper.hasBreakPermission"));
	}

	@Test
	void bloodFeedKeepsLegacyCallersAndAddsStableChannelFeeds() throws Exception {
		String packet = read("common/network/capa/harbinger/PacketBloodStructureFeed.java");
		String clientData = read("client/data/ActiveBloodStructureFeedClientData.java");

		assertTrue(packet.contains("this(positions, progress, visibleTicks, clear, 0L)"));
		assertTrue(packet.contains("buf.writeLong(msg.channelId)"));
		assertTrue(clientData.contains("channelId == 0L ? key(positions) : channelKey(channelId)"));
	}

	@Test
	void castingUsesOrganicTendrilsInsteadOfLightning() throws Exception {
		String manipulation = read("common/manipulation/mortem/LignumMortisManip.java");

		assertTrue(manipulation.contains("TendrilEffectSpawner.spawn"));
		assertTrue(manipulation.contains("new TendrilAnchor.Point(start)"));
		assertTrue(!manipulation.contains("LightningTesterSpawner"));
	}

	private static String read(String relative) throws Exception {
		return Files.readString(JAVA.resolve(relative));
	}
}

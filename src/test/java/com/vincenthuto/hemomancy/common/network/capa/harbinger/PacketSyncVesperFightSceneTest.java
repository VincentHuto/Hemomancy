package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class PacketSyncVesperFightSceneTest {
	@Test
	void activeFloorAnchorSurvivesNetworkRoundTrip() {
		BlockPos center = new BlockPos(4096, 64, 512);
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

		PacketSyncVesperFightScene.encode(buffer, PacketSyncVesperFightScene.activate(center));
		PacketSyncVesperFightScene decoded = PacketSyncVesperFightScene.decode(buffer);

		assertTrue(decoded.active());
		assertEquals(center, decoded.center());
	}

	@Test
	void inactivePayloadCarriesAStableZeroAnchor() {
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

		PacketSyncVesperFightScene.encode(buffer, PacketSyncVesperFightScene.clearScene());
		PacketSyncVesperFightScene decoded = PacketSyncVesperFightScene.decode(buffer);

		assertFalse(decoded.active());
		assertEquals(BlockPos.ZERO, decoded.center());
	}
}

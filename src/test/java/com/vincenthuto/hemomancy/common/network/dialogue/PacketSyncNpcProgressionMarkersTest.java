package com.vincenthuto.hemomancy.common.network.dialogue;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueAttention;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PacketSyncNpcProgressionMarkersTest {
	@Test
	void markerSnapshotRoundTripsAcrossTheWire() {
		PacketSyncNpcProgressionMarkers original = new PacketSyncNpcProgressionMarkers(
				Map.of(17, DialogueAttention.NOTICE, 23, DialogueAttention.URGENT));
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

		PacketSyncNpcProgressionMarkers.STREAM_CODEC.encode(buffer, original);
		PacketSyncNpcProgressionMarkers restored = PacketSyncNpcProgressionMarkers.STREAM_CODEC.decode(buffer);

		assertEquals(original.markers(), restored.markers());
	}
}

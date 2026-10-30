package com.vincenthuto.hemomancy.common.network.heartnode;

import com.vincenthuto.hemomancy.common.event.HeartNodeEnvelopeSync;
import com.vincenthuto.hemomancy.common.world.HeartNodeEntry;
import com.vincenthuto.hemomancy.common.world.HeartNodeRelation;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import com.vincenthuto.hemomancy.common.world.HeartNodeSettings;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeartNodeEnvelopePacketTest {
	private final UUID alice = UUID.randomUUID();
	private final UUID bob = UUID.randomUUID();

	@Test
	void envelopesSurviveTheWire() {
		HeartNodeEnvelopePacket packet = new HeartNodeEnvelopePacket(List.of(
				new HeartNodeEnvelopePacket.Envelope(alice, new BlockPos(-40, 12, 900),
						List.of(new BlockPos(-20, 12, 900), new BlockPos(-4, 6, 905)),
						HeartNodeSettings.DEFAULT.bits(), 4, HeartNodeRelation.OWNER, 123_456_789L, 2),
				new HeartNodeEnvelopePacket.Envelope(bob, BlockPos.ZERO, List.of(), 0, 0,
						HeartNodeRelation.STRANGER, 0L, 3)));
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
		HeartNodeEnvelopePacket.STREAM_CODEC.encode(buf, packet);
		assertEquals(packet, HeartNodeEnvelopePacket.STREAM_CODEC.decode(buf));
		assertEquals(0, buf.readableBytes());
	}

	@Test
	void eachViewerHearsOfNearbySanctumsWithTheirOwnRelation() {
		HeartNodeEntry alices = HeartNodeEntry.planted(alice, new BlockPos(0, 64, 0), 100L)
				.withSynapse(new BlockPos(20, 64, 0));
		HeartNodeEntry far = HeartNodeEntry.planted(bob, new BlockPos(500, 64, 0), 0L);
		long now = 100L + HeartNodeRules.MATURE_TICKS / 3;

		List<HeartNodeEnvelopePacket.Envelope> forAlice = HeartNodeEnvelopeSync.envelopesFor(List.of(alices, far),
				alice, new BlockPos(5, 64, 5), now);
		assertEquals(1, forAlice.size(), "a sanctum past its reach plus the margin is not sent");
		assertEquals(HeartNodeRelation.OWNER, forAlice.getFirst().relation());
		assertEquals(1, forAlice.getFirst().stage());
		assertEquals(alices.synapses(), forAlice.getFirst().synapses());

		List<HeartNodeEnvelopePacket.Envelope> forBob = HeartNodeEnvelopeSync.envelopesFor(List.of(alices, far),
				bob, new BlockPos(5, 64, 5), now);
		assertEquals(HeartNodeRelation.STRANGER, forBob.getFirst().relation(), "standing on it makes no one an owner");

		int edge = (int) (alices.footprint().maxRadius() + HeartNodeEnvelopeSync.SYNC_MARGIN);
		assertTrue(HeartNodeEnvelopeSync.envelopesFor(List.of(alices), bob, new BlockPos(edge, 64, 0), now).size() == 1);
		assertTrue(HeartNodeEnvelopeSync.envelopesFor(List.of(alices), bob, new BlockPos(edge + 1, 64, 0), now).isEmpty());
	}
}

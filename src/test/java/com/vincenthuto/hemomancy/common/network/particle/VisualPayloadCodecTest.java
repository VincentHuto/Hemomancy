package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The folded VFX payloads carry every kind, position, colour and duration through the wire unchanged. */
class VisualPayloadCodecTest {
	@Test
	void everyParticleBurstKindRoundTrips() {
		for (ParticleBurstPacket.Kind kind : ParticleBurstPacket.Kind.values()) {
			var sent = new ParticleBurstPacket(kind, new Vec3(1.25D, -64.5D, 300000.75D), new ParticleColor(131, 64, 255));
			var received = roundTrip(sent);
			assertEquals(kind, received.kind());
			assertEquals(sent.pos(), received.pos());
			assertEquals(131.0F, received.color().getRed());
			assertEquals(64.0F, received.color().getGreen());
			assertEquals(255.0F, received.color().getBlue());
		}
	}

	@Test
	void untintedBurstsCarryABlankColour() {
		var received = roundTrip(new ParticleBurstPacket(ParticleBurstPacket.Kind.POME_PULSE, Vec3.ZERO));
		assertEquals(0.0F, received.color().getRed());
	}

	@Test
	void everyEntityVisualKindRoundTrips() {
		for (EntityVisualPacket.Kind kind : EntityVisualPacket.Kind.values()) {
			var sent = new EntityVisualPacket(kind, 4096, 120);
			FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
			EntityVisualPacket.STREAM_CODEC.encode(buf, sent);
			assertEquals(sent, EntityVisualPacket.STREAM_CODEC.decode(buf));
		}
	}

	private static ParticleBurstPacket roundTrip(ParticleBurstPacket packet) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		ParticleBurstPacket.STREAM_CODEC.encode(buf, packet);
		var decoded = ParticleBurstPacket.STREAM_CODEC.decode(buf);
		assertEquals(0, buf.readableBytes(), "trailing bytes");
		return decoded;
	}
}

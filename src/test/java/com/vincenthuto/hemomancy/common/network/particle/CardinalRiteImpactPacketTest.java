package com.vincenthuto.hemomancy.common.network.particle;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardinalRiteImpactPacketTest {
	@Test
	void packetRoundTripsBriefImpactParameters() {
		CardinalRiteImpactPacket packet = new CardinalRiteImpactPacket(8, 0.52F, 91);
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

		CardinalRiteImpactPacket.encode(buffer, packet);
		CardinalRiteImpactPacket decoded = CardinalRiteImpactPacket.decode(buffer);

		assertEquals(8, decoded.durationTicks());
		assertEquals(0.52F, decoded.peakAlpha(), 0.0001F);
		assertEquals(91, decoded.seed());
	}
}

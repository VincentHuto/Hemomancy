package com.vincenthuto.hemomancy.common.network.boss;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The kit's one payload (Kit API section 16): seven fields survive the wire, and CLEAR is the kit's top bit. */
class SaintMeterPacketTest {
	private static SaintMeterPacket roundTrip(SaintMeterPacket packet) {
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
		SaintMeterPacket.STREAM_CODEC.encode(buf, packet);
		SaintMeterPacket decoded = SaintMeterPacket.STREAM_CODEC.decode(buf);
		assertEquals(0, buf.readableBytes(), "every byte read");
		return decoded;
	}

	@Test
	void everyFieldSurvivesTheWire() {
		SaintMeterPacket packet = new SaintMeterPacket(4127, 61.25F, 240.0F, 0.55F, 3.0F, 0x0000A5C3, 37);
		assertEquals(packet, roundTrip(packet));
		SaintMeterPacket negative = new SaintMeterPacket(-1, -0.0F, Float.MIN_VALUE, 1.0E9F, 0.0F, 0xFFFF, 0);
		assertEquals(negative, roundTrip(negative));
	}

	@Test
	void clearIsTheKitsBitAndSurvivesTheWire() {
		SaintMeterPacket clear = SaintMeterPacket.clear(88);
		assertTrue(clear.clears());
		assertEquals(SaintMeterPacket.CLEAR, clear.flags() & SaintMeterPacket.CLEAR);
		assertEquals(Integer.MIN_VALUE, SaintMeterPacket.CLEAR, "bit 31");
		SaintMeterPacket decoded = roundTrip(clear);
		assertTrue(decoded.clears());
		assertEquals(88, decoded.saintId());
		assertEquals(0, decoded.saintFlags(), "a CLEAR carries no Saint flags");
	}

	@Test
	void theSaintsFlagsAreTheLowSixteenBits() {
		SaintMeterPacket packet = new SaintMeterPacket(1, 0.0F, 1.0F, 0.0F, 1.0F, 0x0003_8001, 0);
		assertFalse(packet.clears());
		assertEquals(0x8001, packet.saintFlags());
	}

	@Test
	void aNegativeCountdownIsClampedToZero() {
		assertEquals(0, new SaintMeterPacket(1, 0.0F, 1.0F, 0.0F, 1.0F, 0, -12).stateTicks());
	}
}

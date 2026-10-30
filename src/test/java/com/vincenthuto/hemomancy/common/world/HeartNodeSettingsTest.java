package com.vincenthuto.hemomancy.common.world;

import com.vincenthuto.hemomancy.common.network.heartnode.HeartNodeSettingsPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The settings' bit layout rides two payloads and the saved data, so it is pinned here. */
class HeartNodeSettingsTest {
	@Test
	void theBitsFollowTheScreensRowOrder() {
		assertEquals(1, HeartNodeSettings.SPREAD);
		assertEquals(1 << 1, HeartNodeSettings.EDGE);
		assertEquals(1 << 2, HeartNodeSettings.THREADS);
		assertEquals(1 << 3, HeartNodeSettings.PULSE);
		assertEquals(1 << 4, HeartNodeSettings.MOTES);
		assertEquals(1 << 5, HeartNodeSettings.SHOW_STRANGERS);
		assertEquals(1 << 6, HeartNodeSettings.EDGE_WARNING);
		assertEquals(0x7F, HeartNodeSettings.ALL);
	}

	@Test
	void everythingShowsByDefaultExceptToStrangers() {
		HeartNodeSettings settings = HeartNodeSettings.DEFAULT;
		for (int bit : new int[] {HeartNodeSettings.SPREAD, HeartNodeSettings.EDGE, HeartNodeSettings.THREADS,
				HeartNodeSettings.PULSE, HeartNodeSettings.MOTES, HeartNodeSettings.EDGE_WARNING}) {
			assertTrue(settings.has(bit), Integer.toBinaryString(bit));
		}
		assertFalse(settings.has(HeartNodeSettings.SHOW_STRANGERS), "a personal place starts hidden");
		assertEquals(2, settings.intensity());
		assertEquals(0b1011111, settings.bits());
	}

	@Test
	void intensityIsClampedAndUnknownBitsAreDropped() {
		assertEquals(0, new HeartNodeSettings(0, -3).intensity());
		assertEquals(HeartNodeSettings.MAX_INTENSITY, new HeartNodeSettings(0, 99).intensity());
		assertEquals(HeartNodeSettings.ALL, new HeartNodeSettings(-1, 0).bits());
		assertEquals(4, HeartNodeSettings.DEFAULT.withIntensity(7).intensity());
	}

	@Test
	void togglesFlipOneBitAndKeepTheRest() {
		HeartNodeSettings off = HeartNodeSettings.DEFAULT.with(HeartNodeSettings.THREADS, false);
		assertFalse(off.has(HeartNodeSettings.THREADS));
		assertEquals(HeartNodeSettings.DEFAULT.bits() & ~HeartNodeSettings.THREADS, off.bits());
		assertEquals(HeartNodeSettings.DEFAULT, off.with(HeartNodeSettings.THREADS, true));
		assertTrue(off.with(HeartNodeSettings.SHOW_STRANGERS, true).has(HeartNodeSettings.SHOW_STRANGERS));
	}

	@Test
	void settingsSurviveTheSaveAndAnOldSaveReadsAsDefault() {
		HeartNodeSettings quiet = new HeartNodeSettings(HeartNodeSettings.SPREAD | HeartNodeSettings.MOTES, 4);
		assertEquals(quiet, HeartNodeSettings.load(quiet.save()));
		assertEquals(HeartNodeSettings.DEFAULT, HeartNodeSettings.load(new CompoundTag()));
	}

	@Test
	void theSettingsPacketCarriesBitsAndIntensityAsTheyAre() {
		HeartNodeSettingsPacket packet = new HeartNodeSettingsPacket(new BlockPos(-40, 12, 900),
				new HeartNodeSettings(HeartNodeSettings.EDGE | HeartNodeSettings.SHOW_STRANGERS, 3));
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
		HeartNodeSettingsPacket.STREAM_CODEC.encode(buf, packet);
		assertEquals(packet, HeartNodeSettingsPacket.STREAM_CODEC.decode(buf));
		assertEquals(0, buf.readableBytes());
	}

	@Test
	void aForgedPacketDecodesToClampedSettings() {
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
		BlockPos.STREAM_CODEC.encode(buf, BlockPos.ZERO);
		buf.writeVarInt(0xFFFF);
		buf.writeByte(120);
		HeartNodeSettingsPacket decoded = HeartNodeSettingsPacket.STREAM_CODEC.decode(buf);
		assertEquals(HeartNodeSettings.ALL, decoded.settings().bits());
		assertEquals(HeartNodeSettings.MAX_INTENSITY, decoded.settings().intensity());
	}
}

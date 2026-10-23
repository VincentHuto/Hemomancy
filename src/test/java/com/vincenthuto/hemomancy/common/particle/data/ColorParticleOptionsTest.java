package com.vincenthuto.hemomancy.common.particle.data;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.vincenthuto.hemomancy.common.particle.type.HemoParticleType;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * The colour particle keeps the wire and command forms of the seven data classes it replaced: the colour as one
 * {@code "r,g,b"} UTF-8 string of floats on the network, and float fields {@code r}, {@code g}, {@code b} in
 * commands. A decoded particle belongs to the type that decoded it.
 */
class ColorParticleOptionsTest {
	private final HemoParticleType<ColorParticleOptions> type =
			new HemoParticleType<>(ColorParticleOptions::codec, ColorParticleOptions::streamCodec);
	private final HemoParticleType<ColorParticleOptions> other =
			new HemoParticleType<>(ColorParticleOptions::codec, ColorParticleOptions::streamCodec);

	@Test
	void networkFormIsTheSerializedColourString() {
		var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
		type.streamCodec().encode(buf, new ColorParticleOptions(type, new ParticleColor(190, 10, 35)));
		byte[] sent = new byte[buf.readableBytes()];
		buf.getBytes(buf.readerIndex(), sent);

		var expected = new FriendlyByteBuf(Unpooled.buffer());
		expected.writeUtf("190.0,10.0,35.0");
		byte[] legacy = new byte[expected.readableBytes()];
		expected.getBytes(0, legacy);
		assertArrayEquals(legacy, sent);

		ColorParticleOptions received = type.streamCodec().decode(buf);
		assertEquals(0, buf.readableBytes(), "trailing bytes");
		assertSame(type, received.getType());
		assertEquals(190.0F, received.color.getRed());
		assertEquals(10.0F, received.color.getGreen());
		assertEquals(35.0F, received.color.getBlue());
	}

	@Test
	void commandFormIsThreeFloatFields() {
		JsonObject json = (JsonObject) type.codec().codec()
				.encodeStart(JsonOps.INSTANCE, new ColorParticleOptions(type, new ParticleColor(255, 0, 12)))
				.getOrThrow();
		assertEquals(255.0F, json.get("r").getAsFloat());
		assertEquals(0.0F, json.get("g").getAsFloat());
		assertEquals(12.0F, json.get("b").getAsFloat());

		ColorParticleOptions parsed = other.codec().codec().parse(JsonOps.INSTANCE, json).getOrThrow();
		assertSame(other, parsed.getType(), "each registered type decodes into itself");
		assertEquals(255.0F, parsed.color.getRed());
		assertEquals(12.0F, parsed.color.getBlue());
	}
}

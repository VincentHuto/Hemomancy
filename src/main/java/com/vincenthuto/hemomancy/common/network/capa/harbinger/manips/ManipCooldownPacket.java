package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server-to-client packet that synchronizes one manipulation's cooldown for
 * radial-menu presentation.
 */
public class ManipCooldownPacket implements CustomPacketPayload {

	public static final Type<ManipCooldownPacket> TYPE = new Type<>(Hemomancy.rloc("manip_cooldown_packet"));
	public static final StreamCodec<FriendlyByteBuf, ManipCooldownPacket> STREAM_CODEC = StreamCodec.of(ManipCooldownPacket::encode, ManipCooldownPacket::decode);

	private final String manipulationId;
	private final int cooldownTicks;

	public ManipCooldownPacket(String manipulationId, int cooldownTicks) {
		this.manipulationId = manipulationId;
		this.cooldownTicks = cooldownTicks;
	}

	public static void encode(final FriendlyByteBuf buffer, final ManipCooldownPacket message) {
		buffer.writeUtf(message.manipulationId);
		buffer.writeInt(message.cooldownTicks);
	}

	public static ManipCooldownPacket decode(final FriendlyByteBuf buffer) {
		return new ManipCooldownPacket(buffer.readUtf(), buffer.readInt());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public int cooldownTicks() {
		return cooldownTicks;
	}

	public String manipulationId() {
		return manipulationId;
	}
}

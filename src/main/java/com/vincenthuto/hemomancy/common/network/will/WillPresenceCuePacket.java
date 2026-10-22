package com.vincenthuto.hemomancy.common.network.will;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

public class WillPresenceCuePacket implements CustomPacketPayload {
	public static final Type<WillPresenceCuePacket> TYPE = new Type<>(Hemomancy.rloc("will_presence_cue"));
	public static final StreamCodec<FriendlyByteBuf, WillPresenceCuePacket> STREAM_CODEC =
			StreamCodec.of(WillPresenceCuePacket::encode, WillPresenceCuePacket::decode);

	private final Vec3 position;

	public WillPresenceCuePacket(Vec3 position) {
		this.position = position == null ? Vec3.ZERO : position;
	}

	public static WillPresenceCuePacket decode(FriendlyByteBuf buf) {
		try {
			return new WillPresenceCuePacket(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
		} catch (IllegalArgumentException | IndexOutOfBoundsException ignored) {
			return new WillPresenceCuePacket(Vec3.ZERO);
		}
	}

	public static void encode(FriendlyByteBuf buf, WillPresenceCuePacket msg) {
		buf.writeDouble(msg.position.x);
		buf.writeDouble(msg.position.y);
		buf.writeDouble(msg.position.z);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public Vec3 position() {
		return position;
	}
}

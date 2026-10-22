package com.vincenthuto.hemomancy.common.network.circus;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PacketSyncCircusPerception(int acclimation, boolean active) implements CustomPacketPayload {
	public static final Type<PacketSyncCircusPerception> TYPE = new Type<>(Hemomancy.rloc("sync_circus_perception"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncCircusPerception> STREAM_CODEC =
			StreamCodec.of(PacketSyncCircusPerception::encode, PacketSyncCircusPerception::decode);

	private static void encode(FriendlyByteBuf buffer, PacketSyncCircusPerception packet) {
		buffer.writeVarInt(packet.acclimation);
		buffer.writeBoolean(packet.active);
	}

	private static PacketSyncCircusPerception decode(FriendlyByteBuf buffer) {
		return new PacketSyncCircusPerception(buffer.readVarInt(), buffer.readBoolean());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

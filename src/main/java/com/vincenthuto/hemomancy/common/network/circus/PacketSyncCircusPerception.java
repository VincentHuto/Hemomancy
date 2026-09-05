package com.vincenthuto.hemomancy.common.network.circus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.overlay.CircusPerceptionOverlay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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

	public static void handle(PacketSyncCircusPerception packet, IPayloadContext context) {
		context.enqueueWork(() -> CircusPerceptionOverlay.setState(packet.acclimation, packet.active));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

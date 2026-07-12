package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.overlay.FungalWhisperVignetteOverlay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncFungalProjection(boolean active, int remainingTicks, int totalTicks)
		implements CustomPacketPayload {
	public static final Type<PacketSyncFungalProjection> TYPE =
			new Type<>(Hemomancy.rloc("sync_fungal_projection"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncFungalProjection> STREAM_CODEC =
			StreamCodec.of(PacketSyncFungalProjection::encode, PacketSyncFungalProjection::decode);

	private static void encode(FriendlyByteBuf buffer, PacketSyncFungalProjection packet) {
		buffer.writeBoolean(packet.active);
		buffer.writeVarInt(packet.remainingTicks);
		buffer.writeVarInt(packet.totalTicks);
	}

	private static PacketSyncFungalProjection decode(FriendlyByteBuf buffer) {
		return new PacketSyncFungalProjection(buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt());
	}

	public static void handle(PacketSyncFungalProjection packet, IPayloadContext context) {
		context.enqueueWork(() -> FungalWhisperVignetteOverlay.setProjectionState(
				packet.active, packet.remainingTicks, packet.totalTicks));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

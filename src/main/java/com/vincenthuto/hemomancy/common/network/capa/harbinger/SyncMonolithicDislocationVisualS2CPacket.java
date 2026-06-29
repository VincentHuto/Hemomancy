package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.MonolithicDislocationClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncMonolithicDislocationVisualS2CPacket(int entityId, int durationTicks)
		implements CustomPacketPayload {
	public static final Type<SyncMonolithicDislocationVisualS2CPacket> TYPE =
			new Type<>(Hemomancy.rloc("sync_monolithic_dislocation_visual"));
	public static final StreamCodec<FriendlyByteBuf, SyncMonolithicDislocationVisualS2CPacket> STREAM_CODEC =
			StreamCodec.of(SyncMonolithicDislocationVisualS2CPacket::encode,
					SyncMonolithicDislocationVisualS2CPacket::decode);

	public static void encode(FriendlyByteBuf buffer, SyncMonolithicDislocationVisualS2CPacket packet) {
		buffer.writeVarInt(packet.entityId());
		buffer.writeVarInt(packet.durationTicks());
	}

	public static SyncMonolithicDislocationVisualS2CPacket decode(FriendlyByteBuf buffer) {
		return new SyncMonolithicDislocationVisualS2CPacket(buffer.readVarInt(), buffer.readVarInt());
	}

	public static void handle(SyncMonolithicDislocationVisualS2CPacket packet, IPayloadContext ctx) {
		ctx.enqueueWork(() ->
				MonolithicDislocationClientState.markActive(packet.entityId(), packet.durationTicks()));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

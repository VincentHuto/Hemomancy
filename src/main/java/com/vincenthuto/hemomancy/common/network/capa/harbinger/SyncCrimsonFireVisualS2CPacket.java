package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.CrimsonFireClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncCrimsonFireVisualS2CPacket(int entityId, int durationTicks) implements CustomPacketPayload {
	public static final Type<SyncCrimsonFireVisualS2CPacket> TYPE =
			new Type<>(Hemomancy.rloc("sync_crimson_fire_visual"));
	public static final StreamCodec<FriendlyByteBuf, SyncCrimsonFireVisualS2CPacket> STREAM_CODEC =
			StreamCodec.of(SyncCrimsonFireVisualS2CPacket::encode, SyncCrimsonFireVisualS2CPacket::decode);

	public static void encode(FriendlyByteBuf buffer, SyncCrimsonFireVisualS2CPacket packet) {
		buffer.writeVarInt(packet.entityId());
		buffer.writeVarInt(packet.durationTicks());
	}

	public static SyncCrimsonFireVisualS2CPacket decode(FriendlyByteBuf buffer) {
		return new SyncCrimsonFireVisualS2CPacket(buffer.readVarInt(), buffer.readVarInt());
	}

	public static void handle(SyncCrimsonFireVisualS2CPacket packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> CrimsonFireClientState.markActive(packet.entityId(), packet.durationTicks()));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

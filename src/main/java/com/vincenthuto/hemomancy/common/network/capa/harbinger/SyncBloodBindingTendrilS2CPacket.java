package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.BloodBindingTendrilClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncBloodBindingTendrilS2CPacket(int casterId, int targetId, int durationTicks, long seed)
		implements CustomPacketPayload {
	public static final Type<SyncBloodBindingTendrilS2CPacket> TYPE =
			new Type<>(Hemomancy.rloc("sync_blood_binding_tendril"));
	public static final StreamCodec<FriendlyByteBuf, SyncBloodBindingTendrilS2CPacket> STREAM_CODEC =
			StreamCodec.of(SyncBloodBindingTendrilS2CPacket::encode, SyncBloodBindingTendrilS2CPacket::decode);

	private static void encode(FriendlyByteBuf buffer, SyncBloodBindingTendrilS2CPacket packet) {
		buffer.writeVarInt(packet.casterId());
		buffer.writeVarInt(packet.targetId());
		buffer.writeVarInt(packet.durationTicks());
		buffer.writeLong(packet.seed());
	}

	private static SyncBloodBindingTendrilS2CPacket decode(FriendlyByteBuf buffer) {
		return new SyncBloodBindingTendrilS2CPacket(buffer.readVarInt(), buffer.readVarInt(),
				buffer.readVarInt(), buffer.readLong());
	}

	public static void handle(SyncBloodBindingTendrilS2CPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> BloodBindingTendrilClientState.start(packet.casterId(), packet.targetId(),
				packet.durationTicks(), packet.seed()));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

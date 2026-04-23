package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Server → Client packet: synchronises the player's current initiatory degree.
 */
public class PacketSyncDegree implements CustomPacketPayload {

	public static final Type<PacketSyncDegree> TYPE = new Type<>(Hemomancy.rloc("packet_sync_degree"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncDegree> STREAM_CODEC = StreamCodec.of(PacketSyncDegree::encode, PacketSyncDegree::decode);

	private final int degreeNumber;

	public PacketSyncDegree(int degreeNumber) {
		this.degreeNumber = degreeNumber;
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncDegree msg) {
		buf.writeInt(msg.degreeNumber);
	}

	public static PacketSyncDegree decode(FriendlyByteBuf buf) {
		return new PacketSyncDegree(buf.readInt());
	}

	public static void handle(final PacketSyncDegree msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (Minecraft.getInstance().player != null) {
				Minecraft.getInstance().HemoCapabilityAccess.getInitiatoryDegree(player)
						.ifPresent(degree -> degree.setDegreeNumber(msg.degreeNumber));
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

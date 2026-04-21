package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.client.data.BloodlinePoolClientData;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Server → Client: Syncs the current state of the shared bloodline pool
 * so the client can display it in the BloodlinePoolScreen.
 */
public class PacketSyncBloodlinePool implements CustomPacketPayload {

	public static final Type<PacketSyncBloodlinePool> TYPE = new Type<>(Hemomancy.rloc("packet_sync_bloodline_pool"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncBloodlinePool> STREAM_CODEC = StreamCodec.of(PacketSyncBloodlinePool::encode, PacketSyncBloodlinePool::decode);

	private final float poolVolume;
	private final float poolMax;
	private final int memberCount;

	public PacketSyncBloodlinePool(float poolVolume, float poolMax, int memberCount) {
		this.poolVolume = poolVolume;
		this.poolMax = poolMax;
		this.memberCount = memberCount;
	}

	public static void encode(PacketSyncBloodlinePool msg, FriendlyByteBuf buf) {
		buf.writeFloat(msg.poolVolume);
		buf.writeFloat(msg.poolMax);
		buf.writeInt(msg.memberCount);
	}

	public static PacketSyncBloodlinePool decode(FriendlyByteBuf buf) {
		return new PacketSyncBloodlinePool(buf.readFloat(), buf.readFloat(), buf.readInt());
	}

	public static void handle(final PacketSyncBloodlinePool msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			BloodlinePoolClientData.set(msg.poolVolume, msg.poolMax, msg.memberCount);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

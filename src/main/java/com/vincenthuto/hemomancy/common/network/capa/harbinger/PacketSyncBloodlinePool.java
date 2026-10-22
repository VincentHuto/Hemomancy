package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

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

	public static void encode(FriendlyByteBuf buf, PacketSyncBloodlinePool msg) {
		buf.writeFloat(msg.poolVolume);
		buf.writeFloat(msg.poolMax);
		buf.writeInt(msg.memberCount);
	}

	public static PacketSyncBloodlinePool decode(FriendlyByteBuf buf) {
		return new PacketSyncBloodlinePool(buf.readFloat(), buf.readFloat(), buf.readInt());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public int memberCount() {
		return memberCount;
	}

	public float poolMax() {
		return poolMax;
	}

	public float poolVolume() {
		return poolVolume;
	}
}

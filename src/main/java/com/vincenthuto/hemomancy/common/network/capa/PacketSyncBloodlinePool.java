package com.vincenthuto.hemomancy.common.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.client.data.BloodlinePoolClientData;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

/**
 * Server → Client: Syncs the current state of the shared bloodline pool
 * so the client can display it in the BloodlinePoolScreen.
 */
public class PacketSyncBloodlinePool {

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

	public static void handle(PacketSyncBloodlinePool msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			BloodlinePoolClientData.set(msg.poolVolume, msg.poolMax, msg.memberCount);
		});
		ctx.get().setPacketHandled(true);
	}
}

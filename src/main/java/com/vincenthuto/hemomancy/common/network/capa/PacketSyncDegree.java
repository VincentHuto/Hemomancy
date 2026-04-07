package com.vincenthuto.hemomancy.common.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server → Client packet: synchronises the player's current initiatory degree.
 */
public class PacketSyncDegree {

	private final int degreeNumber;

	public PacketSyncDegree(int degreeNumber) {
		this.degreeNumber = degreeNumber;
	}

	public static void encode(PacketSyncDegree msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.degreeNumber);
	}

	public static PacketSyncDegree decode(FriendlyByteBuf buf) {
		return new PacketSyncDegree(buf.readInt());
	}

	public static void handle(PacketSyncDegree msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (Minecraft.getInstance().player != null) {
				Minecraft.getInstance().player.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA)
						.ifPresent(degree -> degree.setDegreeNumber(msg.degreeNumber));
			}
		});
		ctx.get().setPacketHandled(true);
	}
}

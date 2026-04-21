package com.vincenthuto.hemomancy.common.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.client.data.ActiveBloodCraftClientData;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

/**
 * Server → Client: Tells the client to spawn a collapsing bloody ring
 * animation at the given position when a blood structure recipe is crafted.
 */
public class PacketBloodCraftRing {

	private final BlockPos center;
	private final float startRadius;
	private final float centerY;
	private final int durationTicks;

	public PacketBloodCraftRing(BlockPos center, float startRadius, float centerY, int durationTicks) {
		this.center = center;
		this.startRadius = startRadius;
		this.centerY = centerY;
		this.durationTicks = durationTicks;
	}

	public static void encode(PacketBloodCraftRing msg, FriendlyByteBuf buf) {
		buf.writeBlockPos(msg.center);
		buf.writeFloat(msg.startRadius);
		buf.writeFloat(msg.centerY);
		buf.writeInt(msg.durationTicks);
	}

	public static PacketBloodCraftRing decode(FriendlyByteBuf buf) {
		BlockPos center = buf.readBlockPos();
		float startRadius = buf.readFloat();
		float centerY = buf.readFloat();
		int durationTicks = buf.readInt();
		return new PacketBloodCraftRing(center, startRadius, centerY, durationTicks);
	}

	public static void handle(PacketBloodCraftRing msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ActiveBloodCraftClientData.addRing(new ActiveBloodCraftClientData.CraftRingEntry(
					msg.center, msg.startRadius, msg.centerY, msg.durationTicks));
		});
		ctx.get().setPacketHandled(true);
	}
}

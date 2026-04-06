package com.vincenthuto.hemomancy.common.network.capa;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server → Client: Syncs all active cardinal rite positions, sizes, and
 * progress so the client can render glowing boundary circles.
 */
public class PacketSyncActiveRites {

	private final List<ActiveRiteClientData.RiteEntry> entries;

	public PacketSyncActiveRites(List<ActiveRiteClientData.RiteEntry> entries) {
		this.entries = entries;
	}

	public static void encode(PacketSyncActiveRites msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entries.size());
		for (ActiveRiteClientData.RiteEntry entry : msg.entries) {
			buf.writeBlockPos(entry.getCenter());
			buf.writeInt(entry.getRiteSize());
			buf.writeDouble(entry.getProgress());
		}
	}

	public static PacketSyncActiveRites decode(FriendlyByteBuf buf) {
		int count = buf.readInt();
		List<ActiveRiteClientData.RiteEntry> entries = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			BlockPos center = buf.readBlockPos();
			int riteSize = buf.readInt();
			double progress = buf.readDouble();
			entries.add(new ActiveRiteClientData.RiteEntry(center, riteSize, progress));
		}
		return new PacketSyncActiveRites(entries);
	}

	public static void handle(PacketSyncActiveRites msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ActiveRiteClientData.set(msg.entries);
		});
		ctx.get().setPacketHandled(true);
	}
}

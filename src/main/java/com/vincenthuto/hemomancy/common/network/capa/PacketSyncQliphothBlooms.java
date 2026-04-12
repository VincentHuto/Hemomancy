package com.vincenthuto.hemomancy.common.network.capa;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.client.data.QliphothBloomClientData;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server → Client: Syncs all active Qliphoth Bloom positions and radii
 * so the client can render the tree and pulsing rings.
 */
public class PacketSyncQliphothBlooms {

	private final List<QliphothBloomClientData.BloomEntry> entries;

	public PacketSyncQliphothBlooms(List<QliphothBloomClientData.BloomEntry> entries) {
		this.entries = entries;
	}

	public static void encode(PacketSyncQliphothBlooms msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entries.size());
		for (QliphothBloomClientData.BloomEntry entry : msg.entries) {
			buf.writeBlockPos(entry.getCenter());
			buf.writeInt(entry.getChunkRadius());
		}
	}

	public static PacketSyncQliphothBlooms decode(FriendlyByteBuf buf) {
		int count = buf.readInt();
		List<QliphothBloomClientData.BloomEntry> entries = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			BlockPos center = buf.readBlockPos();
			int chunkRadius = buf.readInt();
			entries.add(new QliphothBloomClientData.BloomEntry(center, chunkRadius));
		}
		return new PacketSyncQliphothBlooms(entries);
	}

	public static void handle(PacketSyncQliphothBlooms msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			QliphothBloomClientData.set(msg.entries);
		});
		ctx.get().setPacketHandled(true);
	}
}

package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.client.data.QliphothBloomClientData;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Server → Client: Syncs all active Qliphoth Bloom positions and radii
 * so the client can render the tree and pulsing rings.
 */
public class PacketSyncQliphothBlooms implements CustomPacketPayload {

	public static final Type<PacketSyncQliphothBlooms> TYPE = new Type<>(Hemomancy.rloc("packet_sync_qliphoth_blooms"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncQliphothBlooms> STREAM_CODEC = StreamCodec.of(PacketSyncQliphothBlooms::encode, PacketSyncQliphothBlooms::decode);

	private final List<QliphothBloomClientData.BloomEntry> entries;

	public PacketSyncQliphothBlooms(List<QliphothBloomClientData.BloomEntry> entries) {
		this.entries = entries;
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncQliphothBlooms msg) {
		buf.writeInt(msg.entries.size());
		for (QliphothBloomClientData.BloomEntry entry : msg.entries) {
			buf.writeBlockPos(entry.getCenter());
			buf.writeInt(entry.getChunkRadius());
			buf.writeInt(entry.getPomesDropped());
		}
	}

	public static PacketSyncQliphothBlooms decode(FriendlyByteBuf buf) {
		int count = buf.readInt();
		List<QliphothBloomClientData.BloomEntry> entries = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			BlockPos center = buf.readBlockPos();
			int chunkRadius = buf.readInt();
			int pomesDropped = buf.readInt();
			entries.add(new QliphothBloomClientData.BloomEntry(center, chunkRadius, pomesDropped));
		}
		return new PacketSyncQliphothBlooms(entries);
	}

	public static void handle(final PacketSyncQliphothBlooms msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			QliphothBloomClientData.set(msg.entries);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

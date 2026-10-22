package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomData;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Server → Client: Syncs all active Qliphoth Bloom positions and radii
 * so the client can render the tree and pulsing rings.
 */
public class PacketSyncQliphothBlooms implements CustomPacketPayload {

	public static final Type<PacketSyncQliphothBlooms> TYPE = new Type<>(Hemomancy.rloc("packet_sync_qliphoth_blooms"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncQliphothBlooms> STREAM_CODEC = StreamCodec.of(PacketSyncQliphothBlooms::encode, PacketSyncQliphothBlooms::decode);

	private final List<QliphothBloomData.BloomEntry> entries;

	public PacketSyncQliphothBlooms(List<QliphothBloomData.BloomEntry> entries) {
		this.entries = entries;
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncQliphothBlooms msg) {
		buf.writeInt(msg.entries.size());
		for (QliphothBloomData.BloomEntry entry : msg.entries) {
			buf.writeBlockPos(entry.getCenter());
			buf.writeInt(entry.getChunkRadius());
			buf.writeInt(entry.getPomesDropped());
			buf.writeByte(entry.getSeveredState());
		}
	}

	public static PacketSyncQliphothBlooms decode(FriendlyByteBuf buf) {
		int count = buf.readInt();
		List<QliphothBloomData.BloomEntry> entries = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			BlockPos center = buf.readBlockPos();
			int chunkRadius = buf.readInt();
			int pomesDropped = buf.readInt();
			int severedState = buf.readByte();
			entries.add(new QliphothBloomData.BloomEntry(center, chunkRadius, pomesDropped, severedState));
		}
		return new PacketSyncQliphothBlooms(entries);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public List<QliphothBloomData.BloomEntry> entries() {
		return entries;
	}
}

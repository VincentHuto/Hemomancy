package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.SanctumBoundaryClientData;
import com.vincenthuto.hemomancy.common.event.worldevent.SanctumBoundaryRelation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketSyncSanctumBoundaries implements CustomPacketPayload {
	public static final Type<PacketSyncSanctumBoundaries> TYPE = new Type<>(
			Hemomancy.rloc("packet_sync_sanctum_boundaries"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncSanctumBoundaries> STREAM_CODEC = StreamCodec.of(
			PacketSyncSanctumBoundaries::encode, PacketSyncSanctumBoundaries::decode);

	private final List<Entry> entries;

	public PacketSyncSanctumBoundaries(List<Entry> entries) {
		this.entries = List.copyOf(entries);
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncSanctumBoundaries msg) {
		buf.writeInt(msg.entries.size());
		for (Entry entry : msg.entries) {
			buf.writeBlockPos(entry.center());
			buf.writeFloat(entry.radius());
			buf.writeUUID(entry.ownerUuid());
			buf.writeEnum(entry.relation());
		}
	}

	public static PacketSyncSanctumBoundaries decode(FriendlyByteBuf buf) {
		int count = buf.readInt();
		List<Entry> entries = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			BlockPos center = buf.readBlockPos();
			float radius = buf.readFloat();
			UUID ownerUuid = buf.readUUID();
			SanctumBoundaryRelation relation = buf.readEnum(SanctumBoundaryRelation.class);
			entries.add(new Entry(center, radius, ownerUuid, relation));
		}
		return new PacketSyncSanctumBoundaries(entries);
	}

	public static void handle(final PacketSyncSanctumBoundaries msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> SanctumBoundaryClientData.setEntries(msg.entries));
	}

	public List<Entry> entries() {
		return entries;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public record Entry(BlockPos center, float radius, UUID ownerUuid, SanctumBoundaryRelation relation) {
	}
}

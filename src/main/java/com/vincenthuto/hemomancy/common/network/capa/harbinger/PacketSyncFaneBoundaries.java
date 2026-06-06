package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.FaneBoundaryClientData;
import com.vincenthuto.hemomancy.common.event.worldevent.FaneBoundaryRelation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketSyncFaneBoundaries implements CustomPacketPayload {
	public static final Type<PacketSyncFaneBoundaries> TYPE = new Type<>(
			Hemomancy.rloc("packet_sync_fane_boundaries"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncFaneBoundaries> STREAM_CODEC = StreamCodec.of(
			PacketSyncFaneBoundaries::encode, PacketSyncFaneBoundaries::decode);

	private final List<Entry> entries;

	public PacketSyncFaneBoundaries(List<Entry> entries) {
		this.entries = List.copyOf(entries);
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncFaneBoundaries msg) {
		buf.writeInt(msg.entries.size());
		for (Entry entry : msg.entries) {
			buf.writeBlockPos(entry.heart());
			buf.writeInt(entry.stakes().size());
			for (BlockPos stake : entry.stakes()) {
				buf.writeBlockPos(stake);
			}
			buf.writeFloat(entry.radius());
			buf.writeUUID(entry.ownerUuid());
			buf.writeEnum(entry.relation());
		}
	}

	public static PacketSyncFaneBoundaries decode(FriendlyByteBuf buf) {
		int count = buf.readInt();
		List<Entry> entries = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			BlockPos heart = buf.readBlockPos();
			int stakeCount = buf.readInt();
			List<BlockPos> stakes = new ArrayList<>(stakeCount);
			for (int s = 0; s < stakeCount; s++) {
				stakes.add(buf.readBlockPos());
			}
			float radius = buf.readFloat();
			UUID ownerUuid = buf.readUUID();
			FaneBoundaryRelation relation = buf.readEnum(FaneBoundaryRelation.class);
			entries.add(new Entry(heart, stakes, radius, ownerUuid, relation));
		}
		return new PacketSyncFaneBoundaries(entries);
	}

	public static void handle(final PacketSyncFaneBoundaries msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> FaneBoundaryClientData.setEntries(msg.entries));
	}

	public List<Entry> entries() {
		return entries;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public record Entry(BlockPos heart, List<BlockPos> stakes, float radius, UUID ownerUuid, FaneBoundaryRelation relation) {
	}
}

package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveBloodStructureFeedClientData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record PacketBloodStructureFeed(List<BlockPos> positions, float progress, int visibleTicks,
		boolean clear, long channelId) implements CustomPacketPayload {
	public static final Type<PacketBloodStructureFeed> TYPE = new Type<>(Hemomancy.rloc("packet_blood_structure_feed"));
	public static final StreamCodec<FriendlyByteBuf, PacketBloodStructureFeed> STREAM_CODEC =
			StreamCodec.of(PacketBloodStructureFeed::encode, PacketBloodStructureFeed::decode);

	public PacketBloodStructureFeed {
		positions = List.copyOf(positions);
	}

	public PacketBloodStructureFeed(List<BlockPos> positions, float progress, int visibleTicks, boolean clear) {
		this(positions, progress, visibleTicks, clear, 0L);
	}

	public static void encode(FriendlyByteBuf buf, PacketBloodStructureFeed msg) {
		buf.writeBoolean(msg.clear);
		buf.writeVarInt(msg.positions.size());
		for (BlockPos pos : msg.positions) {
			buf.writeBlockPos(pos);
		}
		buf.writeFloat(msg.progress);
		buf.writeVarInt(msg.visibleTicks);
		buf.writeLong(msg.channelId);
	}

	public static PacketBloodStructureFeed decode(FriendlyByteBuf buf) {
		boolean clear = buf.readBoolean();
		int size = buf.readVarInt();
		List<BlockPos> positions = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			positions.add(buf.readBlockPos());
		}
		float progress = buf.readFloat();
		int visibleTicks = buf.readVarInt();
		long channelId = buf.readLong();
		return new PacketBloodStructureFeed(positions, progress, visibleTicks, clear, channelId);
	}

	public static void handle(final PacketBloodStructureFeed msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (msg.clear) {
				ActiveBloodStructureFeedClientData.clear(msg.channelId, msg.positions);
			} else {
				ActiveBloodStructureFeedClientData.upsert(msg.channelId, msg.positions, msg.progress, msg.visibleTicks);
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

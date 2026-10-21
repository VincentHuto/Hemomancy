package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveBloodStructureFeedClientData;
import com.vincenthuto.hemomancy.common.event.BloodFeedStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Server → Client: a blood feed shell's progress. {@code origin} is where an infusion's veins start
 * crawling from; other styles ignore it.
 */
public record PacketBloodStructureFeed(List<BlockPos> positions, float progress, int visibleTicks,
		boolean clear, long channelId, BloodFeedStyle style, Vec3 origin) implements CustomPacketPayload {
	public static final Type<PacketBloodStructureFeed> TYPE = new Type<>(Hemomancy.rloc("packet_blood_structure_feed"));
	public static final StreamCodec<FriendlyByteBuf, PacketBloodStructureFeed> STREAM_CODEC =
			StreamCodec.of(PacketBloodStructureFeed::encode, PacketBloodStructureFeed::decode);

	public PacketBloodStructureFeed {
		positions = List.copyOf(positions);
	}

	public PacketBloodStructureFeed(List<BlockPos> positions, float progress, int visibleTicks, boolean clear) {
		this(positions, progress, visibleTicks, clear, 0L);
	}

	public PacketBloodStructureFeed(List<BlockPos> positions, float progress, int visibleTicks, boolean clear,
			long channelId) {
		this(positions, progress, visibleTicks, clear, channelId, BloodFeedStyle.STRUCTURE, Vec3.ZERO);
	}

	/** A single-block infusion: veins spread from {@code origin}, then the block cracks and melts. */
	public static PacketBloodStructureFeed infusion(BlockPos pos, Vec3 origin, float progress, int visibleTicks,
			boolean clear) {
		return new PacketBloodStructureFeed(List.of(pos), progress, visibleTicks, clear, 0L,
				BloodFeedStyle.INFUSION, origin);
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
		buf.writeByte(msg.style.ordinal());
		buf.writeVec3(msg.origin);
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
		BloodFeedStyle style = BloodFeedStyle.byId(buf.readByte());
		Vec3 origin = buf.readVec3();
		return new PacketBloodStructureFeed(positions, progress, visibleTicks, clear, channelId, style, origin);
	}

	public static void handle(final PacketBloodStructureFeed msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (msg.clear) {
				ActiveBloodStructureFeedClientData.clear(msg.channelId, msg.positions);
			} else {
				ActiveBloodStructureFeedClientData.upsert(msg.channelId, msg.positions, msg.progress, msg.visibleTicks,
						msg.style, msg.origin);
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

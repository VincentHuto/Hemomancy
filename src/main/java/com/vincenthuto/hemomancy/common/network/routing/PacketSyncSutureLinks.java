package com.vincenthuto.hemomancy.common.network.routing;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.SutureLinkClientData;
import com.vincenthuto.hemomancy.common.routing.BloodRoutingMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record PacketSyncSutureLinks(List<Entry> entries) implements CustomPacketPayload {
	public static final Type<PacketSyncSutureLinks> TYPE = new Type<>(Hemomancy.rloc("sync_suture_links"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncSutureLinks> STREAM_CODEC =
			StreamCodec.of(PacketSyncSutureLinks::encode, PacketSyncSutureLinks::decode);

	public PacketSyncSutureLinks {
		entries = List.copyOf(entries);
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncSutureLinks msg) {
		buf.writeVarInt(msg.entries.size());
		for (Entry entry : msg.entries) {
			buf.writeBlockPos(entry.pos);
			buf.writeEnum(entry.mode);
			buf.writeBoolean(entry.bloodlineEnabled);
		}
	}

	public static PacketSyncSutureLinks decode(FriendlyByteBuf buf) {
		int size = buf.readVarInt();
		List<Entry> entries = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			entries.add(new Entry(buf.readBlockPos(), buf.readEnum(BloodRoutingMode.class), buf.readBoolean()));
		}
		return new PacketSyncSutureLinks(entries);
	}

	@OnlyIn(Dist.CLIENT)
	public static void handle(final PacketSyncSutureLinks msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> SutureLinkClientData.setEntries(msg.entries.stream()
				.map(entry -> new SutureLinkClientData.Entry(entry.pos, entry.mode, entry.bloodlineEnabled))
				.toList()));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public record Entry(BlockPos pos, BloodRoutingMode mode, boolean bloodlineEnabled) {
	}
}

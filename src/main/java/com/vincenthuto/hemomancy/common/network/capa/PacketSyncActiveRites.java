package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Server → Client: Syncs all active cardinal rite positions, sizes, and
 * progress so the client can render glowing boundary circles.
 */
public class PacketSyncActiveRites implements CustomPacketPayload {

	public static final Type<PacketSyncActiveRites> TYPE = new Type<>(Hemomancy.rloc("packet_sync_active_rites"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncActiveRites> STREAM_CODEC = StreamCodec.of(PacketSyncActiveRites::encode, PacketSyncActiveRites::decode);

	private final List<ActiveRiteClientData.RiteEntry> entries;

	public PacketSyncActiveRites(List<ActiveRiteClientData.RiteEntry> entries) {
		this.entries = entries;
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncActiveRites msg) {
		buf.writeInt(msg.entries.size());
		for (ActiveRiteClientData.RiteEntry entry : msg.entries) {
			buf.writeBlockPos(entry.getCenter());
			buf.writeInt(entry.getRiteSize());
			buf.writeDouble(entry.getProgress());
			buf.writeResourceLocation(entry.getRecipeId());
			buf.writeBoolean(entry.isUnstained());
		}
	}

	public static PacketSyncActiveRites decode(FriendlyByteBuf buf) {
		int count = buf.readInt();
		List<ActiveRiteClientData.RiteEntry> entries = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			BlockPos center = buf.readBlockPos();
			int riteSize = buf.readInt();
			double progress = buf.readDouble();
			ResourceLocation recipeId = buf.readResourceLocation();
			boolean unstained = buf.readBoolean();
			entries.add(new ActiveRiteClientData.RiteEntry(center, riteSize, progress, recipeId, unstained));
		}
		return new PacketSyncActiveRites(entries);
	}

	public static void handle(final PacketSyncActiveRites msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ActiveRiteClientData.set(msg.entries);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

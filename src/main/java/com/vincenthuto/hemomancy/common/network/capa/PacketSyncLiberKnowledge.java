package com.vincenthuto.hemomancy.common.network.capa;

import java.util.LinkedHashSet;
import java.util.Set;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.DiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.ILiberKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.LiberKnowledge;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketSyncLiberKnowledge implements CustomPacketPayload {
	public static final Type<PacketSyncLiberKnowledge> TYPE = new Type<>(Hemomancy.rloc("packet_sync_liber_knowledge"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncLiberKnowledge> STREAM_CODEC =
			StreamCodec.of(PacketSyncLiberKnowledge::encode, PacketSyncLiberKnowledge::decode);

	private final Set<ResourceLocation> entries;
	private final Set<ResourceLocation> memos;

	public PacketSyncLiberKnowledge(ILiberKnowledge knowledge) {
		this.entries = new LinkedHashSet<>(knowledge.getUnlockedEntries());
		this.memos = new LinkedHashSet<>(knowledge.getKnownMemos());
	}

	private PacketSyncLiberKnowledge(Set<ResourceLocation> entries, Set<ResourceLocation> memos) {
		this.entries = entries;
		this.memos = memos;
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncLiberKnowledge msg) {
		writeIds(buf, msg.entries);
		writeIds(buf, msg.memos);
	}

	public static PacketSyncLiberKnowledge decode(FriendlyByteBuf buf) {
		return new PacketSyncLiberKnowledge(readIds(buf), readIds(buf));
	}

	public static void handle(PacketSyncLiberKnowledge msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) {
				return;
			}
			HemoCapabilityAccess.getLiberKnowledge(mc.player).ifPresent(knowledge -> {
				LiberKnowledge synced = new LiberKnowledge();
				for (ResourceLocation entry : msg.entries) {
					synced.unlockEntry(entry, DiscoverySource.OTHER);
				}
				for (ResourceLocation memo : msg.memos) {
					synced.recordMemo(memo);
				}
				knowledge.setFrom(synced);
			});
		});
	}

	private static void writeIds(FriendlyByteBuf buf, Set<ResourceLocation> ids) {
		buf.writeVarInt(ids.size());
		for (ResourceLocation id : ids) {
			buf.writeResourceLocation(id);
		}
	}

	private static Set<ResourceLocation> readIds(FriendlyByteBuf buf) {
		Set<ResourceLocation> ids = new LinkedHashSet<>();
		int count = buf.readVarInt();
		for (int i = 0; i < count; i++) {
			ids.add(buf.readResourceLocation());
		}
		return ids;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

package com.vincenthuto.hemomancy.common.network.capa;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.DiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.ILiberKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.LiberKnowledge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketSyncLiberKnowledge implements CustomPacketPayload {
	public static final Type<PacketSyncLiberKnowledge> TYPE = new Type<>(Hemomancy.rloc("packet_sync_liber_knowledge"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncLiberKnowledge> STREAM_CODEC =
			StreamCodec.of(PacketSyncLiberKnowledge::encode, PacketSyncLiberKnowledge::decode);

	private final Map<ResourceLocation, Set<DiscoverySource>> entrySources;
	private final Set<ResourceLocation> memos;

	public PacketSyncLiberKnowledge(ILiberKnowledge knowledge) {
		Map<ResourceLocation, Set<DiscoverySource>> copy = new LinkedHashMap<>();
		knowledge.getEntrySources().forEach((entry, sources) ->
				copy.put(entry, sources.isEmpty()
						? EnumSet.noneOf(DiscoverySource.class)
						: EnumSet.copyOf(sources)));
		this.entrySources = copy;
		this.memos = new LinkedHashSet<>(knowledge.getKnownMemos());
	}

	private PacketSyncLiberKnowledge(Map<ResourceLocation, Set<DiscoverySource>> entrySources, Set<ResourceLocation> memos) {
		this.entrySources = entrySources;
		this.memos = memos;
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncLiberKnowledge msg) {
		buf.writeVarInt(msg.entrySources.size());
		for (Map.Entry<ResourceLocation, Set<DiscoverySource>> e : msg.entrySources.entrySet()) {
			buf.writeResourceLocation(e.getKey());
			Set<DiscoverySource> sources = e.getValue();
			buf.writeVarInt(sources.size());
			for (DiscoverySource source : sources) {
				buf.writeUtf(source.name());
			}
		}
		writeIds(buf, msg.memos);
	}

	public static PacketSyncLiberKnowledge decode(FriendlyByteBuf buf) {
		int entryCount = buf.readVarInt();
		Map<ResourceLocation, Set<DiscoverySource>> entrySources = new LinkedHashMap<>();
		for (int i = 0; i < entryCount; i++) {
			ResourceLocation key = buf.readResourceLocation();
			int sourceCount = buf.readVarInt();
			Set<DiscoverySource> sources = EnumSet.noneOf(DiscoverySource.class);
			for (int j = 0; j < sourceCount; j++) {
				try {
					sources.add(DiscoverySource.valueOf(buf.readUtf()));
				} catch (IllegalArgumentException e) {
					// Unknown source name — could be a server/client version mismatch.
					// Log and skip so the rest of the packet can still be applied.
					Hemomancy.LOGGER.warn("[LiberKnowledge] Unknown DiscoverySource in sync packet: {}", e.getMessage());
				}
			}
			entrySources.put(key, sources);
		}
		return new PacketSyncLiberKnowledge(entrySources, readIds(buf));
	}

	public static void handle(PacketSyncLiberKnowledge msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null) {
				return;
			}
			HemoCapabilityAccess.getLiberKnowledge(player).ifPresent(knowledge -> {
				// Count entries the player already has before applying the sync so we
				// can detect genuinely new unlocks and show a toast notification.
				int previousEntryCount = knowledge.getUnlockedEntries().size();

				LiberKnowledge synced = new LiberKnowledge();
				msg.entrySources.forEach((entry, sources) -> {
					if (sources.isEmpty()) {
						synced.unlockEntry(entry, DiscoverySource.OTHER);
					} else {
						for (DiscoverySource source : sources) {
							synced.unlockEntry(entry, source);
						}
					}
				});
				for (ResourceLocation memo : msg.memos) {
					synced.recordMemo(memo);
				}
				knowledge.setFrom(synced);

				// Only show the toast when there are NEW entries relative to what the
				// player had before this sync, and they already had at least one entry
				// previously (to avoid spamming on first login).
				int newEntries = knowledge.getUnlockedEntries().size() - previousEntryCount;
				if (newEntries > 0 && previousEntryCount > 0) {
					final int count = newEntries;
					Minecraft mc = Minecraft.getInstance();
					if (mc != null && mc.getToasts() != null) {
						showNewEntriestoast(mc, count);
					}
				}
			});
		});
	}

	@OnlyIn(Dist.CLIENT)
	private static void showNewEntriestoast(Minecraft mc, int count) {
		SystemToast.addOrUpdate(
				mc.getToasts(),
				SystemToast.SystemToastId.NARRATOR_TOGGLE,
				Component.translatable("toast.hemomancy.liber_new_entries.title"),
				Component.translatable("toast.hemomancy.liber_new_entries.body", count));
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

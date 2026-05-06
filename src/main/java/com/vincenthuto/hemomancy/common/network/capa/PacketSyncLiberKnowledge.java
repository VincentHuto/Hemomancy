package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.LiberKnowledge;
import com.vincenthuto.hutoslib.common.book.knowledge.CommonDiscoverySource;
import com.vincenthuto.hutoslib.common.book.knowledge.IBookKnowledge;
import com.vincenthuto.hutoslib.common.book.knowledge.IDiscoverySource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class PacketSyncLiberKnowledge implements CustomPacketPayload {
	public static final Type<PacketSyncLiberKnowledge> TYPE = new Type<>(Hemomancy.rloc("packet_sync_liber_knowledge"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncLiberKnowledge> STREAM_CODEC =
			StreamCodec.of(PacketSyncLiberKnowledge::encode, PacketSyncLiberKnowledge::decode);

	private final Map<ResourceLocation, Set<IDiscoverySource>> entrySources;
	private final Set<ResourceLocation> memos;
	private final Set<ResourceLocation> markUnreadEntries;

	public PacketSyncLiberKnowledge(IBookKnowledge knowledge) {
		this(knowledge, Set.of());
	}

	public PacketSyncLiberKnowledge(IBookKnowledge knowledge, Set<ResourceLocation> markUnreadEntries) {
		Map<ResourceLocation, Set<IDiscoverySource>> copy = new LinkedHashMap<>();
		Map<ResourceLocation, Set<IDiscoverySource>> sourcesByEntry = knowledge.getEntrySources();
		for (ResourceLocation entry : knowledge.getUnlockedEntries()) {
			Set<IDiscoverySource> sourceCopy = new LinkedHashSet<>(
					sourcesByEntry.getOrDefault(entry, Set.of()));
			copy.put(entry, sourceCopy);
		}
		// Preserve any legacy/inconsistent source records too, but do not let them
		// be the only thing that defines the packet contents.
		sourcesByEntry.forEach((entry, sources) -> {
			copy.computeIfAbsent(entry, ignored -> new LinkedHashSet<>()).addAll(sources);
		});
		this.entrySources = copy;
		this.memos = new LinkedHashSet<>(knowledge.getKnownMemos());
		this.markUnreadEntries = new LinkedHashSet<>(markUnreadEntries);
	}

	private PacketSyncLiberKnowledge(Map<ResourceLocation, Set<IDiscoverySource>> entrySources, Set<ResourceLocation> memos,
			Set<ResourceLocation> markUnreadEntries) {
		this.entrySources = entrySources;
		this.memos = memos;
		this.markUnreadEntries = markUnreadEntries;
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncLiberKnowledge msg) {
		buf.writeVarInt(msg.entrySources.size());
		for (Map.Entry<ResourceLocation, Set<IDiscoverySource>> e : msg.entrySources.entrySet()) {
			buf.writeResourceLocation(e.getKey());
			Set<IDiscoverySource> sources = e.getValue();
			buf.writeVarInt(sources.size());
			for (IDiscoverySource source : sources) {
				buf.writeUtf(source.name());
			}
		}
		writeIds(buf, msg.memos);
		writeIds(buf, msg.markUnreadEntries);
	}

	public static PacketSyncLiberKnowledge decode(FriendlyByteBuf buf) {
		int entryCount = buf.readVarInt();
		Map<ResourceLocation, Set<IDiscoverySource>> entrySources = new LinkedHashMap<>();
		for (int i = 0; i < entryCount; i++) {
			ResourceLocation key = buf.readResourceLocation();
			int sourceCount = buf.readVarInt();
			Set<IDiscoverySource> sources = new LinkedHashSet<>();
			for (int j = 0; j < sourceCount; j++) {
				String name = buf.readUtf();
				IDiscoverySource source = decodeSource(name);
				if (source != null) {
					sources.add(source);
				} else {
					Hemomancy.LOGGER.warn("[LiberKnowledge] Unknown IDiscoverySource in sync packet: {}", name);
				}
			}
			entrySources.put(key, sources);
		}
		Set<ResourceLocation> memos = readIds(buf);
		Set<ResourceLocation> markUnreadEntries = readIds(buf);
		return new PacketSyncLiberKnowledge(entrySources, memos, markUnreadEntries);
	}

	/**
	 * Resolves a source name to an {@link IDiscoverySource}, trying
	 * {@link HemomancyDiscoverySource} first (Hemomancy-specific), then
	 * {@link CommonDiscoverySource} (shared HutosLib sources). Returns
	 * {@code null} for unknown names so the caller can decide how to handle them.
	 */
	private static IDiscoverySource decodeSource(String name) {
		try {
			return HemomancyDiscoverySource.valueOf(name);
		} catch (IllegalArgumentException ignored) {
		}
		try {
			return CommonDiscoverySource.valueOf(name);
		} catch (IllegalArgumentException ignored) {
		}
		return null;
	}

	public static void handle(PacketSyncLiberKnowledge msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null) {
				return;
			}
			HemoCapabilityAccess.getLiberKnowledge(player).ifPresent(knowledge -> {
				LiberKnowledge synced = new LiberKnowledge();
				msg.entrySources.forEach((entry, sources) -> {
					if (sources.isEmpty()) {
						synced.unlockEntry(entry, CommonDiscoverySource.OTHER);
					} else {
						for (IDiscoverySource source : sources) {
							synced.unlockEntry(entry, source);
						}
					}
				});
				for (ResourceLocation memo : msg.memos) {
					synced.recordMemo(memo);
				}
				knowledge.setFrom(synced);
				// If the player currently has a Liber/guide screen open, rebuild
				// its visible chapter list against the freshly-synced knowledge
				// instead of waiting for them to close+reopen.
				if (FMLEnvironment.dist == Dist.CLIENT) {
					com.vincenthuto.hemomancy.client.ClientLiberScreenHooks.markEntriesUnreadAndRefresh(
							player.getUUID(), msg.markUnreadEntries);
				}
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


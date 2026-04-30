package com.vincenthuto.hemomancy.common.network.capa;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.DiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.LiberKnowledge;
import com.vincenthuto.hutoslib.common.book.knowledge.IBookKnowledge;
import com.vincenthuto.hutoslib.common.book.knowledge.IDiscoverySource;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketSyncLiberKnowledge implements CustomPacketPayload {
	public static final Type<PacketSyncLiberKnowledge> TYPE = new Type<>(Hemomancy.rloc("packet_sync_liber_knowledge"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncLiberKnowledge> STREAM_CODEC =
			StreamCodec.of(PacketSyncLiberKnowledge::encode, PacketSyncLiberKnowledge::decode);

	private final Map<ResourceLocation, Set<IDiscoverySource>> entrySources;
	private final Set<ResourceLocation> memos;

	public PacketSyncLiberKnowledge(IBookKnowledge knowledge) {
		Map<ResourceLocation, Set<IDiscoverySource>> copy = new LinkedHashMap<>();
		knowledge.getEntrySources().forEach((entry, sources) -> {
			Set<IDiscoverySource> sourceCopy = new LinkedHashSet<>(sources);
			copy.put(entry, sourceCopy);
		});
		this.entrySources = copy;
		this.memos = new LinkedHashSet<>(knowledge.getKnownMemos());
	}

	private PacketSyncLiberKnowledge(Map<ResourceLocation, Set<IDiscoverySource>> entrySources, Set<ResourceLocation> memos) {
		this.entrySources = entrySources;
		this.memos = memos;
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
				try {
					sources.add(DiscoverySource.valueOf(name));
				} catch (IllegalArgumentException e) {
					// Unknown source name — could be a server/client version mismatch.
					// Log and skip so the rest of the packet can still be applied.
					Hemomancy.LOGGER.warn("[LiberKnowledge] Unknown DiscoverySource in sync packet: {}", name);
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
				LiberKnowledge synced = new LiberKnowledge();
				msg.entrySources.forEach((entry, sources) -> {
					if (sources.isEmpty()) {
						synced.unlockEntry(entry, DiscoverySource.OTHER);
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

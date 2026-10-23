package com.vincenthuto.hemomancy.common.data;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodInjectionData;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import com.vincenthuto.hemomancy.common.network.DatapackSyncPacket;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

/**
 * R-6: server data that remote clients mirror. On every datapack sync each channel sends its definitions, as the
 * JSON text the server loaded, in one {@link DatapackSyncPacket}; the client parses them with the same rules as the
 * server. Add a channel here instead of a new sync payload.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class DatapackSyncChannels {
	/**
	 * @param skipIntegrated an integrated client already shares the server's registry, so the sync is not applied
	 */
	public record Channel(ResourceLocation id, int maxDefinitions, int maxJsonLength,
			Supplier<Map<ResourceLocation, String>> server, Consumer<Map<ResourceLocation, String>> client,
			boolean skipIntegrated) {
	}

	public static final List<Channel> CHANNELS = List.of(
			new Channel(Hemomancy.rloc("blood_profiles"), BloodProfileData.MAX_DEFINITIONS,
					BloodProfileData.MAX_JSON_LENGTH, () -> BloodProfileData.snapshot(false).json(),
					BloodProfileData::receive, false),
			new Channel(Hemomancy.rloc("blood_injection"), BloodInjectionData.MAX_DEFINITIONS,
					BloodInjectionData.MAX_JSON_LENGTH, () -> BloodInjectionData.snapshot(false).json(),
					BloodInjectionData::receive, false),
			new Channel(Hemomancy.rloc("cardinal_rite_floor"), CardinalRiteFloorRegistry.MAX_SYNCED_FLOORS,
					CardinalRiteFloorRegistry.MAX_SYNCED_JSON_LENGTH, CardinalRiteFloorRegistry::syncSnapshot,
					CardinalRiteFloorRegistry::receiveSync, true));

	private DatapackSyncChannels() {
	}

	public static Channel get(ResourceLocation id) {
		for (Channel channel : CHANNELS) {
			if (channel.id().equals(id)) return channel;
		}
		throw new IllegalArgumentException("Unknown datapack sync channel " + id);
	}

	/** Applies a received channel on the client. */
	public static void receive(ResourceLocation id, Map<ResourceLocation, String> definitions, boolean integrated) {
		Channel channel = get(id);
		if (channel.skipIntegrated() && integrated) return;
		channel.client().accept(definitions);
	}

	@SubscribeEvent
	public static void sync(OnDatapackSyncEvent event) {
		for (Channel channel : CHANNELS) {
			var packet = new DatapackSyncPacket(channel.id(), channel.server().get());
			if (event.getPlayer() != null) PacketHandler.sendToPlayer(event.getPlayer(), packet);
			else PacketHandler.sendToAll(packet);
		}
	}
}

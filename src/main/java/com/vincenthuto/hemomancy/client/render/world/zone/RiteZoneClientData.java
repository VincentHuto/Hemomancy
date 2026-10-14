package com.vincenthuto.hemomancy.client.render.world.zone;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.zone.RiteZoneSyncPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import java.util.List;

/** The visible rite zones of the dimension the local player is in, as last sent by the server. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class RiteZoneClientData {
	private static volatile List<RiteZoneSyncPacket.Zone> zones = List.of();

	private RiteZoneClientData() {}

	public static void set(List<RiteZoneSyncPacket.Zone> synced) {
		zones = List.copyOf(synced);
	}

	public static List<RiteZoneSyncPacket.Zone> zones() {
		return zones;
	}

	@SubscribeEvent
	public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		zones = List.of();
	}
}

package com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram;

import com.vincenthuto.hemomancy.Hemomancy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** A weaver who leaves lets go of their lane; a stopped server forgets every unfurled engram and trace. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class EngramEvents {
	private EngramEvents() {
	}

	@SubscribeEvent
	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		EngramTracing.end(event.getEntity());
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		EngramTracing.clear();
		ActiveEngramLooms.clear();
	}
}

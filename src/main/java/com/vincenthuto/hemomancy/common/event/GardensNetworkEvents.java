package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.world.GardensNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Feeds each player's tick to the Gardens' network. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class GardensNetworkEvents {
	private GardensNetworkEvents() {}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (event.getEntity() instanceof ServerPlayer player) GardensNetwork.tick(player);
	}
}

package com.vincenthuto.hemomancy.common.rite.floor;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.CardinalRiteFloorSyncPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CardinalRiteFloorSync {
	private CardinalRiteFloorSync() {
	}

	@SubscribeEvent
	public static void sync(OnDatapackSyncEvent event) {
		var packet = new CardinalRiteFloorSyncPacket(CardinalRiteFloorRegistry.syncSnapshot());
		if (event.getPlayer() != null) PacketDistributor.sendToPlayer(event.getPlayer(), packet);
		else PacketDistributor.sendToAllPlayers(packet);
	}
}

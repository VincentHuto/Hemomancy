package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar;

import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.PacketSyncScarsState;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ScarStateSyncEvents {

	private ScarStateSyncEvents() {
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		if (event.getEntity() instanceof ServerPlayer receiver && event.getTarget() instanceof ServerPlayer source) {
			syncToPlayer(source, receiver);
		}
	}

	public static void syncToClient(ServerPlayer source) {
		PacketHandler.sendToPlayerAndTrackers(source, new PacketSyncScarsState(source, HemoCapabilityAccess.getScarState(source)));
	}

	public static void syncToPlayer(ServerPlayer source, ServerPlayer receiver) {
		PacketHandler.sendToPlayer(receiver, new PacketSyncScarsState(source, HemoCapabilityAccess.getScarState(source)));
	}
}

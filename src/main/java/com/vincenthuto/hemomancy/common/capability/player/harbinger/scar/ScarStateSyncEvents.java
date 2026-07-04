package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.PacketSyncScarsState;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ScarStateSyncEvents {

	private ScarStateSyncEvents() {
	}

	@SubscribeEvent
	public static void playerJoin(EntityJoinLevelEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			syncToClient(player);
		}
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			syncToClient(player);
		}
	}

	@SubscribeEvent
	public static void playerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			syncToClient(player);
		}
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			syncToClient(player);
		}
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		if (event.getEntity() instanceof ServerPlayer receiver && event.getTarget() instanceof ServerPlayer source) {
			syncToPlayer(source, receiver);
		}
	}

	public static void syncToClient(ServerPlayer source) {
		HemoCapabilityAccess.getScarState(source).ifPresent(scars ->
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(source, new PacketSyncScarsState(source, scars)));
	}

	public static void syncToPlayer(ServerPlayer source, ServerPlayer receiver) {
		HemoCapabilityAccess.getScarState(source).ifPresent(scars ->
				PacketDistributor.sendToPlayer(receiver, new PacketSyncScarsState(source, scars)));
	}
}

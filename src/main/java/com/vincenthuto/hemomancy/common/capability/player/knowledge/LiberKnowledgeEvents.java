package com.vincenthuto.hemomancy.common.capability.player.knowledge;

import java.util.Set;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncLiberKnowledge;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class LiberKnowledgeEvents {
	private LiberKnowledgeEvents() {
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			sync(player);
		}
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			sync(player);
		}
	}

	@SubscribeEvent
	public static void playerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			sync(player);
		}
	}

	@SubscribeEvent
	public static void playerJoin(EntityJoinLevelEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			sync(player);
		}
	}

	public static void sync(ServerPlayer player) {
		HemoCapabilityAccess.getLiberKnowledge(player)
				.ifPresent(knowledge -> PacketHandler.sendToPlayer(player, new PacketSyncLiberKnowledge(knowledge)));
	}

	public static void sync(ServerPlayer player, Set<ResourceLocation> markUnreadEntries) {
		HemoCapabilityAccess.getLiberKnowledge(player)
				.ifPresent(knowledge -> PacketHandler.sendToPlayer(player,
						new PacketSyncLiberKnowledge(knowledge, markUnreadEntries)));
	}
}

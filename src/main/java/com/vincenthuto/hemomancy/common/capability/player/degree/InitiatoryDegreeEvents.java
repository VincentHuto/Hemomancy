package com.vincenthuto.hemomancy.common.capability.player.degree;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncDegree;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonTrialEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class InitiatoryDegreeEvents {

	public static void syncDegree(ServerPlayer player, IInitiatoryDegree degree) {
		PacketHandler.sendToPlayer(player, new PacketSyncDegree(degree.getDegreeNumber()));
		PuppeteerSummonTrialEvents.awardTrialRecipes(player, degree.getDegreeNumber());
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		HemoCapabilityAccess.getInitiatoryDegree(player).ifPresent(degree ->
				{
					syncDegree(player, degree);
					LiberKnowledgeHelper.unlockForDegree(player, degree.getDegreeNumber());
				});
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		HemoCapabilityAccess.getInitiatoryDegree(player).ifPresent(degree ->
				syncDegree(player, degree));
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerRespawnEvent event) {
		Player player = event.getEntity();
		if (!player.level().isClientSide) {
			HemoCapabilityAccess.getInitiatoryDegree(player).ifPresent(degree ->
					syncDegree((ServerPlayer) player, degree));
		}
	}
}

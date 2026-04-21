package com.vincenthuto.hemomancy.common.capability.player.degree;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncDegree;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.GAME)
public class InitiatoryDegreeEvents {

	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(Hemomancy.rloc("initiatory_degree"), new InitiatoryDegreeProvider());
		}
	}

	public static void syncDegree(ServerPlayer player, IInitiatoryDegree degree) {
		PacketHandler.CHANNELBLOODVOLUME.send(
				PacketDistributor.PLAYER.with(() -> player),
				new PacketSyncDegree(degree.getDegreeNumber()));
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		player.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA).ifPresent(degree ->
				syncDegree(player, degree));
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		player.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA).ifPresent(degree ->
				syncDegree(player, degree));
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerRespawnEvent event) {
		Player player = event.getEntity();
		if (!player.level().isClientSide) {
			player.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA).ifPresent(degree ->
					syncDegree((ServerPlayer) player, degree));
		}
	}

	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			Player original = event.getOriginal();
			Player newPlayer = event.getEntity();
			original.reviveCaps();
			IInitiatoryDegree oldDegree = original.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA)
					.orElseThrow(IllegalStateException::new);
			IInitiatoryDegree newDegree = newPlayer.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA)
					.orElseThrow(IllegalStateException::new);
			newDegree.setDegreeNumber(oldDegree.getDegreeNumber());
			original.invalidateCaps();
		}
	}
}

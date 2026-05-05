package com.vincenthuto.hemomancy.common.capability.player.unstained.stillart;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.unstained.stillarts.StillArt;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.unstained.KnownStillArtsServerPacket;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class KnownStillArtEvents {
	public static void sync(ServerPlayer player, IKnownStillArts known) {
		PacketHandler.sendToPlayer(player, new KnownStillArtsServerPacket(known));
	}

	public static boolean grantArt(ServerPlayer player, StillArt art) {
		return HemoCapabilityAccess.getKnownStillArts(player)
				.map(known -> {
					boolean learned = known.learnArt(art);
					if (learned) {
						sync(player, known);
					}
					return learned;
				})
				.orElse(false);
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		HemoCapabilityAccess.getKnownStillArts(player).ifPresent(known -> sync(player, known));
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		HemoCapabilityAccess.getKnownStillArts(player).ifPresent(known -> sync(player, known));
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			HemoCapabilityAccess.getKnownStillArts(player).ifPresent(known -> sync(player, known));
		}
	}
}

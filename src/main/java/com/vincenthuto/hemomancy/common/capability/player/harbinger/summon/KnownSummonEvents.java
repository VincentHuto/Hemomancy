package com.vincenthuto.hemomancy.common.capability.player.harbinger.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.summon.KnownSummonsServerPacket;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinition;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class KnownSummonEvents {
	public static void sync(ServerPlayer player, IKnownSummons known) {
		if (player == null || player.connection == null) {
			return;
		}
		PacketHandler.sendToPlayer(player, new KnownSummonsServerPacket(known));
	}

	public static boolean grantSummon(ServerPlayer player, PuppeteerSummonDefinition definition) {
		return HemoCapabilityAccess.getKnownSummons(player)
				.map(known -> {
					boolean learned = known.learn(definition);
					if (learned) {
						sync(player, known);
					}
					return learned;
				})
				.orElse(false);
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			HemoCapabilityAccess.getKnownSummons(player).ifPresent(known -> sync(player, known));
		}
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			HemoCapabilityAccess.getKnownSummons(player).ifPresent(known -> sync(player, known));
		}
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			HemoCapabilityAccess.getKnownSummons(player).ifPresent(known -> sync(player, known));
		}
	}
}

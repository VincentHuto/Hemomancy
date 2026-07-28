package com.vincenthuto.hemomancy.common.capability.player.harbinger.rite;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncIchorianKnowledge;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class IchorianKnowledgeEvents {
	private IchorianKnowledgeEvents() {
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) sync(player);
	}

	@SubscribeEvent
	public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) sync(player);
	}

	public static void sync(ServerPlayer player) {
		HemoCapabilityAccess.getIchorianKnowledge(player)
				.ifPresent(knowledge -> PacketHandler.sendToPlayer(player,
						new PacketSyncIchorianKnowledge(knowledge)));
	}
}

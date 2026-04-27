package com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class LiberDiscoveryEvents {
	private LiberDiscoveryEvents() {
	}

	@SubscribeEvent
	public static void onItemPickup(ItemEntityPickupEvent.Post event) {
		if (event.getPlayer() instanceof ServerPlayer player) {
			LiberKnowledgeHelper.unlockForItemPickup(player, event.getOriginalStack());
		}
	}

	@SubscribeEvent
	public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			LiberKnowledgeHelper.unlockForAdvancement(player, event.getAdvancement().id());
		}
	}
}

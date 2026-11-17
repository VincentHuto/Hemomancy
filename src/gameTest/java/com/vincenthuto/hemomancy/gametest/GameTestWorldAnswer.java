package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.gametest.framework.GameTestServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Keeps the world's ambient scavengers (core-loop Track 10) out of the headless GameTest server. Hundreds of suites
 * lay wet stains and pour chum next to joined players under template roofs on Normal difficulty; a vampire bat drawn
 * to one would drink another suite's stain mid-assertion. The daily-loop suite calls the draws directly. Client
 * reviews and acceptance drivers keep the server default.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class GameTestWorldAnswer {
	private GameTestWorldAnswer() {
	}

	@SubscribeEvent
	public static void quietScavengers(ServerStartedEvent event) {
		if (!(event.getServer() instanceof GameTestServer) || HemoServerConfig.WORLD_ANSWER_SCAVENGERS == null) return;
		HemoServerConfig.WORLD_ANSWER_SCAVENGERS.set(false);
		Hemomancy.LOGGER.info("GameTest server: world-answer scavengers off");
	}
}

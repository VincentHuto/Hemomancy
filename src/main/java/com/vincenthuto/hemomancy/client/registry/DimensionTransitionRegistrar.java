package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.ChamberTransitionScreen;
import com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionTransitionScreenEvent;

/** The Chamber of Will has no loading screen: both ways through it, the change is veiled by the void. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class DimensionTransitionRegistrar {
	private DimensionTransitionRegistrar() {
	}

	@SubscribeEvent
	public static void registerTransitions(RegisterDimensionTransitionScreenEvent event) {
		event.registerIncomingEffect(ChamberOfWillManager.CHAMBER_OF_WILL,
				(levelReceived, reason) -> new ChamberTransitionScreen(levelReceived, reason, true));
		event.registerOutgoingEffect(ChamberOfWillManager.CHAMBER_OF_WILL,
				(levelReceived, reason) -> new ChamberTransitionScreen(levelReceived, reason, false));
	}
}

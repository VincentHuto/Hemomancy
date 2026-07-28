package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CardinalRiteThreatEvents {
	private static final String RITE_BOUND = "HemomancyRiteBound";

	private CardinalRiteThreatEvents() {
	}

	@SubscribeEvent
	public static void suppressDrops(LivingDropsEvent event) {
		if (event.getEntity().getPersistentData().getBoolean(RITE_BOUND)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void suppressExperience(LivingExperienceDropEvent event) {
		if (event.getEntity().getPersistentData().getBoolean(RITE_BOUND)) event.setDroppedExperience(0);
	}
}

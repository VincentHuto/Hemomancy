package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.Hemomancy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

/**
 * The Downfall deed's faithful (bog bodies raised as Bog Revenants, tagged {@link #TAG}) leave nothing behind: no
 * drops and no experience. They are the chamber's dead, not a loot source.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class PutricielFaithfulEvents {
	public static final String TAG = "putriciel_faithful";

	private PutricielFaithfulEvents() {
	}

	@SubscribeEvent
	public static void onDrops(LivingDropsEvent event) {
		if (event.getEntity().getTags().contains(TAG)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onExperience(LivingExperienceDropEvent event) {
		if (event.getEntity().getTags().contains(TAG)) event.setCanceled(true);
	}
}

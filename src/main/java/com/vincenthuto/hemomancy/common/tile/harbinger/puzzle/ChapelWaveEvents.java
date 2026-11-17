package com.vincenthuto.hemomancy.common.tile.harbinger.puzzle;

import com.vincenthuto.hemomancy.Hemomancy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;

/**
 * A Downfall wave (tagged {@link #TAG} when it rises) leaves nothing behind: no drops and no experience, so a Chapel
 * cannot be farmed by failing it over and over. It never despawns, Peaceful included (NQ-08-22), so a Downfall cannot
 * finish itself when its wave vanishes. The tag is saved with the mob, so a reload keeps the rule.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ChapelWaveEvents {
	public static final String TAG = "hemomancy_chapel_wave";

	private ChapelWaveEvents() {
	}

	@SubscribeEvent
	public static void onDrops(LivingDropsEvent event) {
		if (event.getEntity().getTags().contains(TAG)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onDespawn(MobDespawnEvent event) {
		if (event.getEntity().getTags().contains(TAG)) event.setResult(MobDespawnEvent.Result.DENY);
	}

	@SubscribeEvent
	public static void onExperience(LivingExperienceDropEvent event) {
		if (event.getEntity().getTags().contains(TAG)) event.setCanceled(true);
	}
}

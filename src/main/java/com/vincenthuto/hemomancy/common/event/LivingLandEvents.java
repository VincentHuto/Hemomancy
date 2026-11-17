package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.world.livingland.LivingLandLedger;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Once a second, each level settles the Living Land blocks nothing carries any more (core-loop Track 9): a strike
 * that unloaded or was lost, a raise whose player logged out, anything left lifted across a restart.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class LivingLandEvents {
	private static final int SWEEP_INTERVAL = 20;

	private LivingLandEvents() {
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % SWEEP_INTERVAL != 0) return;
		LivingLandLedger ledger = LivingLandLedger.get(level);
		if (ledger.size() > 0) ledger.sweep(level, level.getGameTime());
	}
}

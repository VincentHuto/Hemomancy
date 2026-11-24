package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * The floorless tier's one tick: pending Blood Structure crafts count down, hand-fed structures settle and infusions
 * run, each once per server tick on its own level's tick (F-11). Cardinal Rites tick apart, in
 * {@code HarbingerCardinalRiteEvents}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class BloodStructureTickEvents {
	private BloodStructureTickEvents() {
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		PendingBloodCraftManager.tick(level);
		BloodStructureFeedManager.tick(level);
		BloodInfusionManager.tick(level);
	}
}

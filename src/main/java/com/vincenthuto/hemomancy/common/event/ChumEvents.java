package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.world.chum.ChumPools;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Chum's world side (core-loop Track 7): each level's pools bloom near players and now and then draw a fish. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ChumEvents {
	private ChumEvents() {
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (event.getLevel() instanceof ServerLevel level) ChumPools.tick(level);
	}
}

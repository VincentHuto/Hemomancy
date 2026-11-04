package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.gametest.framework.GameTestServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;

/**
 * Stops vanilla despawning fixture mobs on the GameTest server. A mob despawns by its distance to the nearest player, and
 * in a suite of far-apart plots that is any test player anywhere, so mobs in other plots were discarded at random (audit
 * row A-35). A test that exercises the despawn rule itself wraps its call in {@link #exercisingDespawn}. Review clients
 * and real play never reach this: they are not a {@link GameTestServer}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class FixtureMobDespawnGuard {
	private static boolean exercising;

	private FixtureMobDespawnGuard() {
	}

	/** Runs {@code check} under the ordinary despawn rule. */
	public static void exercisingDespawn(Runnable check) {
		exercising = true;
		try {
			check.run();
		} finally {
			exercising = false;
		}
	}

	@SubscribeEvent
	public static void onMobDespawn(MobDespawnEvent event) {
		if (exercising || !(event.getEntity().getServer() instanceof GameTestServer)) return;
		event.setResult(MobDespawnEvent.Result.DENY);
	}
}

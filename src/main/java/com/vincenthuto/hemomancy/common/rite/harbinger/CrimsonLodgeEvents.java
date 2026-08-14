package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Legacy hook retained so older worlds with Crimson Lodge data do not crash.
 * Spatial territory mechanics now belong exclusively to Founding Fanes.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class CrimsonLodgeEvents {

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		// Intentionally left blank.
	}

	/**
	 * Legacy helper retained for compatibility. Ledger and boundary logic now use
	 * Founding Fane ownership instead.
	 */
	public static boolean isInCrimsonLodge(ServerPlayer player) {
		return false;
	}
}

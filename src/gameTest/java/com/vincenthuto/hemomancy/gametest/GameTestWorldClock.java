package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonSavedData;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Holds the GameTest server at noon. With the daylight cycle running, a long suite reaches dusk (day time 12542),
 * where the Blood Moon roll (1 in 7) can start a Blood Moon that discounts every manipulation's cost and breaks
 * exact-cost assertions depending on how far the suite has run. Only the headless GameTest server is touched; client
 * reviews and acceptance drivers set their own time.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class GameTestWorldClock {
    public static final long FIXED_DAY_TIME = 6000L;

    private GameTestWorldClock() {
    }

    @SubscribeEvent
    public static void freezeAtNoon(ServerStartedEvent event) {
        if (!(event.getServer() instanceof GameTestServer server)) return;
        ServerLevel overworld = server.overworld();
        overworld.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
        overworld.setDayTime(FIXED_DAY_TIME);
        BloodMoonSavedData.get(overworld).stop();
        Hemomancy.LOGGER.info("GameTest world clock frozen at day time {} with no Blood Moon", FIXED_DAY_TIME);
    }
}

package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class StationUpgradeEvents {
    private StationUpgradeEvents() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        HemoCapabilityAccess.stationUpgrades(player).deliver(player);
        HemoCapabilityAccess.stationUpgrades(player).unlockEligibleLessons(player);
    }

    /**
     * Lessons follow eligibility, which also moves with degree, blood activation and First Separation, so they are
     * backfilled on a slow cadence. The state itself is sent by PlayerStateSync when it changes.
     */
    @SubscribeEvent
    public static void onTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0)
            HemoCapabilityAccess.stationUpgrades(player).unlockEligibleLessons(player);
    }
}

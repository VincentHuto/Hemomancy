package com.vincenthuto.hemomancy.common.station;

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
        StationUpgradeMigration.migrate(player);
        HemoCapabilityAccess.stationUpgrades(player).deliver(player);
        HemoCapabilityAccess.stationUpgrades(player).sync(player, true);
    }

    @SubscribeEvent
    public static void onTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0)
            HemoCapabilityAccess.stationUpgrades(player).sync(player, false);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            HemoCapabilityAccess.stationUpgrades(player).sync(player, true);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            HemoCapabilityAccess.stationUpgrades(player).sync(player, true);
    }
}

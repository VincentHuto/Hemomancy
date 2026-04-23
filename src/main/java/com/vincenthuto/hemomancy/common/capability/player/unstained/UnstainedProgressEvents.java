package com.vincenthuto.hemomancy.common.capability.player.unstained;

import net.neoforged.fml.common.EventBusSubscriber;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncUnstainedProgress;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class UnstainedProgressEvents {

    public static void syncProgress(ServerPlayer player, IUnstainedProgress progress) {
        PacketHandler.sendToPlayer(player, new PacketSyncUnstainedProgress(progress));
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress ->
                syncProgress(player, progress));
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerChangedDimensionEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress ->
                syncProgress(player, progress));
    }

    @SubscribeEvent
    public static void playerRespawn(PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress ->
                    syncProgress((ServerPlayer) player, progress));
        }
    }
}

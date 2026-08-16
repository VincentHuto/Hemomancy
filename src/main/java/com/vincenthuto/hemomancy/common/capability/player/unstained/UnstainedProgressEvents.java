package com.vincenthuto.hemomancy.common.capability.player.unstained;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.KnownStillArtEvents;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.unstained.PacketSyncUnstainedProgress;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class UnstainedProgressEvents {

    public static void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(UnstainedProgressEvents::initUnstainedStageIcons);
    }

    private static void initUnstainedStageIcons() {
        EnumPurityStage.CORRUPTED.setIconItem(() -> new ItemStack(ItemInit.blood_stained_stone.get()));
        EnumPurityStage.TAINTED.setIconItem(() -> new ItemStack(ItemInit.lethean_poppy_wreath.get()));
        EnumPurityStage.CLEANSING.setIconItem(() -> new ItemStack(ItemInit.pale_distillate.get()));
        EnumPurityStage.ABSOLVED.setIconItem(() -> new ItemStack(ItemInit.tears_of_silthmere.get()));
        EnumPurityStage.PURIFIED.setIconItem(() -> new ItemStack(ItemInit.pallid_icon.get()));
        EnumClarityStage.AWAKENED.setIconItem(() -> new ItemStack(ItemInit.cleansed_blood_crystal_shard.get()));
        EnumClarityStage.DISCERNING.setIconItem(() -> new ItemStack(ItemInit.silver_chalice.get()));
        EnumClarityStage.VIGILANT.setIconItem(() -> new ItemStack(ItemInit.pale_silver_ingot.get()));
        EnumClarityStage.RESOLUTE.setIconItem(() -> new ItemStack(ItemInit.liber_immaculatus.get()));
        EnumClarityStage.ENLIGHTENED.setIconItem(() -> new ItemStack(ItemInit.pallid_icon.get()));
    }

    public static void syncProgress(ServerPlayer player, IUnstainedProgress progress) {
        KnownStillArtEvents.grantEligibleArts(player, progress);
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

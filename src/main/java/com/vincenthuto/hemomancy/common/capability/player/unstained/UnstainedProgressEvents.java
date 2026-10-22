package com.vincenthuto.hemomancy.common.capability.player.unstained;

import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.KnownStillArtEvents;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.unstained.PacketSyncUnstainedProgress;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

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
}

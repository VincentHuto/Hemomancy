package com.vincenthuto.hemomancy.common.capability.player.unstained;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncUnstainedProgress;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UnstainedProgressEvents {

    @SubscribeEvent
    public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(Hemomancy.rloc("unstained_progress"), new UnstainedProgressProvider());
        }
    }

    public static void syncProgress(ServerPlayer player, IUnstainedProgress progress) {
        PacketHandler.CHANNELBLOODVOLUME.send(
                PacketDistributor.PLAYER.with(() -> player),
                new PacketSyncUnstainedProgress(progress));
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(progress ->
                syncProgress(player, progress));
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerChangedDimensionEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(progress ->
                syncProgress(player, progress));
    }

    @SubscribeEvent
    public static void playerRespawn(PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(progress ->
                    syncProgress((ServerPlayer) player, progress));
        }
    }

    @SubscribeEvent
    public static void playerDeath(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player original = event.getOriginal();
            Player newPlayer = event.getEntity();
            original.reviveCaps();
            IUnstainedProgress oldProgress = original.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
                    .orElseThrow(IllegalStateException::new);
            IUnstainedProgress newProgress = newPlayer.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
                    .orElseThrow(IllegalStateException::new);
            newProgress.setBegunPurification(oldProgress.hasBegunPurification());
            newProgress.setPurity(oldProgress.getPurity());
            newProgress.setClarityUnlocked(oldProgress.hasClarityUnlocked());
            newProgress.setClarity(oldProgress.getClarity());
            newProgress.setLastManipulationTick(oldProgress.getLastManipulationTick());
            // Milestones
            for (int i = 0; i < oldProgress.getHemoMobKills(); i++) newProgress.addHemoMobKill();
            for (int i = 0; i < oldProgress.getUndeadKills(); i++) newProgress.addUndeadKill();
            for (int i = 0; i < oldProgress.getHostileKills(); i++) newProgress.addHostileKill();
            for (int i = 0; i < oldProgress.getFlawlessKills(); i++) newProgress.addFlawlessKill();
            for (int i = 0; i < oldProgress.getAnimalsBreed(); i++) newProgress.addAnimalBreed();
            for (int i = 0; i < oldProgress.getCropsPlanted(); i++) newProgress.addCropPlanted();
            for (int i = 0; i < oldProgress.getAdvancementsEarned(); i++) newProgress.addAdvancementEarned();
            for (int i = 0; i < oldProgress.getNightsSlept(); i++) newProgress.addNightSlept();
            for (int i = 0; i < oldProgress.getPetsHealed(); i++) newProgress.addPetHealed();
            newProgress.setSleptWithHemolysis(oldProgress.hasSleptWithHemolysis());
            newProgress.setKilledFirstHemoMob(oldProgress.hasKilledFirstHemoMob());
            newProgress.setReachedAbstinence(oldProgress.hasReachedAbstinence());
            newProgress.setEmptiedBlood(oldProgress.hasEmptiedBlood());
            newProgress.setEarnedAdvancement(oldProgress.hasEarnedAdvancement());
            newProgress.setSilverWardEnabled(oldProgress.isSilverWardEnabled());
            newProgress.setVerdigrisAuraEnabled(oldProgress.isVerdigrisAuraEnabled());
            original.invalidateCaps();
        }
    }
}

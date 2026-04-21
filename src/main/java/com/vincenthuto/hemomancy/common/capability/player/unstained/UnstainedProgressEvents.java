package com.vincenthuto.hemomancy.common.capability.player.unstained;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncUnstainedProgress;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.GAME)
public class UnstainedProgressEvents {

    @SubscribeEvent
    public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(Hemomancy.rloc("unstained_progress"), new UnstainedProgressProvider());
        }
    }

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

    @SubscribeEvent
    public static void playerDeath(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player original = event.getOriginal();
            Player newPlayer = event.getEntity();
            original.reviveCaps();
            IUnstainedProgress oldProgress = HemoCapabilityAccess.getUnstainedProgress(original)
                    .orElseThrow(IllegalStateException::new);
            IUnstainedProgress newProgress = HemoCapabilityAccess.getUnstainedProgress(newPlayer)
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
            newProgress.setUsedAltarOfCleansing(oldProgress.hasUsedAltarOfCleansing());
            original.invalidateCaps();
        }
    }
}

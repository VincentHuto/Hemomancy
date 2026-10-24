package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.*;
import com.vincenthuto.hemomancy.client.render.entity.misc.*;
import com.vincenthuto.hemomancy.client.render.world.*;
import com.vincenthuto.hemomancy.client.render.world.chamberofwill.VesperFightFloorRenderer;
import com.vincenthuto.hemomancy.client.screen.manips.ClientManipulationCooldowns;
import com.vincenthuto.hemomancy.client.screen.overlay.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/**
 * Clears client state when a player joins or leaves a world and when a client level unloads.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ClientSessionEvents {

    private ClientSessionEvents() {
    }

    @SubscribeEvent
    public static void onClientPlayerLogin(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn event) {
        ClientManipulationCooldowns.clear();
        NaeglerophaeonCaptureOverlay.clear();
        CircusPerceptionOverlay.clear();
        QliphothBloomClientData.clear();
        NpcProgressionMarkerClientState.clear();
    }

    @SubscribeEvent
    public static void onClientPlayerLogout(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        com.vincenthuto.hemomancy.common.item.harbinger.BloodInjectionData.clearClient();
        com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData.clearClient();
        ClientManipulationCooldowns.clear();
        NaeglerophaeonCaptureOverlay.clear();
        // HutosLib now retains read tracker state across disconnect/reload.
        FaneBoundaryClientData.clear();
        ActiveRiteClientData.clear();
        com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState.clear();
        WorkingCraftSwallowRenderer.clear();
        com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteGuidance.clear();
        com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState.clear();
        com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteSkyWound.clear();
        com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeQueue.clear();
        com.vincenthuto.hemomancy.client.sound.RiteSoundBed.stopAll();
        com.vincenthuto.hemomancy.client.render.world.CardinalRiteBoundaryRenderer.clearCaches();
        com.vincenthuto.hemomancy.client.render.world.floor.FloorRiteResponse.clear();
        com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteOfferingMemory.clear();
        ChamberOfWillClientData.clear();
        com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram.EngramClientState.clearAll();
        FungalWhisperVignetteOverlay.clear();
        CardinalRiteFogRenderer.clear();
        VesperFightClientData.clear();
        MycophantFightClientData.clear();
        com.vincenthuto.hemomancy.client.data.MycophantIntroClientState.INSTANCE.start(
                com.vincenthuto.hemomancy.client.data.MycophantIntroClientState.CLEAR, 0);
        VesperFightFloorRenderer.clear();
        ArborOfWillRenderer.clearCaches();
        QliphothBloomRenderer.clearCaches();
        FaneBoundaryRenderer.clearCaches();
        if (BloodVolumeOverlay.instance != null) BloodVolumeOverlay.instance.clearCaches();
        QliphothBloomClientData.clear();
        NpcProgressionMarkerClientState.clear();
        CardinalRiteImpactClientEvents.clear();
        CircusPerceptionOverlay.clear();
        if (SanguineOmenOverlay.instance != null) SanguineOmenOverlay.instance.clear();
        MnemonicBlueprintRenderer.disconnect();
        // Entity-id caches: a reused id in the next world must not pick up this world's state.
        CrimsonFireClientState.clear();
        MonolithicDislocationClientState.clear();
        BloodBindingTendrilClientState.clear();
        LivingTorchClientEvents.clear();
        com.vincenthuto.hemomancy.client.player.BloodVialInjectionClientState.clear();
        com.vincenthuto.hemomancy.client.player.HematicMicroscopeClientState.clearSession();
        com.vincenthuto.hemomancy.client.player.LivingStaffMorphClientState.clear();
        com.vincenthuto.hemomancy.client.render.item.QliphothSeedTendrilEffects.clear();
        com.vincenthuto.hemomancy.client.render.layer.MonolithicDislocationShellLayer.clear();
        BloodProjectileEffects.clear();
        CinderBreathFlames.clear();
        PuppetStringStates.clear();
        com.vincenthuto.hemomancy.client.data.CardinalRiteStaffPlantingClientState.clear();
    }

    @SubscribeEvent
    public static void onClientLevelUnload(net.neoforged.neoforge.event.level.LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof net.minecraft.client.multiplayer.ClientLevel)) return;
        ActiveRiteClientData.clear();
        com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState.clear();
        WorkingCraftSwallowRenderer.clear();
        com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteGuidance.clear();
        com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState.clear();
        com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteSkyWound.clear();
        com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeQueue.clear();
        com.vincenthuto.hemomancy.client.sound.RiteSoundBed.stopAll();
        com.vincenthuto.hemomancy.client.render.world.CardinalRiteBoundaryRenderer.clearCaches();
        com.vincenthuto.hemomancy.client.render.world.floor.FloorRiteResponse.clear();
        com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteOfferingMemory.clear();
        CardinalRiteFogRenderer.clear();
        ChamberOfWillClientData.clear();
        com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram.EngramClientState.clearAll();
        FungalWhisperVignetteOverlay.clear();
    }
}

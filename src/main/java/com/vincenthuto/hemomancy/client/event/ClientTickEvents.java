package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.*;
import com.vincenthuto.hemomancy.client.render.world.*;
import com.vincenthuto.hemomancy.client.screen.overlay.*;
import com.vincenthuto.hemomancy.client.sound.EndgameBossMusicHandler;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/**
 * The per-tick client work, in a fixed order: client state and effects, then the camera, held
 * attacks and key input.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ClientTickEvents {

    private ClientTickEvents() {
    }

    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        ClientInputEvents.handleRadialMenuTick();
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        MnemonicBlueprintRenderer.tick();
        StillArtCooldownOverlay.tick();
        ActiveBloodCraftClientData.tick();
        ActiveBloodStructureFeedClientData.tick();
        ActiveRiteClientData.tick();
        com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState.tick();
        com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteGuidance.tick(Minecraft.getInstance());
        if (Minecraft.getInstance().level != null && !Minecraft.getInstance().isPaused()) {
            com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState.tick(
                    ActiveRiteClientData.getActiveRites(),
                    com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState.ghosts(),
                    center -> com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteFloor.at(
                            Minecraft.getInstance().level, center));
            com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteSkyWound.tick(Minecraft.getInstance());
            com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteBloodRain.tick(Minecraft.getInstance());
        }
        com.vincenthuto.hemomancy.client.sound.RiteSoundBed.tick(Minecraft.getInstance());
        BloodStructureFeedSpiralParticles.tick();
        BloodBallClientData.tick();
        VeinSpiderCourierClientData.tick();
        SanguineMonolithShatterRenderer.tick();
        ClawSlashRenderer.tick();
        MonolithicDislocationClientState.tick();
        CrimsonFireClientState.tick();
        BloodBindingTendrilClientState.tick();
        if (SanguineOmenOverlay.instance != null) {
            SanguineOmenOverlay.instance.tick();
        }
        if (FungalWhisperVignetteOverlay.instance != null) {
            FungalWhisperVignetteOverlay.instance.tick();
        }
        ChamberVisitOverlay.tick();
        CircusPerceptionOverlay.tick();
        if (WillPresenceOverlay.instance != null) {
            WillPresenceOverlay.instance.tick();
        }
        EndgameBossMusicHandler.tick();
        ArmatureCameraEvents.handleArmatureCameraFallback();
        ClientInputEvents.handleLivingBaghnakhAutoAttack();
        ClientInputEvents.handleCommonClientTickInput();
    }
}

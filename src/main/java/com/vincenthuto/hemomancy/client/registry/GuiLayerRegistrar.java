package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.client.screen.overlay.ConcentrationOverlay;
import com.vincenthuto.hemomancy.client.screen.overlay.SporeLoadOverlay;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.event.*;
import com.vincenthuto.hemomancy.client.render.world.ManipulationVisualRenderer;
import com.vincenthuto.hemomancy.client.screen.overlay.*;
import com.vincenthuto.hemomancy.client.sound.AntecedentClientEffects;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class GuiLayerRegistrar {

    private GuiLayerRegistrar() {
    }

    // Overlay
    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        // Vanilla's action bar is one line and runs off the screen; this one wraps long messages.
        event.replaceLayer(VanillaGuiLayers.OVERLAY_MESSAGE, ActionBarOverlay::render);
        // An Apotheos's spore bar sits where the food bar is hidden for them (ApotheosHudEvents).
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, Hemomancy.rloc("spore_load"), (graphics, deltaTracker) ->
                SporeLoadOverlay.render(graphics, graphics.guiWidth(), graphics.guiHeight()));
        // Full-screen effects under the whole HUD, vanilla's camera overlays included.
        event.registerBelowAll(Hemomancy.rloc("screen_effects"), (graphics, deltaTracker) ->
                ScreenOverlayEvents.renderUnderHud(graphics));
        event.registerAboveAll(Hemomancy.rloc("concentration"), (graphics, deltaTracker) ->
                ConcentrationOverlay.render(graphics, graphics.guiWidth(), graphics.guiHeight()));
        event.registerAboveAll(Hemomancy.rloc("ironhearted"), (graphics, deltaTracker) ->
                IronheartedOverlay.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight()));
        event.registerAboveAll(Hemomancy.rloc("blackhearted"), (graphics, deltaTracker) ->
                BlackheartedOverlay.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight()));
        event.registerAboveAll(Hemomancy.rloc("manipulation_charge"), (graphics, deltaTracker) ->
                ManipulationChargeOverlay.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight()));
        event.registerAboveAll(Hemomancy.rloc("arbor_fruit_name"), (graphics, deltaTracker) ->
                ArborFruitHudOverlay.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight()));
        event.registerAboveAll(Hemomancy.rloc("mnemonic_blueprint_progress"), (graphics, deltaTracker) ->
                MnemonicBlueprintProgressOverlay.renderHUD(
                        graphics, graphics.guiWidth(), graphics.guiHeight()));
        event.registerAboveAll(Hemomancy.rloc("chamber_visit"), (graphics, deltaTracker) ->
                ChamberVisitOverlay.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight()));
        event.registerAboveAll(Hemomancy.rloc("mycophant_waking"), (graphics, deltaTracker) ->
                MycophantIntroClientEvents.renderOverlay(graphics, graphics.guiWidth(), graphics.guiHeight(),
                        deltaTracker.getGameTimeDeltaPartialTick(true)));
        event.registerAboveAll(Hemomancy.rloc("cardinal_rite"), (graphics, deltaTracker) -> {
            if (CardinalRiteOverlay.instance != null) {
                float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                CardinalRiteOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
            }
        });
        event.registerAboveAll(Hemomancy.rloc("bloodvolume"), (graphics, deltaTracker) -> {
            if (BloodVolumeOverlay.instance != null) {
                float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                BloodVolumeOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
            }
        });
        event.registerAboveAll(Hemomancy.rloc("harbinger_lodestone"), (graphics, deltaTracker) -> {
            if (HarbingerLodestoneOverlay.instance != null) {
                float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                HarbingerLodestoneOverlay.instance.renderHUD(
                        graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
            }
        });
        event.registerAboveAll(Hemomancy.rloc("still_art_cooldown"), (graphics, deltaTracker) -> {
            if (StillArtCooldownOverlay.instance != null) {
                float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                StillArtCooldownOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
            }
        });
        event.registerAboveAll(Hemomancy.rloc("unstained_gauge"), (graphics, deltaTracker) -> {
            if (UnstainedGaugeOverlay.instance != null) {
                float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                UnstainedGaugeOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
            }
        });
        event.registerAboveAll(Hemomancy.rloc("fungal_whisper_vignette"), (graphics, deltaTracker) -> {
            if (FungalWhisperVignetteOverlay.instance != null) {
                float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                FungalWhisperVignetteOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
            }
        });
        event.registerAboveAll(Hemomancy.rloc("sanguine_omen"), (graphics, deltaTracker) -> {
            if (SanguineOmenOverlay.instance != null) {
                float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                SanguineOmenOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
            }
        });
        event.registerAboveAll(Hemomancy.rloc("will_presence"), (graphics, deltaTracker) -> {
            if (WillPresenceOverlay.instance != null) {
                float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                WillPresenceOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
            }
        });
        event.registerAboveAll(Hemomancy.rloc("cabinet_inspection"), (graphics, deltaTracker) ->
                com.vincenthuto.hemomancy.client.screen.overlay.PhlebotomistsCabinetOverlay.render(graphics));
        event.registerAboveAll(Hemomancy.rloc("hematic_microscope"), (graphics, deltaTracker) ->
                com.vincenthuto.hemomancy.client.screen.overlay.HematicMicroscopeOverlay.render(
                        graphics, deltaTracker.getGameTimeDeltaPartialTick(true)));
        event.registerAboveAll(Hemomancy.rloc("boss_scene"), (graphics, deltaTracker) ->
                BossSceneOverlay.render(graphics, graphics.guiWidth(), graphics.guiHeight(),
                        deltaTracker.getGameTimeDeltaPartialTick(true)));
        // Last, but at the base depth: the layers above stack toward the camera, and these markers stay behind them.
        event.registerAboveAll(Hemomancy.rloc("hud_markers"), (graphics, deltaTracker) -> {
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, -graphics.pose().last().pose().m32());
            EarthenVeinTravelClientEvents.renderTooltip(graphics);
            ManipulationVisualRenderer.hud(graphics);
            AntecedentClientEffects.render(graphics);
            graphics.pose().popPose();
        });
    }
}

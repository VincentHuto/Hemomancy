package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.client.screen.overlay.HudVisibilityRules;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.Minecraft;
import com.vincenthuto.hemomancy.client.screen.overlay.ConcentrationOverlay;
import com.vincenthuto.hemomancy.client.screen.overlay.SporeLoadOverlay;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.SaintMeterLayer;
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

    /** A HUD layer that hides with F1 and for spectators; full-screen effects and the boss scene stay unguarded. */
    private static LayeredDraw.Layer hud(LayeredDraw.Layer layer) {
        return (graphics, deltaTracker) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (HudVisibilityRules.shows(minecraft.options.hideGui,
                    minecraft.player != null && minecraft.player.isSpectator())) {
                layer.render(graphics, deltaTracker);
            }
        };
    }

    // Overlay
    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        // Vanilla's action bar is one line and runs off the screen; this one wraps long messages.
        event.replaceLayer(VanillaGuiLayers.OVERLAY_MESSAGE, hud(ActionBarOverlay::render));
        // The fought Saint's meter, under its bar: just above the boss bars so it reads their painted origins.
        event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, Hemomancy.rloc("saint_meter"), hud(SaintMeterLayer::render));
        // An Apotheos's spore bar sits where the food bar is hidden for them (ApotheosHudEvents).
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, Hemomancy.rloc("spore_load"), hud((graphics, deltaTracker) ->
                SporeLoadOverlay.render(graphics, graphics.guiWidth(), graphics.guiHeight())));
        // Full-screen effects under the whole HUD, vanilla's camera overlays included.
        event.registerBelowAll(Hemomancy.rloc("screen_effects"), (graphics, deltaTracker) ->
                ScreenOverlayEvents.renderUnderHud(graphics));
        event.registerAboveAll(Hemomancy.rloc("concentration"), hud((graphics, deltaTracker) ->
                ConcentrationOverlay.render(graphics, graphics.guiWidth(), graphics.guiHeight())));
        event.registerAboveAll(Hemomancy.rloc("ironhearted"), hud((graphics, deltaTracker) ->
                IronheartedOverlay.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight())));
        event.registerAboveAll(Hemomancy.rloc("blackhearted"), hud((graphics, deltaTracker) ->
                BlackheartedOverlay.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight())));
        event.registerAboveAll(Hemomancy.rloc("manipulation_charge"), hud((graphics, deltaTracker) ->
                ManipulationChargeOverlay.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight())));
        event.registerAboveAll(Hemomancy.rloc("arbor_fruit_name"), hud((graphics, deltaTracker) ->
                ArborFruitHudOverlay.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight())));
        event.registerAboveAll(Hemomancy.rloc("mnemonic_blueprint_progress"), hud((graphics, deltaTracker) ->
                MnemonicBlueprintProgressOverlay.renderHUD(
                        graphics, graphics.guiWidth(), graphics.guiHeight())));
        event.registerAboveAll(Hemomancy.rloc("chamber_visit"), hud((graphics, deltaTracker) ->
                ChamberVisitOverlay.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight())));
        event.registerAboveAll(Hemomancy.rloc("mycophant_waking"), hud((graphics, deltaTracker) ->
                MycophantIntroClientEvents.renderOverlay(graphics, graphics.guiWidth(), graphics.guiHeight(),
                        deltaTracker.getGameTimeDeltaPartialTick(true))));
        event.registerAboveAll(Hemomancy.rloc("cardinal_rite"), hud((graphics, deltaTracker) -> {
            if (CardinalRiteOverlay.instance != null) {
                float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                CardinalRiteOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
            }
        }));
        event.registerAboveAll(Hemomancy.rloc("bloodvolume"), hud((graphics, deltaTracker) -> {
            if (BloodVolumeOverlay.instance != null) {
                float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                BloodVolumeOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
            }
        }));
        event.registerAboveAll(Hemomancy.rloc("harbinger_lodestone"), hud((graphics, deltaTracker) -> {
            if (HarbingerLodestoneOverlay.instance != null) {
                float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                HarbingerLodestoneOverlay.instance.renderHUD(
                        graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
            }
        }));
        event.registerAboveAll(Hemomancy.rloc("still_art_cooldown"), hud((graphics, deltaTracker) -> {
            if (StillArtCooldownOverlay.instance != null) {
                float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                StillArtCooldownOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
            }
        }));
        event.registerAboveAll(Hemomancy.rloc("unstained_gauge"), hud((graphics, deltaTracker) -> {
            if (UnstainedGaugeOverlay.instance != null) {
                float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                UnstainedGaugeOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
            }
        }));
        event.registerAboveAll(Hemomancy.rloc("fungal_whisper_vignette"), hud((graphics, deltaTracker) -> {
            if (FungalWhisperVignetteOverlay.instance != null) {
                float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                FungalWhisperVignetteOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
            }
        }));
        event.registerAboveAll(Hemomancy.rloc("sanguine_omen"), hud((graphics, deltaTracker) -> {
            if (SanguineOmenOverlay.instance != null) {
                float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                SanguineOmenOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
            }
        }));
        event.registerAboveAll(Hemomancy.rloc("will_presence"), hud((graphics, deltaTracker) -> {
            if (WillPresenceOverlay.instance != null) {
                float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                WillPresenceOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);
            }
        }));
        event.registerAboveAll(Hemomancy.rloc("cabinet_inspection"), hud((graphics, deltaTracker) ->
                com.vincenthuto.hemomancy.client.screen.overlay.PhlebotomistsCabinetOverlay.render(graphics)));
        event.registerAboveAll(Hemomancy.rloc("hematic_microscope"), hud((graphics, deltaTracker) ->
                com.vincenthuto.hemomancy.client.screen.overlay.HematicMicroscopeOverlay.render(
                        graphics, deltaTracker.getGameTimeDeltaPartialTick(true))));
        event.registerAboveAll(Hemomancy.rloc("boss_scene"), (graphics, deltaTracker) ->
                BossSceneOverlay.render(graphics, graphics.guiWidth(), graphics.guiHeight(),
                        deltaTracker.getGameTimeDeltaPartialTick(true)));
        // Last, but at the base depth: the layers above stack toward the camera, and these markers stay behind them.
        event.registerAboveAll(Hemomancy.rloc("hud_markers"), hud((graphics, deltaTracker) -> {
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, -graphics.pose().last().pose().m32());
            EarthenVeinTravelClientEvents.renderTooltip(graphics);
            ManipulationVisualRenderer.hud(graphics);
            AntecedentClientEffects.render(graphics);
            graphics.pose().popPose();
        }));
    }
}

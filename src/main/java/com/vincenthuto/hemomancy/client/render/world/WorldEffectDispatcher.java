package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.event.EarthenVeinTravelClientEvents;
import com.vincenthuto.hemomancy.client.event.MycophantIntroClientEvents;
import com.vincenthuto.hemomancy.client.render.entity.npc.*;
import com.vincenthuto.hemomancy.client.render.item.harbinger.crossbar.CrossbarHolderTracker;
import com.vincenthuto.hemomancy.client.render.layer.player.AvatarFirstPersonRenderer;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRenderer;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringScreenPasses;
import com.vincenthuto.hemomancy.client.render.scar.OculifloraRevealRenderer;
import com.vincenthuto.hemomancy.client.render.world.chamberofwill.VesperFightFloorRenderer;
import com.vincenthuto.hemomancy.client.screen.overlay.*;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/**
 * The ordered world-effect dispatcher: every world-space effect draws from this one
 * RenderLevelStageEvent handler, in the order listed for each stage. The Outpouring screen passes are the one
 * exception to that single handler: they run at high priority, ahead of other mods' AFTER_LEVEL drawers.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class WorldEffectDispatcher {

    private WorldEffectDispatcher() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void renderScreenPasses(RenderLevelStageEvent event) {
        OutpouringScreenPasses.render(event);
    }

    @SubscribeEvent
    public static void renderLevelLastEvent(RenderLevelStageEvent event) {
        // Effects that pick their own stages, first and in this order.
        AxonalTravelView.renderWorld(event);
        EarthenVeinTravelClientEvents.renderWorld(event);
        MycophantIntroClientEvents.renderGlimpse(event);
        CrossbarHolderTracker.onStage(event);
        AvatarFirstPersonRenderer.render(event);
        OutpouringRenderer.render(event);
        ManipulationVisualRenderer.render(event);

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            renderBloodMoonSky(event);
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) {
            VesperFightFloorRenderer.renderOpaque(event);
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            // Before entities and translucent blocks, so the echo, threads and water inside draw over the Chamber sky.
            com.vincenthuto.hemomancy.client.render.world.chamberofwill.SomaticLoomDomeRenderer.render(event);
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            MnemonicBlueprintRenderer.render(event);
            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
            // The heart-node spread lies on the ground, so it goes first: the ward and every rite visual draw over it.
            HeartNodeSpreadRenderer.render(event.getPoseStack(), partialTick);
            MausoleumWardRenderer.render(event.getPoseStack(), partialTick);
            CircusPerceptionOverlay.renderWorld(event.getPoseStack(), partialTick);
            VesperFightFloorRenderer.renderFadingPerimeter(event);
            VesperFightFloorRenderer.renderFissures(event.getPoseStack(), partialTick);
            com.vincenthuto.hemomancy.client.render.world.chamberofwill.VesperBurrowFissureRenderer.render(event.getPoseStack(), partialTick);
            CardinalRiteBoundaryRenderer.render(event.getPoseStack(), partialTick);
            WorkingCraftSwallowRenderer.render(event.getPoseStack(), partialTick);
            com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteOfferingRenderer.render(event.getPoseStack(), partialTick);
            com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteOfferingDissolveRenderer.render(event.getPoseStack(), partialTick);
            com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteAnchorJetRenderer.render(event.getPoseStack(), partialTick);
            com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteSocketMarkerRenderer.render(event.getPoseStack(), partialTick);
            com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteCulminationRenderer.render(event.getPoseStack(), partialTick);
            UnstainedRiteBoundaryRenderer.render(event.getPoseStack(), partialTick);
            com.vincenthuto.hemomancy.client.render.world.zone.RiteZoneBoundaryRenderer.render(event.getPoseStack(), partialTick);
            GourdVineRenderer.render(event.getPoseStack(), partialTick);
            BloodStructureFeedWarpRenderer.render(event.getPoseStack(), partialTick);
            FaneBoundaryRenderer.renderWorldMask(event.getPoseStack(), partialTick);
            BloodCraftRingRenderer.render(event.getPoseStack(), partialTick);
            QliphothBloomRenderer.render(event.getPoseStack(), partialTick, event.getFrustum());
            OculifloraRevealRenderer.render(event.getPoseStack(), partialTick);
            BloodBallRenderer.render(event.getPoseStack(), partialTick);
            SanguineMonolithShatterRenderer.render(event.getPoseStack(), partialTick);
            PossessionSceneRenderer.render(event.getPoseStack(), partialTick);
            HeartNodeMomentRenderer.render(event.getPoseStack(), partialTick);
            ClawSlashRenderer.render(event.getPoseStack(), partialTick);
            PuppeteerThreadRenderer.render(event.getPoseStack(), partialTick);
            HematicSutureLinkRenderer.render(event.getPoseStack(), partialTick);
            TendonLineRenderer.render(event.getPoseStack(), partialTick);
            MemoryThreadLineRenderer.render(event.getPoseStack(), partialTick);
            HeartyCompassTendrilRenderer.render(event.getPoseStack(), partialTick);
            VeinSpiderCourierRenderer.render(event.getPoseStack(), partialTick);
            com.vincenthuto.hemomancy.client.render.entity.npc.SuccessionBrazierLabels.renderWorld(event.getPoseStack());
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            // After the clouds, so a cloud bank above never paints over the wound hanging below it.
            com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteSkyWound.render(event.getPoseStack(),
                    event.getPartialTick().getGameTimeDeltaPartialTick(true));
            // The daemon's haze writes no depth, so it comes after every other rite visual and the clouds.
            com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeQueue.render(event.getPoseStack());
        }
    }

    private static void renderBloodMoonSky(RenderLevelStageEvent event) {
        if (!BloodMoonClientState.isActive()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.effects().skyType() != DimensionSpecialEffects.SkyType.NORMAL) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        PoseStack moonPose = new PoseStack();
        moonPose.mulPose(event.getModelViewMatrix());
        moonPose.mulPose(Axis.YP.rotationDegrees(-90.0F));
        moonPose.mulPose(Axis.XP.rotationDegrees(mc.level.getTimeOfDay(partialTick) * 360.0F));
        BloodMoonVeinSkyRenderer.renderInSky(moonPose, mc.level, partialTick);
    }
}

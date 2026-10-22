package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.entity.npc.*;
import com.vincenthuto.hemomancy.client.render.scar.OculifloraRevealRenderer;
import com.vincenthuto.hemomancy.client.render.world.chamberofwill.VesperFightFloorRenderer;
import com.vincenthuto.hemomancy.client.screen.overlay.*;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/**
 * The ordered world-effect dispatcher: every world-space effect draws from this one
 * RenderLevelStageEvent handler, in the order listed for each stage.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class WorldEffectDispatcher {

    private WorldEffectDispatcher() {
    }

    @SubscribeEvent
    public static void renderLevelLastEvent(RenderLevelStageEvent event) {
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

package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.entity.boss.annetta.StainedPriestessModel;
import com.vincenthuto.hemomancy.client.model.item.unstained.AnnettasSanguisLanceaModel;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.StainedLanceEntity;
import com.vincenthuto.hemomancy.client.render.item.harbinger.AnnettasSanguisLanceaItemRenderer;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * The thrown lancea: point first along its flight with a blood streak behind it; planted, it quivers in what it
 * struck, glowing hotter until the thorns burst, then thins and fades as its blood runs home.
 */
public class StainedLanceRenderer extends EntityRenderer<StainedLanceEntity> {
    private final AnnettasSanguisLanceaModel spear;

    public StainedLanceRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.spear = new AnnettasSanguisLanceaModel(context.bakeLayer(AnnettasSanguisLanceaModel.LAYER_LOCATION));
    }

    @Override
    public void render(StainedLanceEntity lance, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light) {
        float time = AnimationTime.age(lance, partialTick);
        float planted = lance.plantedAge(partialTick);
        float yRot = Mth.lerp(partialTick, lance.yRotO, lance.getYRot()) * Mth.DEG_TO_RAD;
        float xRot = Mth.lerp(partialTick, lance.xRotO, lance.getXRot()) * Mth.DEG_TO_RAD;
        float fade = planted < 0 ? 1.0F : 1.0F - Mth.clamp((planted - PriestessCombatRules.ERUPTION_TELL - 10.0F) / 12.0F, 0.0F, 1.0F);
        float tip = StainedPriestessModel.SPEAR_TIP / 16.0F;

        poseStack.pushPose();
        // +Z along the flight, then flip so the drawer's point (-Z) leads, sunk a hand's width into the target.
        poseStack.mulPose(Axis.YP.rotation(yRot));
        poseStack.mulPose(Axis.XP.rotation(-xRot));
        if (planted >= 0 && planted < PriestessCombatRules.ERUPTION_TELL + 4) {
            float quiver = (1.0F - planted / (PriestessCombatRules.ERUPTION_TELL + 4)) * 0.06F;
            poseStack.mulPose(Axis.XP.rotation(Mth.sin(time * 2.6F) * quiver));
            poseStack.mulPose(Axis.YP.rotation(Mth.cos(time * 3.1F) * quiver));
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, 0.0F, tip - 0.3F);
        LanceaDraw.draw(poseStack, buffer, spear, 0xF000F0, OverlayTexture.NO_OVERLAY, 0.7F + 0.3F * fade, fade);
        float heat = planted < 0 ? 0.35F : Mth.clamp(planted / PriestessCombatRules.ERUPTION_TELL, 0.0F, 1.0F) * fade;
        LanceaDraw.charge(poseStack.last(), buffer, heat, time, StainedPriestessModel.SPEAR_TIP);
        poseStack.popPose();

        if (planted < 0) drawTrail(lance, partialTick, poseStack, buffer);
        super.render(lance, yaw, partialTick, poseStack, buffer, light);
    }

    /** The flight streak: recorded positions, newest first, drawn relative to the spear's own render origin. */
    private static void drawTrail(StainedLanceEntity lance, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        Vec3 origin = lance.getPosition(partialTick);
        int n = 0;
        while (n < lance.trail.length && lance.trail[n] != null) n++;
        if (n < 2) return;
        float[] line = new float[(n + 1) * 3];
        float[] radii = new float[n + 1];
        float[] alpha = new float[n + 1];
        for (int i = 0; i <= n; i++) {
            Vec3 at = i == 0 ? origin : lance.trail[i - 1];
            line[i * 3] = (float) (at.x - origin.x) * 16.0F;
            line[i * 3 + 1] = (float) (at.y - origin.y) * 16.0F;
            line[i * 3 + 2] = (float) (at.z - origin.z) * 16.0F;
            float s = i / (float) n;
            radii[i] = 2.2F * (1.0F - s) + 0.3F;
            alpha[i] = 0.45F * (1.0F - s) * (1.0F - s);
        }
        TendrilTube.glow(poseStack.last(), buffer.getBuffer(HLRenderTypeInit.TENDRIL_GLOW), line, radii, 1.0F, 0.9F, 0.08F, 0.12F, alpha);
    }

    @Override
    public ResourceLocation getTextureLocation(StainedLanceEntity entity) {
        return AnnettasSanguisLanceaItemRenderer.texture;
    }
}

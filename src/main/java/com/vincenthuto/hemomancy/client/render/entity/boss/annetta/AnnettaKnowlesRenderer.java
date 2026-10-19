package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.annetta.AnnettaKnowlesModel;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaEncounterRules;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaKnowlesEntity;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaKnowlesEntity.EncounterState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class AnnettaKnowlesRenderer extends MobRenderer<AnnettaKnowlesEntity, AnnettaKnowlesModel> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/boss/annetta/annetta_knowles.png");
    private static final ResourceLocation PURGED_TEXTURE =
            Hemomancy.rloc("textures/entity/boss/annetta/annetta_knowles_purged.png");
    private static final ResourceLocation EYES_TEXTURE =
            Hemomancy.rloc("textures/entity/boss/annetta/annetta_knowles_eyes.png");
    private static final ResourceLocation BLOOD_CREEP =
            Hemomancy.rloc("textures/entity/boss/annetta/annetta_blood_creep.png");

    public AnnettaKnowlesRenderer(Context context) {
        super(context, new AnnettaKnowlesModel(context.bakeLayer(AnnettaKnowlesModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new AnnettaHeldItemLayer(this, context.getItemInHandRenderer()));
        this.addLayer(new GoldEyesLayer(this));
        this.addLayer(new BloodCreepLayer(this));
    }

    static AnnettaLooks.Look lookOf(AnnettaKnowlesEntity entity, float partialTick) {
        float drain = entity.getEncounterState() == EncounterState.PURGING
                ? AnnettaEncounterRules.hairDrain(entity.sceneTime(partialTick)) : 0.0F;
        return AnnettaLooks.look(entity.getEncounterState(), drain);
    }

    @Override
    public void render(AnnettaKnowlesEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        AnnettaLooks.Look look = lookOf(entity, partialTick);
        this.model.prepare(look, AnnettaLooks.tickSwell(entity.getHealth(), entity.getMaxHealth()), !entity.isHeadless());
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AnnettaKnowlesEntity entity) {
        return AnnettaLooks.purged(lookOf(entity, 0.0F)) ? PURGED_TEXTURE : TEXTURE;
    }

    /** A faint catch-light in her gold eyes, a tell rather than a glow, until the Draught takes the colour out. */
    private static final class GoldEyesLayer extends RenderLayer<AnnettaKnowlesEntity, AnnettaKnowlesModel> {
        GoldEyesLayer(RenderLayerParent<AnnettaKnowlesEntity, AnnettaKnowlesModel> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AnnettaKnowlesEntity entity,
                float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
                float headPitch) {
            if (AnnettaLooks.purged(lookOf(entity, partialTick)) || entity.isHeadless()) return;
            this.getParentModel().renderToBuffer(poseStack, buffer.getBuffer(RenderType.eyes(EYES_TEXTURE)),
                    packedLight, OverlayTexture.NO_OVERLAY, 0xFF4A4A4A);
        }
    }

    /** The reanimating pool climbs her fallen body: wet blood laid over her, thickening until she is wholly red. */
    private static final class BloodCreepLayer extends RenderLayer<AnnettaKnowlesEntity, AnnettaKnowlesModel> {
        BloodCreepLayer(RenderLayerParent<AnnettaKnowlesEntity, AnnettaKnowlesModel> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AnnettaKnowlesEntity entity,
                float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
                float headPitch) {
            if (entity.getEncounterState() != EncounterState.BREAKING) return;
            float creep = AnnettaEncounterRules.creep(entity.sceneTime(partialTick));
            if (creep <= 0.0F) return;
            int alpha = (int) (Math.min(1.0F, creep) * 235.0F);
            this.getParentModel().renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityTranslucent(BLOOD_CREEP)),
                    packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), FastColor.ARGB32.color(alpha, 255, 255, 255));
        }
    }
}

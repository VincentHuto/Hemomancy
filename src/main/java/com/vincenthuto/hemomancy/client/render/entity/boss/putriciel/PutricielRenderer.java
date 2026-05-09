package com.vincenthuto.hemomancy.client.render.entity.boss.putriciel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielModel;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PutricielRenderer extends HumanoidMobRenderer<PutricielEntity, PutricielModel<PutricielEntity>> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/boss/putriciel/putriciel.png");

    public PutricielRenderer(Context context) {
        super(context, new PutricielModel<>(context.bakeLayer(PutricielModel.LAYER_LOCATION)), 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(PutricielEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(PutricielEntity entity, PoseStack poseStack, float partialTick) {
        float scale = 1.4F;
        if (entity.isAbsolved()) {
            float intensity = Math.min(1.0F, entity.getAbsolutionCount() / 5.0F);
            scale *= 1.0F + intensity * 0.04F + (float) Math.sin((entity.tickCount + partialTick) * 0.45F) * 0.025F;
        } else if (entity.getVisualState() == PutricielEntity.VISUAL_ROT_NOVA) {
            scale *= 1.0F + (float) Math.sin((entity.tickCount + partialTick) * 0.6F) * 0.04F;
        } else if (entity.getVisualState() == PutricielEntity.VISUAL_ABSOLUTION_HIT) {
            scale *= 0.97F;
        }
        poseStack.scale(scale, scale, scale);
    }
}

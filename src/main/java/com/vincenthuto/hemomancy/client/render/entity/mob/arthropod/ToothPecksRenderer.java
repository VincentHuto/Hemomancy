package com.vincenthuto.hemomancy.client.render.entity.mob.arthropod;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.ToothPecksModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ToothPecksEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ToothPecksRenderer extends MobRenderer<ToothPecksEntity, ToothPecksModel<ToothPecksEntity>> {

    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/tooth_pecks/model_tooth_pecks.png");

    public ToothPecksRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ToothPecksModel<>(ctx.bakeLayer(ToothPecksModel.LAYER_LOCATION)), 0.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(ToothPecksEntity entity) {
        return TEXTURE;
    }

    // Scale up as the creature engorges — fully fed is 70% larger than a fresh one
    @Override
    protected void scale(ToothPecksEntity entity, PoseStack poseStack, float partialTick) {
        float s = 1.0F + entity.getFeedCount() * 0.14F;
        poseStack.scale(s, s, s);
    }
}

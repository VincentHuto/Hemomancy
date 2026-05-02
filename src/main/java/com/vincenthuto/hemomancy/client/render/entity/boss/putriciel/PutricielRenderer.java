package com.vincenthuto.hemomancy.client.render.entity.boss.putriciel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Placeholder renderer for Putriciel (The Rotting Saint).
 * Uses the vanilla humanoid model scaled up to match his 1.0×2.6 hitbox,
 * with a blank texture until artwork is available.
 */
public class PutricielRenderer extends HumanoidMobRenderer<PutricielEntity, HumanoidModel<PutricielEntity>> {

    // TODO: replace with dedicated putriciel texture
    private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/blank.png");

    public PutricielRenderer(Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(PutricielEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(PutricielEntity entity, PoseStack poseStack, float partialTick) {
        // Scale up to approximate his larger hitbox proportions
        poseStack.scale(1.4F, 1.4F, 1.4F);
    }
}

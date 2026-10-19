package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.annetta.StainedPriestessModel;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.StainedPriestessEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class StainedPriestessRenderer extends MobRenderer<StainedPriestessEntity, StainedPriestessModel> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/boss/annetta/stained_priestess.png");
    private static final float SCALE = PriestessTendrilRules.RENDER_SCALE;

    public StainedPriestessRenderer(EntityRendererProvider.Context context) {
        super(context, new StainedPriestessModel(context.bakeLayer(StainedPriestessModel.LAYER_LOCATION)), 0.7F);
        // The lancea is drawn by its own layer at the model's spear bone: both hands are solved onto its shaft.
        this.addLayer(new StainedPriestessSpearLayer(this, context.getModelSet()));
        this.addLayer(new StainedPriestessTendrilLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(StainedPriestessEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(StainedPriestessEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}

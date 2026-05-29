package com.vincenthuto.hemomancy.client.render.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.MycophantModel;
import com.vincenthuto.hemomancy.client.render.layer.mob.endgame.MycophantAwokenMaskLayer;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MycophantRenderer extends MobRenderer<MycophantEntity, MycophantModel> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/boss/endgame/mycophant.png");
    private static final ResourceLocation AWOKEN_TEXTURE =
            Hemomancy.rloc("textures/entity/boss/endgame/mycophant_awoken.png");

    public MycophantRenderer(Context context) {
        super(context, new MycophantModel(context.bakeLayer(MycophantModel.LAYER_LOCATION)), 3.5F);
        this.addLayer(new MycophantAwokenMaskLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(MycophantEntity entity) {
        return entity.getHealth() <= entity.getMaxHealth() * 0.5F ? AWOKEN_TEXTURE : TEXTURE;
    }

    @Override
    protected void scale(MycophantEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(2.0F, 2.0F, 2.0F);
    }
}

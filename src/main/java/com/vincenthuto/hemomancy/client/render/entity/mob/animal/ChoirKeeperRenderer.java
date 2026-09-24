package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.ChoirKeeperModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ChoirKeeperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class ChoirKeeperRenderer extends MobRenderer<ChoirKeeperEntity, ChoirKeeperModel> {
    private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/choir_keeper_java.png");

    public ChoirKeeperRenderer(EntityRendererProvider.Context context) {
        super(context, new ChoirKeeperModel(context.bakeLayer(ChoirKeeperModel.LAYER_LOCATION)), 0.35F);
    }

    @Override
    protected void scale(ChoirKeeperEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.25F, 0.25F, 0.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(ChoirKeeperEntity entity) {
        return TEXTURE;
    }
}

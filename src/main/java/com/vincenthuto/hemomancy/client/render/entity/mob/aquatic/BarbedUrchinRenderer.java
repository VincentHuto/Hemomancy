package com.vincenthuto.hemomancy.client.render.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.BarbedUrchinModel;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.BarbedUrchinEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class BarbedUrchinRenderer extends MobRenderer<BarbedUrchinEntity, BarbedUrchinModel> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/barbed_urchin/model_barbed_urchin.png");

    public BarbedUrchinRenderer(EntityRendererProvider.Context context) {
        super(context, new BarbedUrchinModel(), 0.1F);
    }

    @Override
    public void render(BarbedUrchinEntity entity, float yaw, float partialTick, PoseStack stack,
                       MultiBufferSource buffer, int light) {
        shadowRadius = 0.1F + 0.1F * entity.getVisualPuff(partialTick);
        super.render(entity, yaw, partialTick, stack, buffer, light);
    }

    @Override
    protected void scale(BarbedUrchinEntity entity, PoseStack stack, float partialTick) {
        float scale = 0.43F + 0.095F * entity.getVisualPuff(partialTick);
        stack.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTextureLocation(BarbedUrchinEntity entity) {
        return TEXTURE;
    }
}

package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.ToothPecksModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ToothPecksEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ToothPecksRenderer extends MobRenderer<ToothPecksEntity, ToothPecksModel<ToothPecksEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/tooth_pecks/model_tooth_pecks.png");

    public ToothPecksRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ToothPecksModel<>(ctx.bakeLayer(ToothPecksModel.LAYER_LOCATION)), 0.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(ToothPecksEntity entity) {
        return TEXTURE;
    }
}

package com.vincenthuto.hemomancy.client.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.HarbingerHermitModel;
import com.vincenthuto.hemomancy.common.entity.mob.HarbingerHermitEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HarbingerHermitRenderer extends MobRenderer<HarbingerHermitEntity, HarbingerHermitModel<HarbingerHermitEntity>> {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
            "textures/entity/harbinger_hermit/harbinger_hermit.png");

    public HarbingerHermitRenderer(Context context) {
        super(context, new HarbingerHermitModel<>(context.bakeLayer(HarbingerHermitModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(HarbingerHermitEntity entity) {
        return TEXTURE;
    }
}

package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.HarbingerHermitModel;
import com.vincenthuto.hemomancy.common.entity.npc.HarbingerHermitEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HarbingerHermitRenderer extends MobRenderer<HarbingerHermitEntity, HarbingerHermitModel<HarbingerHermitEntity>> {

    protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/harbinger_hermit/harbinger_hermit.png");

    public HarbingerHermitRenderer(Context context) {
        super(context, new HarbingerHermitModel<>(context.bakeLayer(HarbingerHermitModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(HarbingerHermitEntity entity) {
        return TEXTURE;
    }
}

package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.HarbingerVicarModel;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HarbingerVicarRenderer extends MobRenderer<HarbingerVicarEntity, HarbingerVicarModel<HarbingerVicarEntity>> {

    protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/harbinger_vicar/harbinger_vicar.png");

    public HarbingerVicarRenderer(Context context) {
        super(context, new HarbingerVicarModel<>(context.bakeLayer(HarbingerVicarModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(HarbingerVicarEntity entity) {
        return TEXTURE;
    }
}

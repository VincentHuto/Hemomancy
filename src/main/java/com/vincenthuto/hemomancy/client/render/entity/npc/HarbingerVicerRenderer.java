package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.HarbingerVicerModel;
import com.vincenthuto.hemomancy.common.entity.npc.HarbingerVicerEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HarbingerVicerRenderer extends MobRenderer<HarbingerVicerEntity, HarbingerVicerModel<HarbingerVicerEntity>> {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
            "textures/entity/harbinger_vicer/harbinger_vicer.png");

    public HarbingerVicerRenderer(Context context) {
        super(context, new HarbingerVicerModel<>(context.bakeLayer(HarbingerVicerModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(HarbingerVicerEntity entity) {
        return TEXTURE;
    }
}

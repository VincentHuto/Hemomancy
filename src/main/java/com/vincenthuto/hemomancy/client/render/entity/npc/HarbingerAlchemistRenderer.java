package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.HarbingerAlchemistModel;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HarbingerAlchemistRenderer extends MobRenderer<HarbingerAlchemistEntity, HarbingerAlchemistModel<HarbingerAlchemistEntity>> {

    protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/harbinger_alchemist/harbinger_alchemist.png");

    public HarbingerAlchemistRenderer(Context context) {
        super(context, new HarbingerAlchemistModel<>(context.bakeLayer(HarbingerAlchemistModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(HarbingerAlchemistEntity entity) {
        return TEXTURE;
    }
}

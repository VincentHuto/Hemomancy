package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.UnstainedZealotModel;
import com.vincenthuto.hemomancy.common.entity.npc.UnstainedZealotEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class UnstainedZealotRenderer extends MobRenderer<UnstainedZealotEntity, UnstainedZealotModel<UnstainedZealotEntity>> {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
            "textures/entity/unstained_zealot/unstained_zealot.png");

    public UnstainedZealotRenderer(Context context) {
        super(context, new UnstainedZealotModel<>(context.bakeLayer(UnstainedZealotModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(UnstainedZealotEntity entity) {
        return TEXTURE;
    }
}

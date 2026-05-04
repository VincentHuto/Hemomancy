package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.UnstainedScoutModel;
import com.vincenthuto.hemomancy.common.entity.npc.unstained.UnstainedScoutEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class UnstainedScoutRenderer extends MobRenderer<UnstainedScoutEntity, UnstainedScoutModel<UnstainedScoutEntity>> {

    protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/unstained_scout/unstained_scout.png");

    public UnstainedScoutRenderer(Context context) {
        super(context, new UnstainedScoutModel<>(context.bakeLayer(UnstainedScoutModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(UnstainedScoutEntity entity) {
        return TEXTURE;
    }
}

package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.OsteophageModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.OsteophageEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class OsteophageRenderer extends MobRenderer<OsteophageEntity, OsteophageModel> {
    private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/osteophage.png");
    public OsteophageRenderer(EntityRendererProvider.Context context) {
        super(context, new OsteophageModel(context.bakeLayer(OsteophageModel.LAYER_LOCATION)), .4F);
    }
    @Override public ResourceLocation getTextureLocation(OsteophageEntity bird) { return TEXTURE; }
}

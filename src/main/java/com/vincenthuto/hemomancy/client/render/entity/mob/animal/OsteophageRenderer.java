package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.OsteophageModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.OsteophageEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class OsteophageRenderer extends MobRenderer<OsteophageEntity, OsteophageModel> {
    private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/osteophage.png");
    public OsteophageRenderer(EntityRendererProvider.Context context) {
        super(context, new OsteophageModel(), .4F);
    }
    @Override
    public boolean shouldRender(OsteophageEntity bird, Frustum frustum, double x, double y, double z) {
        return super.shouldRender(bird, frustum, x, y, z)
                || (bird.getFlightPoseAmount(1) > 0 && bird.shouldRender(x, y, z)
                    && frustum.isVisible(bird.getBoundingBoxForCulling().inflate(2.3, 1.2, 2.3)));
    }

    @Override public ResourceLocation getTextureLocation(OsteophageEntity bird) { return TEXTURE; }
}

package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.ChoirKeeperModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ChoirKeeperEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class ChoirKeeperRenderer extends MobRenderer<ChoirKeeperEntity, ChoirKeeperModel> {
    private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/choir_keeper_java.png");

    public ChoirKeeperRenderer(EntityRendererProvider.Context context) {
        super(context, new ChoirKeeperModel(), 0.35F);
    }

    @Override
    public boolean shouldRender(ChoirKeeperEntity entity, Frustum frustum, double x, double y, double z) {
        return super.shouldRender(entity, frustum, x, y, z)
                || (entity.shouldRender(x, y, z)
                    && frustum.isVisible(entity.getBoundingBoxForCulling().inflate(1.6, .85, 1.6)));
    }

    @Override
    public ResourceLocation getTextureLocation(ChoirKeeperEntity entity) {
        return TEXTURE;
    }
}

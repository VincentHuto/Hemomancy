package com.vincenthuto.hemomancy.client.render.entity.boss.endgame;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperTheCrownedRefusalModel;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VesperTheCrownedRefusalRenderer
        extends MobRenderer<VesperTheCrownedRefusalEntity, VesperTheCrownedRefusalModel> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/boss/endgame/vesper_crowned_refusal.png");

    public VesperTheCrownedRefusalRenderer(Context context) {
        super(context, new VesperTheCrownedRefusalModel(context.bakeLayer(VesperTheCrownedRefusalModel.LAYER_LOCATION)), 4.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(VesperTheCrownedRefusalEntity entity) {
        return TEXTURE;
    }
}

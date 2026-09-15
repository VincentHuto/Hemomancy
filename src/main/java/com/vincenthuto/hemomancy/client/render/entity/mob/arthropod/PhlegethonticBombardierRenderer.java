package com.vincenthuto.hemomancy.client.render.entity.mob.arthropod;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.PhlegethonticBombardierModel;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.PhlegethonticBombardier;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class PhlegethonticBombardierRenderer extends MobRenderer<PhlegethonticBombardier, PhlegethonticBombardierModel> {
    private static final ResourceLocation TEXTURE = Hemomancy.rloc(
            "textures/entity/phlegethontic_bombardier/phlegethontic_bombardier.png");
    public PhlegethonticBombardierRenderer(EntityRendererProvider.Context context) {
        super(context, new PhlegethonticBombardierModel(context.bakeLayer(
                PhlegethonticBombardierModel.LAYER_LOCATION)), .55F);
    }
    @Override public ResourceLocation getTextureLocation(PhlegethonticBombardier entity) { return TEXTURE; }
}

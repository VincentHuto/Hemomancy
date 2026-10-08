package com.vincenthuto.hemomancy.client.render.entity.mob.aquatic;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.VampireSquidModel;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.VampireSquidEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VampireSquidRenderer extends MobRenderer<VampireSquidEntity, VampireSquidModel> {
    private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/pelagic/vampire_squid.png");
    public VampireSquidRenderer(EntityRendererProvider.Context context) {
        super(context, new VampireSquidModel(context.bakeLayer(VampireSquidModel.LAYER_LOCATION)), .25F);
    }
    @Override public ResourceLocation getTextureLocation(VampireSquidEntity entity) { return TEXTURE; }
}

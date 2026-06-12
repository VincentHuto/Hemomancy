package com.vincenthuto.hemomancy.client.render.entity.mob.monster;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.ModelBogRevenant;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BogRevenantEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BogRevenantRenderer extends MobRenderer<BogRevenantEntity, ModelBogRevenant> {

    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/bog_revenant/model_bog_revenant.png");

    public BogRevenantRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelBogRevenant(context.bakeLayer(ModelBogRevenant.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(BogRevenantEntity entity) {
        return TEXTURE;
    }
}


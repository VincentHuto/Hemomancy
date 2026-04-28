package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.RougeDrudgeModel;
import com.vincenthuto.hemomancy.client.model.entity.npc.DrudgeModel;
import com.vincenthuto.hemomancy.common.entity.npc.DrudgeEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the {@link DrudgeEntity}.
 * Uses the default {@link DrudgeModel} with a grey texture for the tame state,
 * and the {@link RougeDrudgeModel} with a red texture for the rogue state.
 */
public class DrudgeRenderer extends MobRenderer<DrudgeEntity, DrudgeModel<DrudgeEntity>> {

    private static final ResourceLocation TEXTURE_TAME =
            Hemomancy.rloc("textures/entity/drudge/model_drudge_grey.png");
    private static final ResourceLocation TEXTURE_ROGUE =
            Hemomancy.rloc("textures/entity/drudge/model_drudge_red.png");

    public DrudgeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new DrudgeModel<>(ctx.bakeLayer(DrudgeModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(DrudgeEntity entity) {
        return entity.isRogue() ? TEXTURE_ROGUE : TEXTURE_TAME;
    }
}

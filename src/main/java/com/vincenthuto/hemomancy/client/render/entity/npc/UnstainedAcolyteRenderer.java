package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.UnstainedAcolyteModel;
import com.vincenthuto.hemomancy.common.entity.npc.UnstainedAcolyteEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class UnstainedAcolyteRenderer extends MobRenderer<UnstainedAcolyteEntity, UnstainedAcolyteModel<UnstainedAcolyteEntity>> {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
            "textures/entity/unstained_acolyte/unstained_acolyte.png");

    public UnstainedAcolyteRenderer(Context context) {
        super(context, new UnstainedAcolyteModel<>(context.bakeLayer(UnstainedAcolyteModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(UnstainedAcolyteEntity entity) {
        return TEXTURE;
    }
}

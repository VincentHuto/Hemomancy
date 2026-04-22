package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.UnstainedGuardianModel;
import com.vincenthuto.hemomancy.common.entity.npc.UnstainedGuardianEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class UnstainedGuardianRenderer extends MobRenderer<UnstainedGuardianEntity, UnstainedGuardianModel<UnstainedGuardianEntity>> {

    protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/unstained_guardian/unstained_guardian.png");

    public UnstainedGuardianRenderer(Context context) {
        super(context, new UnstainedGuardianModel<>(context.bakeLayer(UnstainedGuardianModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(UnstainedGuardianEntity entity) {
        return TEXTURE;
    }
}

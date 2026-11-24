package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.SanguineRamModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.SanguineRamEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class SanguineRamRenderer extends MobRenderer<SanguineRamEntity, SanguineRamModel> {
    private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/sanguine_ram.png");

    public SanguineRamRenderer(EntityRendererProvider.Context context) {
        super(context, new SanguineRamModel(), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(SanguineRamEntity entity) {
        return TEXTURE;
    }
}

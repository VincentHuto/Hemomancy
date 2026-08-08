package com.vincenthuto.hemomancy.client.render.entity.boss.endgame;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperTheCrownedRefusalModel;
import com.vincenthuto.hemomancy.client.render.layer.mob.endgame.VesperMountAbsorptionLayer;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseTransitionRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class VesperTheCrownedRefusalRenderer
        extends MobRenderer<VesperTheCrownedRefusalEntity, VesperTheCrownedRefusalModel> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/boss/endgame/vesper_crowned_refusal.png");

    public VesperTheCrownedRefusalRenderer(Context context) {
        super(context, new VesperTheCrownedRefusalModel(context.bakeLayer(VesperTheCrownedRefusalModel.LAYER_LOCATION)), 4.5F);
        addLayer(new VesperMountAbsorptionLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(VesperTheCrownedRefusalEntity entity) {
        return TEXTURE;
    }

    @Nullable
    @Override
    protected RenderType getRenderType(VesperTheCrownedRefusalEntity entity, boolean bodyVisible,
            boolean translucent, boolean glowing) {
        if (bodyVisible && VesperPhaseTransitionRules.isAbsorbing(entity.getTransitionTick())) {
            return RenderType.entityTranslucent(TEXTURE);
        }
        return super.getRenderType(entity, bodyVisible, translucent, glowing);
    }
}

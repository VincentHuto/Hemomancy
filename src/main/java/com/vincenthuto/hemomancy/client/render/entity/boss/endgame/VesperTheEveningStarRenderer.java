package com.vincenthuto.hemomancy.client.render.entity.boss.endgame;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperTheEveningStarModel;
import com.vincenthuto.hemomancy.client.render.layer.mob.endgame.VesperEveningStarLinesLayer;
import com.vincenthuto.hemomancy.client.render.layer.mob.endgame.VesperLivingWeaponLayer;
import com.vincenthuto.hemomancy.client.render.layer.mob.endgame.VesperTendencySigilLayer;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VesperTheEveningStarRenderer
        extends MobRenderer<VesperTheEveningStarEntity, VesperTheEveningStarModel> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/boss/endgame/vesper_evening_star.png");

    public VesperTheEveningStarRenderer(Context context) {
        super(context, new VesperTheEveningStarModel(context.bakeLayer(VesperTheEveningStarModel.LAYER_LOCATION)), 1.5F);
        this.addLayer(new VesperEveningStarLinesLayer(this));
		this.addLayer(new VesperTendencySigilLayer(this));
		this.addLayer(new VesperLivingWeaponLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(VesperTheEveningStarEntity entity) {
        return TEXTURE;
    }
}

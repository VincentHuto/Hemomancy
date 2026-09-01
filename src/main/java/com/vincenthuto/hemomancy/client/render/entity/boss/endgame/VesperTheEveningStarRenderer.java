package com.vincenthuto.hemomancy.client.render.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperTheEveningStarModel;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.layer.mob.endgame.*;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperEveningStarPresentationRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseTransitionRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class VesperTheEveningStarRenderer
        extends MobRenderer<VesperTheEveningStarEntity, VesperTheEveningStarModel> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/boss/endgame/vesper_evening_star.png");

    public VesperTheEveningStarRenderer(Context context) {
        super(context, new VesperTheEveningStarModel(context.bakeLayer(VesperTheEveningStarModel.LAYER_LOCATION)), 1.5F);
		this.addLayer(new VesperAwakeningGlowLayer(this));
        this.addLayer(new VesperEveningStarLinesLayer(this));
		this.addLayer(new VesperShamedDissolutionLayer(this));
		this.addLayer(new VesperTendencySigilLayer(this));
		this.addLayer(new VesperLivingWeaponLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(VesperTheEveningStarEntity entity) {
        return TEXTURE;
    }

	@Override
	protected void scale(VesperTheEveningStarEntity entity, PoseStack poseStack, float partialTick) {
		float scale = VesperPhaseTransitionRules.awakeningScale(entity.getAwakeningFrame(partialTick));
		if (entity.isAwaitingAbsorption()) {
			float absorption = entity.getDefeatAbsorptionProgress();
			poseStack.translate(0.0D, VesperEveningStarPresentationRules.absorptionLowering(absorption), 0.0D);
			scale *= VesperEveningStarPresentationRules.absorptionScale(absorption)
					* VesperEveningStarPresentationRules.finalCollapseScale(entity.getFinalCollapseTick());
		}
		poseStack.scale(scale, scale, scale);
	}

	@Nullable
	@Override
	protected RenderType getRenderType(VesperTheEveningStarEntity entity, boolean bodyVisible,
			boolean translucent, boolean glowing) {
		if (bodyVisible && entity.isAwaitingAbsorption() && entity.getDefeatAbsorptionProgress() > 0.0F) {
			float dissolve = VesperEveningStarPresentationRules.absorptionDissolve(
					entity.getDefeatAbsorptionProgress());
			return HemoRenderTypes.hermitFarewellDissolve(TEXTURE,
					entity.tickCount + dissolve, dissolve, entity.getId() * 0.173F);
		}
		return super.getRenderType(entity, bodyVisible, translucent, glowing);
	}
}

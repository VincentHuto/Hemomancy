package com.vincenthuto.hemomancy.client.render.entity.boss;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.HollowVesselModel;
import com.vincenthuto.hemomancy.client.render.layer.mob.HemorathOverloadLayer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HollowVesselEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the Hollow Vessel (Saint Hemorath).
 * Uses the dedicated {@link HollowVesselModel} — an emaciated, elongated
 * humanoid distinct from the standard Harbinger Vicar silhouette.
 */
public class HollowVesselRenderer extends MobRenderer<HollowVesselEntity, HollowVesselModel<HollowVesselEntity>> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/hollow_vessel/hollow_vessel.png");

	public HollowVesselRenderer(Context context) {
		super(context, new HollowVesselModel<>(context.bakeLayer(HollowVesselModel.LAYER_LOCATION)), 0.8F);
		this.addLayer(new HemorathOverloadLayer<>(this));
	}

	@Override
	public ResourceLocation getTextureLocation(HollowVesselEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(HollowVesselEntity entity, PoseStack poseStack, float partialTick) {
		float base = entity.isInPhase2() ? 1.12F : 1.06F;
		if (entity.getVisualState() == HollowVesselEntity.VISUAL_OVERLOAD) {
			float pulse = 1.0F + (float) Math.sin((entity.tickCount + partialTick) * 0.6F) * 0.08F;
			base *= pulse;
		} else if (entity.isCollapseCharging()) {
			float pulse = 1.0F + (float) Math.sin((entity.tickCount + partialTick) * 0.35F) * 0.035F;
			base *= pulse;
		}
		poseStack.scale(base, base, base);
	}
}

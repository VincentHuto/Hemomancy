package com.vincenthuto.hemomancy.client.render.entity.boss.hemorath;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathModel;
import com.vincenthuto.hemomancy.client.render.layer.mob.HemorathOverloadLayer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the Hollow Vessel (Saint Hemorath).
 * Uses the dedicated {@link HemorathModel} — an emaciated, elongated
 * humanoid distinct from the standard Harbinger Vicar silhouette.
 */
public class HemorathRenderer extends MobRenderer<HemorathEntity, HemorathModel<HemorathEntity>> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/boss/hemorath/hemorath.png");

	public HemorathRenderer(Context context) {
		super(context, new HemorathModel<>(context.bakeLayer(HemorathModel.LAYER_LOCATION)), 1.5F);
		this.addLayer(new HemorathOverloadLayer<>(this));
	}

	@Override
	public ResourceLocation getTextureLocation(HemorathEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(HemorathEntity entity, PoseStack poseStack, float partialTick) {
		float base = entity.isInPhase2() ? 2.12F : 2.06F;
		if (entity.getVisualState() == HemorathEntity.VISUAL_OVERLOAD) {
			float pulse = 1.0F + (float) Math.sin((entity.tickCount + partialTick) * 0.6F) * 0.08F;
			base *= pulse;
		} else if (entity.isCollapseCharging()) {
			float pulse = 1.0F + (float) Math.sin((entity.tickCount + partialTick) * 0.35F) * 0.035F;
			base *= pulse;
		}
		poseStack.scale(base, base, base);
	}
}

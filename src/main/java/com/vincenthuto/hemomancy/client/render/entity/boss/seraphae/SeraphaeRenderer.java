package com.vincenthuto.hemomancy.client.render.entity.boss.seraphae;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeModel;
import com.vincenthuto.hemomancy.client.render.layer.mob.SeraphaeWingGlowLayer;
import com.vincenthuto.hemomancy.common.entity.boss.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.seraphae.SeraphaeState;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Saint Seraphae — The Bound Radiance.
 * Applies visual scaling and translucency based on the boss's state.
 */
public class SeraphaeRenderer extends MobRenderer<SeraphaeEntity, SeraphaeModel<SeraphaeEntity>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/seraphae/seraphae.png");

	public SeraphaeRenderer(Context context) {
		super(context, new SeraphaeModel<>(context.bakeLayer(SeraphaeModel.LAYER_LOCATION)), 0.9F);
		this.addLayer(new SeraphaeWingGlowLayer<>(this));
	}

	@Override
	public ResourceLocation getTextureLocation(SeraphaeEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(SeraphaeEntity entity, PoseStack poseStack, float partialTick) {
		// Slightly larger than standard humanoid to feel imposing
		poseStack.scale(1.15F, 1.15F, 1.15F);

		// Pulsating scale during FRACTURING to show instability
		if (entity.getSeraphaeState() == SeraphaeState.FRACTURING) {
			float pulse = 1.0F + (float) Math.sin(entity.tickCount * 0.3 + partialTick * 0.3) * 0.05F;
			poseStack.scale(pulse, pulse, pulse);
		}
	}

	@Override
	public void render(SeraphaeEntity entity, float entityYaw, float partialTicks,
					   PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		// Skip rendering when fully dispersed (invisible is set server-side,
		// but we reinforce it client-side for visual clarity)
		if (entity.getSeraphaeState() == SeraphaeState.DISPERSED) {
			return;
		}
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
	}
}

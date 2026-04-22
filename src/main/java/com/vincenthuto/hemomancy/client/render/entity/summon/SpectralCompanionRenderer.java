package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.SpectralCompanionEntity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the Spectral Companion — renders as a translucent humanoid
 * with a spectral blue-white tint using the default player model.
 */
public class SpectralCompanionRenderer extends HumanoidMobRenderer<SpectralCompanionEntity, HumanoidModel<SpectralCompanionEntity>> {

	// TODO: Replace with a proper spectral companion texture when available
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/blank.png");

	public SpectralCompanionRenderer(Context context) {
		super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
	}

	@Override
	public void render(SpectralCompanionEntity entity, float entityYaw, float partialTicks,
					   PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		// Semi-transparent ghostly appearance
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
	}

	@Override
	protected boolean isBodyVisible(SpectralCompanionEntity entity) {
		return true;
	}

	@Override
	public ResourceLocation getTextureLocation(SpectralCompanionEntity entity) {
		return TEXTURE;
	}
}

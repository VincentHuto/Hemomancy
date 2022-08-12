package com.vincenthuto.hemomancy.render.entity.blood;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.blood.EntityBloodShot;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class BloodShotRenderer extends EntityRenderer<EntityBloodShot> {
	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/block/end_portal.png");

	public BloodShotRenderer(Context renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public void render(EntityBloodShot entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int packedLightIn) {
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getTextureLocation(EntityBloodShot entity) {
		return TEXTURE;
	}
}
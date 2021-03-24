package com.huto.hemomancy.render.entity.projectile;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.projectile.EntityBloodShot;
import com.huto.hemomancy.model.entity.armor.ModelDarkArrowHorizontal;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class RenderBloodShot<T extends EntityBloodShot> extends EntityRenderer<EntityBloodShot> {
	ModelDarkArrowHorizontal model = new ModelDarkArrowHorizontal();
	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/block/end_portal.png");

	public RenderBloodShot(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}

	public void render(EntityBloodShot entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int packedLightIn) {
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getEntityTexture(EntityBloodShot entity) {
		return TEXTURE;
	}
}

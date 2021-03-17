package com.huto.hemomancy.render.entity;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.iron.EntityIronPillar;
import com.huto.hemomancy.model.entity.ModelIronPillar;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderIronPillar extends MobRenderer<EntityIronPillar, ModelIronPillar> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/iron_pillar/model_iron_pillar.png");

	public RenderIronPillar(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new ModelIronPillar(), 0.1f);

	}

	@Override
	public void render(EntityIronPillar entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int packedLightIn) {
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);

	}

	// Growth Scaling
	@Override
	protected void preRenderCallback(EntityIronPillar entitylivingbaseIn, MatrixStack matrixStackIn,
			float partialTickTime) {
		super.preRenderCallback(entitylivingbaseIn, matrixStackIn, partialTickTime);
		float f = 0.0F;
		int i = entitylivingbaseIn.ticksExisted * 2;
		if (i > 0) {
			f = ((float) i - partialTickTime) / 30.0F * 0.5F;
		}

		if (f > 2.3) {
			f = 2.3f;
		}
		if (entitylivingbaseIn.deathTicks == 1) {
			matrixStackIn.scale(1, f, 1);
		}

		if (entitylivingbaseIn.deathTicks > 0) {
			float d = entitylivingbaseIn.deathTicks * 2;
			matrixStackIn.scale(1, d, 1);

		}
	}

	@Override
	public ResourceLocation getEntityTexture(EntityIronPillar entity) {
		return TEXTURE;

	}

}

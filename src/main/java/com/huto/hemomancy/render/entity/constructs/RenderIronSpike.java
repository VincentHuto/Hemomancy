package com.huto.hemomancy.render.entity.constructs;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.blood.iron.EntityIronSpike;
import com.huto.hemomancy.model.entity.ModelIronSpike;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderIronSpike extends MobRenderer<EntityIronSpike, ModelIronSpike> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/iron_spike/model_iron_spike.png");

	public RenderIronSpike(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new ModelIronSpike(), 0.1f);

	}

	@Override
	public void render(EntityIronSpike entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int packedLightIn) {
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);

	}

	// Growth Scaling
	@Override
	protected void preRenderCallback(EntityIronSpike entitylivingbaseIn, MatrixStack matrixStackIn,
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
			matrixStackIn.scale(1 * (f * 0.8f), 1 * (f * 0.8f), 1 * (f * 0.8f));
		}

		if (entitylivingbaseIn.deathTicks > 0) {
			float d = entitylivingbaseIn.deathTicks / 2;
			matrixStackIn.scale(d, d, d);

		}
	}

	@Override
	public ResourceLocation getEntityTexture(EntityIronSpike entity) {
		return TEXTURE;

	}

}

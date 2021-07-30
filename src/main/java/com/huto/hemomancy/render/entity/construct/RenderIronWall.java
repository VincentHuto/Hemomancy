package com.huto.hemomancy.render.entity.construct;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.blood.iron.EntityIronWall;
import com.huto.hemomancy.model.entity.ModelIronWall;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import ResourceLocation;

public class RenderIronWall extends MobRenderer<EntityIronWall, ModelIronWall> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/iron_wall/model_iron_wall.png");

	public RenderIronWall(EntityRenderDispatcher renderManagerIn) {
		super(renderManagerIn, new ModelIronWall(), 0f);

	}

	@Override
	public void render(EntityIronWall entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int packedLightIn) {
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);

	}

	// Growth Scaling
	@Override
	protected void preRenderCallback(EntityIronWall entitylivingbaseIn, PoseStack matrixStackIn,
			float partialTickTime) {
		super.preRenderCallback(entitylivingbaseIn, matrixStackIn, partialTickTime);
		float f = 0.0F;
		int i = entitylivingbaseIn.ticksExisted * 2;
		if (i > 0) {
			f = ((float) i - partialTickTime) / 30.0F * 0.5F;
		}

		if (f > 0.75) {
			f = 0.75f;
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
	public ResourceLocation getEntityTexture(EntityIronWall entity) {
		return TEXTURE;

	}

}

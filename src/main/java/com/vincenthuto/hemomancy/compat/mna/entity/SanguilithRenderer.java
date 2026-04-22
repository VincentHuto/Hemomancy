package com.vincenthuto.hemomancy.compat.mna.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SanguilithRenderer extends MobRenderer<SanguilithEntity, SanguilithModel<SanguilithEntity>> {
	private static final ResourceLocation texture = Hemomancy.rloc("textures/entity/iron_pillar/model_iron_pillar.png");

	public SanguilithRenderer(EntityRendererProvider.Context p_174447_) {
		super(p_174447_, new SanguilithModel<SanguilithEntity>(p_174447_.bakeLayer(SanguilithModel.sanguilith)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(SanguilithEntity p_114482_) {
		return texture;
	}

	@Override
	protected void scale(SanguilithEntity entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime) {
		super.scale(entitylivingbaseIn, matrixStackIn, partialTickTime);
		float f = 0.0F;
		int i = entitylivingbaseIn.tickCount;
		if (i > 0) {
			f = (i - partialTickTime) / 30.0F * 0.5F;
		}

		if (f > 0.6) {
			f = 0.6f;
		}
		if (entitylivingbaseIn.deathTicks == 1) {
			matrixStackIn.scale(1, f, 1);
		}

		if (entitylivingbaseIn.deathTicks > 0) {
			float d = entitylivingbaseIn.deathTicks * 2;
			matrixStackIn.scale(1, d, 1);

		}
	}
}
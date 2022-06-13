package com.vincenthuto.hemomancy.render.entity.blood.iron;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.blood.iron.EntityIronPillar;
import com.vincenthuto.hemomancy.model.entity.ModelIronPillar;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderIronPillar extends MobRenderer<EntityIronPillar, ModelIronPillar<EntityIronPillar>> {
	private static final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/iron_pillar/model_iron_pillar.png");

	public RenderIronPillar(EntityRendererProvider.Context p_174447_) {
		super(p_174447_, new ModelIronPillar<EntityIronPillar>(p_174447_.bakeLayer(ModelIronPillar.iron_pillar)), 0.5F);

	}

	@Override
	protected void scale(EntityIronPillar entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime) {
		super.scale(entitylivingbaseIn, matrixStackIn, partialTickTime);
		float f = 0.0F;
		int i = entitylivingbaseIn.tickCount;
		if (i > 0) {
			f = (i - partialTickTime) / 30.0F * 0.5F;
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
	public ResourceLocation getTextureLocation(EntityIronPillar p_114482_) {
		return texture;
	}
}
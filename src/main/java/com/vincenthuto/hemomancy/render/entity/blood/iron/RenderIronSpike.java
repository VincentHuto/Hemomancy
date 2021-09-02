package com.vincenthuto.hemomancy.render.entity.blood.iron;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.blood.iron.EntityIronSpike;
import com.vincenthuto.hemomancy.init.ModelLayersInit;
import com.vincenthuto.hemomancy.model.entity.ModelIronSpike;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderIronSpike extends MobRenderer<EntityIronSpike, ModelIronSpike<EntityIronSpike>> {
	private static final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/iron_spike/model_iron_spike.png");

	public RenderIronSpike(EntityRendererProvider.Context p_174447_) {
		super(p_174447_, new ModelIronSpike<EntityIronSpike>(p_174447_.bakeLayer(ModelLayersInit.iron_spike)), 0.5F);

	}
	
	
	@Override
	protected void scale(EntityIronSpike entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime) {
		super.scale(entitylivingbaseIn, matrixStackIn, partialTickTime);
		float f = 0.0F;
		int i = entitylivingbaseIn.tickCount * 2;
		if (i > 0) {
			f = (i - partialTickTime) / 30.0F * 0.5F;
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
	public ResourceLocation getTextureLocation(EntityIronSpike p_114482_) {
		return texture;
	}
}
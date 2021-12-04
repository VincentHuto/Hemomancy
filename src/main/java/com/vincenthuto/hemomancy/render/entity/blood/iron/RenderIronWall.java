package com.vincenthuto.hemomancy.render.entity.blood.iron;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.blood.iron.EntityIronWall;
import com.vincenthuto.hemomancy.init.ModelLayersInit;
import com.vincenthuto.hemomancy.model.entity.ModelIronWall;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderIronWall extends MobRenderer<EntityIronWall, ModelIronWall<EntityIronWall>> {
	private static final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/iron_wall/model_iron_wall.png");

	public RenderIronWall(EntityRendererProvider.Context p_174447_) {
		super(p_174447_, new ModelIronWall<EntityIronWall>(p_174447_.bakeLayer(ModelLayersInit.iron_wall)), 0.5F);

	}
	
	
	@Override
	protected void scale(EntityIronWall entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime) {
		super.scale(entitylivingbaseIn, matrixStackIn, partialTickTime);
		float f = 0.0F;
		int i = entitylivingbaseIn.tickCount * 2;
		if (i > 0) {
			f = (i - partialTickTime) / 30.0F * 0.5F;
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
	public ResourceLocation getTextureLocation(EntityIronWall p_114482_) {
		return texture;
	}
}
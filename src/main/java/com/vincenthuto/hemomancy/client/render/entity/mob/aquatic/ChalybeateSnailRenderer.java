package com.vincenthuto.hemomancy.client.render.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.ChalybeateSnailModel;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.ChalybeateSnailEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ChalybeateSnailRenderer extends MobRenderer<ChalybeateSnailEntity, ChalybeateSnailModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/chalybeate_snail/model_chalybeate_snail.png");

	public ChalybeateSnailRenderer(Context context) {
		super(context, new ChalybeateSnailModel(context.bakeLayer(ChalybeateSnailModel.LAYER_LOCATION)), 0.35F);
	}

	@Override
	protected void scale(ChalybeateSnailEntity entity, PoseStack poseStack, float partialTickTime) {
		poseStack.scale(0.92F, 0.92F, 0.92F);
	}

	@Override
	public ResourceLocation getTextureLocation(ChalybeateSnailEntity entity) {
		return TEXTURE;
	}
}

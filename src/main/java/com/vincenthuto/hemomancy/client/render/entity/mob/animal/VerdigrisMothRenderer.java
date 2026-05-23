package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.VerdigrisMothModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VerdigrisMothEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class VerdigrisMothRenderer extends MobRenderer<VerdigrisMothEntity, VerdigrisMothModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/verdigris_moth/model_verdigris_moth.png");

	public VerdigrisMothRenderer(EntityRendererProvider.Context context) {
		super(context, new VerdigrisMothModel(context.bakeLayer(VerdigrisMothModel.LAYER_LOCATION)), 0.18F);
	}

	@Override
	public ResourceLocation getTextureLocation(VerdigrisMothEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(VerdigrisMothEntity entity, PoseStack poseStack, float partialTick) {
		poseStack.scale(0.72F, 0.72F, 0.72F);
	}

	@Nullable
	@Override
	protected RenderType getRenderType(VerdigrisMothEntity entity, boolean bodyVisible,
			boolean translucent, boolean glowing) {
		return RenderType.entityTranslucent(this.getTextureLocation(entity));
	}

	@Override
	protected int getBlockLightLevel(VerdigrisMothEntity entity, BlockPos pos) {
		return Math.max(8, super.getBlockLightLevel(entity, pos));
	}
}

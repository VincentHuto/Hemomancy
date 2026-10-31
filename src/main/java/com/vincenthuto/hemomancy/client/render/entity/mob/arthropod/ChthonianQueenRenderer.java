package com.vincenthuto.hemomancy.client.render.entity.mob.arthropod;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.entity.mob.WallClimbRendering;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.ChthonianQueenModel;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.ChthonianQueenEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ChthonianQueenRenderer extends MobRenderer<ChthonianQueenEntity, ChthonianQueenModel<ChthonianQueenEntity>> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/chthonian_queen/model_chthonian_queen.png");

	public ChthonianQueenRenderer(Context renderManagerIn) {
		super(renderManagerIn, new ChthonianQueenModel<ChthonianQueenEntity>(renderManagerIn.bakeLayer(ChthonianQueenModel.LAYER_LOCATION)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(ChthonianQueenEntity entity) {
		return TEXTURE;

	}

	@Override
	protected void setupRotations(ChthonianQueenEntity entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick,
			float scale) {
		super.setupRotations(entity, poseStack, bob, WallClimbRendering.bodyYaw(entity.climbPose(), yBodyRot, partialTick),
				partialTick, scale);
		WallClimbRendering.tilt(entity.climbPose(), poseStack, partialTick, 0.45F, 0.5F);
	}

}

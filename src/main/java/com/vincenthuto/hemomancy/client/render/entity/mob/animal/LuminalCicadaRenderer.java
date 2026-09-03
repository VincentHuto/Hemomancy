package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.LuminalCicadaModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.LuminalCicadaEntity;
import com.vincenthuto.hemomancy.common.entity.mob.animal.LuminalCicadaRules;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public final class LuminalCicadaRenderer extends MobRenderer<LuminalCicadaEntity, LuminalCicadaModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/luminal_cicada/luminal_cicada.png");

	public LuminalCicadaRenderer(EntityRendererProvider.Context context) {
		super(context, new LuminalCicadaModel(context.bakeLayer(LuminalCicadaModel.LAYER_LOCATION)), 0.18F);
	}

	@Override
	public ResourceLocation getTextureLocation(LuminalCicadaEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void setupRotations(LuminalCicadaEntity entity, PoseStack poseStack, float bob,
			float bodyYaw, float partialTick, float scale) {
		Direction face = entity.getClingFace();
		super.setupRotations(entity, poseStack, bob,
				face == null ? bodyYaw : LuminalCicadaRules.clingBodyYaw(face), partialTick, scale);
		if (face != null) poseStack.mulPose(Axis.XP.rotationDegrees(LuminalCicadaRules.clingTiltDegrees(face)));
	}

	@Override
	protected int getBlockLightLevel(LuminalCicadaEntity entity, BlockPos pos) {
		return entity.isFlashing() ? 15 : Math.max(7, super.getBlockLightLevel(entity, pos));
	}
}

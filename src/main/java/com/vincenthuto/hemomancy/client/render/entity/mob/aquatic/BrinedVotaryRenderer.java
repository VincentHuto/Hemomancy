package com.vincenthuto.hemomancy.client.render.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.BrinedVotaryModel;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.BrinedVotaryEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws the Brined Votary from its generated model (tools/model_export/brined_votary.py): the body, then a full-bright
 * pass for the light left in its sunken eyes and its votive vial. Swimming toward a target, it is laid along the
 * stroke.
 */
public class BrinedVotaryRenderer extends MobRenderer<BrinedVotaryEntity, BrinedVotaryModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/brined_votary/brined_votary.png");
	private static final RenderType GLOW = RenderType.eyes(Hemomancy.rloc(
			"textures/entity/brined_votary/brined_votary_glow.png"));
	private static final float SWIM_TILT = 70.0F;

	public BrinedVotaryRenderer(Context context) {
		super(context, new BrinedVotaryModel(context.bakeLayer(BrinedVotaryModel.LAYER_LOCATION)), 0.45F);
		this.addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return GLOW;
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(BrinedVotaryEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void setupRotations(BrinedVotaryEntity entity, PoseStack poseStack, float bob, float yBodyRot,
			float partialTick, float scale) {
		super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
		if (entity.isSwimming()) {
			poseStack.translate(0.0F, 1.0F, 0.0F);
			poseStack.mulPose(Axis.XP.rotationDegrees(-SWIM_TILT));
			poseStack.translate(0.0F, -1.0F, 0.0F);
		}
	}
}

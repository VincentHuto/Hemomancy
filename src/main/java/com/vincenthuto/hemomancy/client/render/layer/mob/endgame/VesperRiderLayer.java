package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperMountModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperTheCrownedRefusalModel;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/** Seats Vesper on the mount: the rider model follows the mount's seat as it bobs, rears and banks. */
public final class VesperRiderLayer extends RenderLayer<VesperTheCrownedRefusalEntity, VesperMountModel> {
	public static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/boss/endgame/vesper_crowned_refusal.png");
	/** The mount seat's rest pivot, in model units (see tools/model_export/vesper_mount.py). */
	private static final float SEAT_X = 0.0F;
	private static final float SEAT_Y = -35.0F;
	private static final float SEAT_Z = -8.0F;
	/** Settles the Blockbench rider's hips (authored at y -40.6) onto the dais seat's surface. */
	private static final float RIDER_DROP = 1.6F;
	private static final float RIDER_SHIFT_Z = -0.5F;

	private final VesperTheCrownedRefusalModel rider;

	public VesperRiderLayer(RenderLayerParent<VesperTheCrownedRefusalEntity, VesperMountModel> parent,
			EntityModelSet models) {
		super(parent);
		this.rider = new VesperTheCrownedRefusalModel(models.bakeLayer(VesperTheCrownedRefusalModel.LAYER_LOCATION));
	}

	public VesperTheCrownedRefusalModel rider() {
		return rider;
	}

	/** Moves the pose stack from the mount's model space into the rider model's space. */
	public void applyRiderTransform(PoseStack poseStack) {
		getParentModel().translateToSeat(poseStack);
		poseStack.translate(-SEAT_X / 16.0F, -SEAT_Y / 16.0F, -SEAT_Z / 16.0F);
		poseStack.translate(0.0F, RIDER_DROP / 16.0F, RIDER_SHIFT_Z / 16.0F);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			VesperTheCrownedRefusalEntity entity, float limbSwing, float limbSwingAmount,
			float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.isInvisible()) return;
		rider.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
		rider.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		poseStack.pushPose();
		applyRiderTransform(poseStack);
		rider.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight,
				LivingEntityRenderer.getOverlayCoords(entity, 0.0F), 0xFFFFFFFF);
		poseStack.popPose();
	}
}

package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperMountModel;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Renders the Crowned Refusal's Living Staff and puppeteering crossbar. */
public final class VesperCrownedWeaponLayer
		extends RenderLayer<VesperTheCrownedRefusalEntity, VesperMountModel> {
	private static final float STAFF_SCALE = 1.35F;
	private static final float CROSSBAR_SCALE = 1.7F;
	private final ItemStack staff = new ItemStack(ItemInit.living_staff.get());
	private final ItemStack crossbar = new ItemStack(ItemInit.marionette_crossbar.get());

	private final VesperRiderLayer rider;

	public VesperCrownedWeaponLayer(
			RenderLayerParent<VesperTheCrownedRefusalEntity, VesperMountModel> parent, VesperRiderLayer rider) {
		super(parent);
		this.rider = rider;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			VesperTheCrownedRefusalEntity entity, float limbSwing, float limbSwingAmount,
			float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.isInvisible()) return;

		poseStack.pushPose();
		rider.applyRiderTransform(poseStack);
		rider.rider().translateToRiderWeapon(poseStack);
		poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(
				VesperWeaponGripRules.yawDegrees(EnumBloodTendency.FERRIC, false)));
		poseStack.scale(STAFF_SCALE, STAFF_SCALE, STAFF_SCALE);
		Minecraft.getInstance().getItemRenderer().renderStatic(staff,
				ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, OverlayTexture.NO_OVERLAY,
				poseStack, buffer, entity.level(), entity.getId());
		poseStack.popPose();

		// The left hand lets go of the Crossbar to grip the mount's nape for a Burrow.
		if (entity.isBurrowing()) return;
		poseStack.pushPose();
		rider.applyRiderTransform(poseStack);
		rider.rider().translateToRiderLeftHand(poseStack);
		poseStack.scale(CROSSBAR_SCALE, CROSSBAR_SCALE, CROSSBAR_SCALE);
		Minecraft.getInstance().getItemRenderer().renderStatic(crossbar,
				ItemDisplayContext.THIRD_PERSON_LEFT_HAND, packedLight, OverlayTexture.NO_OVERLAY,
				poseStack, buffer, entity.level(), entity.getId());
		poseStack.popPose();
	}
}

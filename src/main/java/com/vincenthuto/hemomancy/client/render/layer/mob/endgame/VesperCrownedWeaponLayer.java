package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperTheCrownedRefusalModel;
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

/** Renders the Crowned Refusal's Living Staff in the rider's right hand. */
public final class VesperCrownedWeaponLayer
		extends RenderLayer<VesperTheCrownedRefusalEntity, VesperTheCrownedRefusalModel> {
	private static final float STAFF_SCALE = 1.35F;
	private final ItemStack staff = new ItemStack(ItemInit.living_staff.get());

	public VesperCrownedWeaponLayer(
			RenderLayerParent<VesperTheCrownedRefusalEntity, VesperTheCrownedRefusalModel> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			VesperTheCrownedRefusalEntity entity, float limbSwing, float limbSwingAmount,
			float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.isInvisible()) return;

		poseStack.pushPose();
		getParentModel().translateToRiderWeapon(poseStack);
		poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(
				VesperWeaponGripRules.yawDegrees(EnumBloodTendency.FERRIC, false)));
		poseStack.scale(STAFF_SCALE, STAFF_SCALE, STAFF_SCALE);
		Minecraft.getInstance().getItemRenderer().renderStatic(staff,
				ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, OverlayTexture.NO_OVERLAY,
				poseStack, buffer, entity.level(), entity.getId());
		poseStack.popPose();
	}
}

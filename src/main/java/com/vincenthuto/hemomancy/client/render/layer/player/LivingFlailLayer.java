package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.item.LivingFlailModel;
import com.vincenthuto.hemomancy.client.player.LivingStaffMorphClientState;
import com.vincenthuto.hemomancy.client.render.item.harbinger.LivingFlailRenderHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingFlailItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class LivingFlailLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
	private static final float THIRD_PERSON_MODEL_PITCH_DEGREES = 0.0F;
	private static final float THIRD_PERSON_MODEL_YAW_DEGREES = 0.0F;
	private final LivingFlailModel<T> model;

	public LivingFlailLayer(LivingEntityRenderer<T, M> owner) {
		super(owner);
		this.model = new LivingFlailModel<>(
				Minecraft.getInstance().getEntityModels().bakeLayer(LivingFlailModel.LAYER_LOCATION));
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing,
			float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (!(entity instanceof Player player)) {
			return;
		}

		renderHand(player, player.getMainArm(), player.getMainHandItem(), poseStack, buffer, packedLight);
		renderHand(player, player.getMainArm().getOpposite(), player.getOffhandItem(), poseStack, buffer, packedLight);
	}

	private void renderHand(Player player, HumanoidArm arm, ItemStack stack, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight) {
		if (LivingStaffMorphClientState.affectsArm(player, arm)
				|| !(stack.getItem() instanceof LivingFlailItem)) {
			return;
		}

		poseStack.pushPose();
		getParentModel().translateToHand(arm, poseStack);
		float side = arm == HumanoidArm.LEFT ? -1.0f : 1.0f;
		poseStack.translate(side * 0.08f, 0.48f, -0.09f);
		poseStack.mulPose(Axis.XP.rotationDegrees(THIRD_PERSON_MODEL_PITCH_DEGREES));
		poseStack.mulPose(Axis.YP.rotationDegrees(THIRD_PERSON_MODEL_YAW_DEGREES));
		poseStack.mulPose(Axis.ZP.rotationDegrees(side * 8.0f));
		poseStack.scale(0.9f, 0.9f, 0.9f);
		LivingFlailRenderHelper.renderHeld(model, player, arm, stack, poseStack, buffer,
				packedLight, OverlayTexture.NO_OVERLAY, false);
		poseStack.popPose();
	}
}

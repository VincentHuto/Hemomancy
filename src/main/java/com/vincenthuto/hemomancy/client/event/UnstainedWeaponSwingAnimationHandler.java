package com.vincenthuto.hemomancy.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public class UnstainedWeaponSwingAnimationHandler {

	@SubscribeEvent
	public static void onRenderHand(RenderHandEvent event) {
		ItemStack stack = event.getItemStack();
		if (stack.isEmpty()) {
			return;
		}

		float swing = event.getSwingProgress();
		if (swing <= 0.0F) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null) {
			return;
		}

		HumanoidArm arm = event.getHand() == InteractionHand.MAIN_HAND
				? player.getMainArm()
				: player.getMainArm().getOpposite();
		float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		PoseStack poseStack = event.getPoseStack();

		if (stack.is(ItemInit.silthmere_glaive.get())) {
			applyGlaiveSwing(poseStack, swing, side);
		} else if (stack.is(ItemInit.unstained_warhammer.get())) {
			applyWarhammerSwing(poseStack, swing, side);
		} else if (stack.is(ItemInit.absolution_dagger.get())) {
			applyDaggerSwing(poseStack, swing, side);
		}
	}

	private static void applyGlaiveSwing(PoseStack poseStack, float swing, float side) {
		float sqrtSwing = Mth.sqrt(swing);
		float arc = Mth.sin(sqrtSwing * (float) Math.PI);
		float sweep = Mth.sin(swing * (float) Math.PI);

		poseStack.translate(side * 0.14F * sweep, 0.10F * arc, -0.06F * sweep);
		poseStack.mulPose(Axis.YP.rotationDegrees(side * 46.0F * arc));
		poseStack.mulPose(Axis.ZP.rotationDegrees(side * 62.0F * sweep));
		poseStack.mulPose(Axis.XP.rotationDegrees(-24.0F * arc));
	}

	private static void applyWarhammerSwing(PoseStack poseStack, float swing, float side) {
		float windup = 1.0F - smoothStep(Mth.clamp(swing / 0.68F, 0.0F, 1.0F));
		float slamPhase = smoothStep(Mth.clamp((swing - 0.58F) / 0.42F, 0.0F, 1.0F));
		float body = Mth.sin(swing * (float) Math.PI);

		poseStack.translate(side * 0.05F * windup, 0.18F * windup - 0.34F * slamPhase,
				-0.18F * windup - 0.08F * slamPhase);
		poseStack.mulPose(Axis.YP.rotationDegrees(side * (8.0F * windup - 6.0F * slamPhase)));
		poseStack.mulPose(Axis.ZP.rotationDegrees(side * (6.0F * windup - 8.0F * slamPhase)));
		poseStack.mulPose(Axis.XP.rotationDegrees(58.0F * windup - 178.0F * slamPhase - 10.0F * body));
	}

	private static void applyDaggerSwing(PoseStack poseStack, float swing, float side) {
		float tuck = Mth.clamp(1.0F - swing * 2.0F, 0.0F, 1.0F);
		float thrust = Mth.sin(swing * (float) Math.PI);
		float lift = Mth.clamp((swing - 0.18F) / 0.82F, 0.0F, 1.0F);

		poseStack.translate(side * -0.04F * tuck, 0.16F * thrust + 0.10F * lift, -0.10F * tuck + 0.18F * thrust);
		poseStack.mulPose(Axis.YP.rotationDegrees(side * (8.0F * tuck + 10.0F * thrust)));
		poseStack.mulPose(Axis.ZP.rotationDegrees(side * (8.0F * tuck + 16.0F * thrust)));
		poseStack.mulPose(Axis.XP.rotationDegrees(48.0F * thrust + 18.0F * lift));
	}

	private static float smoothStep(float value) {
		return value * value * (3.0F - 2.0F * value);
	}

}

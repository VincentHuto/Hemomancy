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

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
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

		poseStack.translate(side * -0.06F * sweep, 0.02F * sweep, -0.10F * sweep);
		poseStack.mulPose(Axis.YP.rotationDegrees(side * -70.0F * arc));
		poseStack.mulPose(Axis.ZP.rotationDegrees(side * -35.0F * sweep));
		poseStack.mulPose(Axis.XP.rotationDegrees(-18.0F * arc));
	}

	private static void applyWarhammerSwing(PoseStack poseStack, float swing, float side) {
		float raisePhase = Mth.clamp(1.0F - swing * 2.0F, 0.0F, 1.0F);
		float slamPhase = Mth.clamp((swing - 0.35F) / 0.65F, 0.0F, 1.0F);
		float body = Mth.sin(swing * (float) Math.PI);

		poseStack.translate(side * -0.04F * raisePhase, 0.14F * raisePhase - 0.20F * slamPhase, -0.16F * raisePhase);
		poseStack.mulPose(Axis.YP.rotationDegrees(side * (18.0F * raisePhase - 22.0F * slamPhase)));
		poseStack.mulPose(Axis.ZP.rotationDegrees(side * (10.0F * raisePhase - 16.0F * slamPhase)));
		poseStack.mulPose(Axis.XP.rotationDegrees(72.0F * raisePhase - 138.0F * slamPhase - 8.0F * body));
	}

	private static void applyDaggerSwing(PoseStack poseStack, float swing, float side) {
		float drawBack = Mth.sin((1.0F - swing) * (float) Math.PI);
		float drive = Mth.sin(swing * (float) Math.PI);

		poseStack.translate(side * -0.07F * drawBack, 0.10F * drawBack + 0.06F * drive, -0.18F * drawBack);
		poseStack.mulPose(Axis.YP.rotationDegrees(side * (18.0F * drawBack + 28.0F * drive)));
		poseStack.mulPose(Axis.ZP.rotationDegrees(side * (14.0F * drawBack + 52.0F * drive)));
		poseStack.mulPose(Axis.XP.rotationDegrees(-20.0F * drawBack - 64.0F * drive));
	}
}

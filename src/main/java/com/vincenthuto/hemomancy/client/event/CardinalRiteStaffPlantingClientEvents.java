package com.vincenthuto.hemomancy.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteFirstPersonPlantingPose;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteStaffPlantingClientState;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRitePlantingSequence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** First-person rendering, input restraint, and impact shake for staff planting. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class CardinalRiteStaffPlantingClientEvents {
	private CardinalRiteStaffPlantingClientEvents() {
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void renderHands(RenderHandEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		AbstractClientPlayer player = minecraft.player;
		if (player == null || !CardinalRiteStaffPlantingClientState.isAnimating(player)) return;

		event.setCanceled(true);
		if (event.getHand() != InteractionHand.MAIN_HAND) return;

		var animation = CardinalRiteStaffPlantingClientState.animation(player);
		float elapsed = CardinalRiteStaffPlantingClientState.elapsed(player, event.getPartialTick());
		float windup = CardinalRitePlantingSequence.windupProgress(elapsed);
		float strike = CardinalRitePlantingSequence.strikeProgress(elapsed);
		float recovery = CardinalRitePlantingSequence.recoveryProgress(elapsed);
		float vertical = CardinalRiteFirstPersonPlantingPose.verticalOffset(
				windup, strike, recovery);
		PoseStack poseStack = event.getPoseStack();
		PlayerRenderer renderer = (PlayerRenderer) minecraft.getEntityRenderDispatcher().getRenderer(player);

		renderArm(renderer, player, poseStack, event, true, vertical, windup, strike);
		renderArm(renderer, player, poseStack, event, false, vertical, windup, strike);

		poseStack.pushPose();
		poseStack.translate(0.0F, vertical - 0.08F, -0.72F);
		poseStack.mulPose(Axis.XP.rotationDegrees(-12.0F + windup * 12.0F - strike * 8.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		poseStack.scale(1.7F, 1.7F, 1.7F);
		minecraft.getItemRenderer().renderStatic(
				animation.staff(), ItemDisplayContext.FIXED, event.getPackedLight(),
				OverlayTexture.NO_OVERLAY, poseStack, event.getMultiBufferSource(),
				player.level(), player.getId());
		poseStack.popPose();
	}

	private static void renderArm(PlayerRenderer renderer, AbstractClientPlayer player,
			PoseStack poseStack, RenderHandEvent event, boolean right,
			float vertical, float windup, float strike) {
		float side = right ? 1.0F : -1.0F;
		poseStack.pushPose();
		poseStack.translate(CardinalRiteFirstPersonPlantingPose.shoulderOffsetX(right),
				vertical + 0.14F, -0.62F);
		poseStack.mulPose(Axis.YP.rotationDegrees(side * (18.0F + windup * 8.0F)));
		poseStack.mulPose(Axis.ZP.rotationDegrees(
				CardinalRiteFirstPersonPlantingPose.armRollDegrees(right, strike)));
		poseStack.mulPose(Axis.XP.rotationDegrees(-52.0F - windup * 48.0F + strike * 78.0F));
		poseStack.translate(
				CardinalRiteFirstPersonPlantingPose.modelPivotCorrectionX(right),
				CardinalRiteFirstPersonPlantingPose.modelPivotCorrectionY(),
				0.0F);
		if (right) {
			renderer.renderRightHand(poseStack, event.getMultiBufferSource(), event.getPackedLight(), player);
		} else {
			renderer.renderLeftHand(poseStack, event.getMultiBufferSource(), event.getPackedLight(), player);
		}
		poseStack.popPose();
	}

	@SubscribeEvent
	public static void tickClient(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player != null
				&& CardinalRiteStaffPlantingClientState.isAnimating(minecraft.player)) {
			minecraft.player.input.leftImpulse = 0.0F;
			minecraft.player.input.forwardImpulse = 0.0F;
			minecraft.player.input.jumping = false;
			minecraft.player.input.shiftKeyDown = false;
			minecraft.options.keyUse.setDown(false);
			minecraft.options.keyAttack.setDown(false);
			minecraft.player.stopUsingItem();
		}
		CardinalRiteStaffPlantingClientState.tick();
	}

	@SubscribeEvent
	public static void shakeCamera(ViewportEvent.ComputeCameraAngles event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null
				|| !CardinalRiteStaffPlantingClientState.isAnimating(minecraft.player)) return;
		float elapsed = CardinalRiteStaffPlantingClientState.elapsed(
				minecraft.player, (float) event.getPartialTick());
		float shake = CardinalRitePlantingSequence.cameraPitchShake(elapsed);
		event.setPitch(event.getPitch() + shake);
		event.setRoll(event.getRoll() + shake * 0.18F);
	}
}

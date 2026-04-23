package com.vincenthuto.hemomancy.client.render.layer.player;

import net.neoforged.fml.common.EventBusSubscriber;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Renders the equipped morphling on the player's right hand in first-person view.
 * Does NOT cancel the event â€” vanilla renders the arm normally, then we render
 * the morphling on top using the same transforms so it tracks the arm's bob/swing.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class EquippedMorphlingHandRenderer {

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onRenderHand(RenderHandEvent event) {
		// Only render on the main hand (right hand) when it's empty
		if (event.getHand() != InteractionHand.MAIN_HAND)
			return;
		if (!event.getItemStack().isEmpty())
			return;

		Minecraft mc = Minecraft.getInstance();
		AbstractClientPlayer player = mc.player;
		if (player == null)
			return;

		HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(cap -> {
			ItemStack morphling = cap.getEquippedMorphling();
			if (morphling.isEmpty())
				return;

			PoseStack poseStack = event.getPoseStack();
			MultiBufferSource buffer = event.getMultiBufferSource();
			int packedLight = event.getPackedLight();
			float swingProgress = event.getSwingProgress();
			float equipProgress = event.getEquipProgress();

			boolean rightHand = (player.getMainArm() == HumanoidArm.RIGHT);
			float side = rightHand ? 1.0F : -1.0F;

			poseStack.pushPose();

			// â”€â”€ Vanilla applyItemArmTransform (positions the arm in first-person) â”€â”€
			poseStack.translate(side * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);

			// â”€â”€ Vanilla applyItemArmAttackTransform (swing animation) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
			float sqrtSwing = Mth.sqrt(swingProgress);
			float sinSqrtPi = Mth.sin(sqrtSwing * (float) Math.PI);
			poseStack.mulPose(Axis.YP.rotationDegrees(side * sinSqrtPi * -20.0F));
			poseStack.mulPose(Axis.ZP.rotationDegrees(side * Mth.sin(swingProgress * swingProgress * (float) Math.PI) * -20.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(sinSqrtPi * -80.0F));

			// â”€â”€ Vanilla arm positioning (same as renderPlayerArm) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
			poseStack.mulPose(Axis.YP.rotationDegrees(side * 45.0F));

			// â”€â”€ Now position the morphling icon flat on the forearm â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
			// Offset from the arm origin to the top of the forearm
			poseStack.translate(side * -0.05F, -0.4F, 0.0F);

			// Lay flat on the arm surface facing upward
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));

			// Scale to fit on the arm (uniform scale)
			float scale = 0.25F;
			poseStack.scale(scale, scale, scale);

			mc.getItemRenderer().renderStatic(
					morphling, ItemDisplayContext.FIXED,
					packedLight, OverlayTexture.NO_OVERLAY,
					poseStack, buffer, player.level(), player.getId());

			poseStack.popPose();
		});
	}
}

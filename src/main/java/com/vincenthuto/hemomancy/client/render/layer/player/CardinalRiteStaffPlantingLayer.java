package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteStaffPlantingClientState;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRitePlantingSequence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

/** Draws the transient staff between both hands during the third-person planting pose. */
public final class CardinalRiteStaffPlantingLayer
		extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

	public CardinalRiteStaffPlantingLayer(
			RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
			float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		var animation = CardinalRiteStaffPlantingClientState.animation(player);
		if (animation == null) return;

		float elapsed = CardinalRiteStaffPlantingClientState.elapsed(player, partialTick);
		float windup = CardinalRitePlantingSequence.windupProgress(elapsed);
		float strike = CardinalRitePlantingSequence.strikeProgress(elapsed);
		float recovery = CardinalRitePlantingSequence.recoveryProgress(elapsed);

		poseStack.pushPose();
		getParentModel().body.translateAndRotate(poseStack);
		poseStack.translate(0.0D,
				-0.25D - windup * 0.55D + strike * 0.92D - recovery * 0.08D,
				-0.34D);
		poseStack.mulPose(Axis.XP.rotationDegrees(-8.0F + windup * 16.0F - strike * 12.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		poseStack.scale(1.35F, 1.35F, 1.35F);
		Minecraft.getInstance().getItemRenderer().renderStatic(
				animation.staff(), ItemDisplayContext.FIXED, packedLight,
				OverlayTexture.NO_OVERLAY, poseStack, buffer, player.level(), player.getId());
		poseStack.popPose();
	}
}

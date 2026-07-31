package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.tile.functional.FloatingHeartModel;
import com.vincenthuto.hemomancy.common.tile.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Reuses the Mortal Display heart model to show the temple heart's bond inside
 * its linked focus. An inserted medium makes the heart contract visibly.
 */
public class CardinalFocusRenderer implements BlockEntityRenderer<CardinalFocusBlockEntity> {
	private final FloatingHeartModel heart;

	public CardinalFocusRenderer(BlockEntityRendererProvider.Context context) {
		heart = new FloatingHeartModel(context.bakeLayer(FloatingHeartModel.mortal_display));
	}

	@Override
	public void render(CardinalFocusBlockEntity focus, float partialTick, PoseStack pose,
			MultiBufferSource buffers, int light, int overlay) {
		if (focus.getTempleDisplay() == null && !focus.hasMedium()) return;
		float time = focus.getLevel() == null ? 0.0F
				: focus.getLevel().getGameTime() + partialTick;
		float pulse = focus.hasMedium() ? 0.19F : 0.15F + (float) Math.sin(time * 0.12F) * 0.015F;
		pose.pushPose();
		pose.translate(0.5D, 0.66D, 0.5D);
		pose.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
		pose.mulPose(Vector3.YP.rotationDegrees(time * 1.5F).toMoj());
		pose.scale(pulse, pulse, pulse);
		VertexConsumer consumer = buffers.getBuffer(heart.renderType(MortalDisplayRenderer.texture));
		heart.renderToBuffer(pose, consumer, light, OverlayTexture.NO_OVERLAY, -1);
		pose.popPose();
	}
}

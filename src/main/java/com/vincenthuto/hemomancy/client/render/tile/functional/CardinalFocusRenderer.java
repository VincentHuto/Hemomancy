package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.tile.functional.FloatingHeartModel;
import com.vincenthuto.hemomancy.client.render.item.QliphothSeedTendrilEffects;
import com.vincenthuto.hemomancy.common.rite.CardinalFocusMediumVisualRules;
import com.vincenthuto.hemomancy.common.tile.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Reuses the Mortal Display heart model to show the temple heart's bond inside
 * its linked focus. An inserted medium makes the heart contract visibly.
 */
public class CardinalFocusRenderer implements BlockEntityRenderer<CardinalFocusBlockEntity> {
	private final FloatingHeartModel heart;
	private final ItemRenderer itemRenderer;

	public CardinalFocusRenderer(BlockEntityRendererProvider.Context context) {
		heart = new FloatingHeartModel(context.bakeLayer(FloatingHeartModel.mortal_display));
		itemRenderer = context.getItemRenderer();
	}

	@Override
	public void render(CardinalFocusBlockEntity focus, float partialTick, PoseStack pose,
			MultiBufferSource buffers, int light, int overlay) {
		if (focus.getTempleDisplay() == null && !focus.hasMedium()) return;
		float time = focus.getLevel() == null ? 0.0F
				: focus.getLevel().getGameTime() + partialTick;
		if (focus.getTempleDisplay() != null) {
			float pulse = focus.hasMedium() ? 0.19F
					: 0.15F + (float) Math.sin(time * 0.12F) * 0.015F;
			pose.pushPose();
			pose.translate(0.5D, 0.66D, 0.5D);
			pose.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
			pose.mulPose(Vector3.YP.rotationDegrees(time * 1.5F).toMoj());
			pose.scale(pulse, pulse, pulse);
			VertexConsumer consumer = buffers.getBuffer(heart.renderType(MortalDisplayRenderer.texture));
			heart.renderToBuffer(pose, consumer, light, OverlayTexture.NO_OVERLAY, -1);
			pose.popPose();
		}

		ItemStack medium = focus.getMediumDisplayStack();
		if (!medium.isEmpty()) {
			double mediumY = 0.88D + Math.sin(time * 0.08F) * 0.025D;
			pose.pushPose();
			pose.translate(0.5D, mediumY, 0.5D);
			pose.mulPose(Vector3.YP.rotationDegrees(time * 1.5F).toMoj());
			pose.scale(0.48F, 0.48F, 0.48F);
			itemRenderer.renderStatic(null, medium, ItemDisplayContext.FIXED, false,
					pose, buffers, focus.getLevel(), light, OverlayTexture.NO_OVERLAY, 0);
			pose.popPose();
			if (focus.getLevel() != null && CardinalFocusMediumVisualRules.emitsQliphothRoots(medium)) {
				Vec3 origin = Vec3.atLowerCornerOf(focus.getBlockPos()).add(0.5D, mediumY-.1, 0.5D);
				QliphothSeedTendrilEffects.spawnAt(focus.getBlockPos().asLong(), origin,
						focus.getLevel().getGameTime(), partialTick);
			}
		}
	}
}

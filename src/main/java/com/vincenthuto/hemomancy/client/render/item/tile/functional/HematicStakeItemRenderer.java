package com.vincenthuto.hemomancy.client.render.item.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.tile.functional.HematicStakeRenderer;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class HematicStakeItemRenderer extends BlockEntityWithoutLevelRenderer {
	private static final int TIME_CYCLE_MS = 240000;

	public HematicStakeItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		poseStack.pushPose();
		poseStack.translate(0.5f, 0.08f, 0.5f);
		applyDisplayTransform(displayContext, poseStack);

		float timeSeconds = (System.currentTimeMillis() % (long) TIME_CYCLE_MS) / 1000.0f;
		float shardSeed = Math.floorMod(System.identityHashCode(stack), 10007) / 10007.0f + stack.getCount() * 0.011f;
		float guiClamp = displayContext == ItemDisplayContext.GUI ? 1.0f : 0.0f;
		VertexConsumer vc = buffer.getBuffer(HemoRenderTypes.monolithFragment(timeSeconds, shardSeed, 0.0f, 0.72f, guiClamp));
		HematicStakeRenderer.drawJaggedStakeShard(vc, poseStack.last().pose(), LightTexture.FULL_BRIGHT, shardSeed,
				displayContext == ItemDisplayContext.GUI ? 0.68f : 0.82f);

		poseStack.popPose();
	}

	private static void applyDisplayTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
		if (displayContext == ItemDisplayContext.GUI) {
			poseStack.translate(0.0f, -0.03f, 0.0f);
			poseStack.mulPose(Axis.XP.rotationDegrees(18.0f));
			poseStack.mulPose(Axis.YP.rotationDegrees(-28.0f));
			poseStack.scale(1.38f, 1.38f, 1.38f);
		} else if (displayContext == ItemDisplayContext.FIXED) {
			poseStack.scale(1.18f, 1.18f, 1.18f);
		} else if (displayContext == ItemDisplayContext.GROUND) {
			poseStack.scale(0.82f, 0.82f, 0.82f);
		} else if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
				|| displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
			poseStack.mulPose(Axis.XP.rotationDegrees(8.0f));
			poseStack.scale(0.78f, 0.78f, 0.78f);
		} else {
			poseStack.scale(0.72f, 0.72f, 0.72f);
		}
	}
}

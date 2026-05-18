package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.MorphlingCradleBlock;
import com.vincenthuto.hemomancy.common.tile.functional.MorphlingCradleBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.AttachFace;

public class MorphlingCradleRenderer implements BlockEntityRenderer<MorphlingCradleBlockEntity> {
	private final ItemRenderer itemRenderer;

	public MorphlingCradleRenderer(BlockEntityRendererProvider.Context context) {
		this.itemRenderer = context.getItemRenderer();
	}

	@Override
	public void render(MorphlingCradleBlockEntity te, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn) {
		ItemStack hosted = te.getHostedMorphling();
		if (hosted.isEmpty()) {
			return;
		}

		AttachFace face = te.getBlockState().getValue(MorphlingCradleBlock.FACE);
		Direction facing = te.getBlockState().getValue(MorphlingCradleBlock.FACING);
		long gameTime = te.getLevel() != null ? te.getLevel().getGameTime() : (Util.getMillis() / 50L);
		float time = gameTime + partialTicks;
		float bob = Mth.sin(time * 0.1F) * 0.04F;

		poseStack.pushPose();
		if (face == AttachFace.FLOOR) {
			poseStack.translate(0.5F, 0.9F, 0.5F);
			poseStack.mulPose(Vector3.YP.rotationDegrees(time * 2.0F).toMoj());
			poseStack.translate(0.0F, bob, 0.0F);
		} else if (face == AttachFace.CEILING) {
			poseStack.translate(0.5F, 0.25F, 0.5F);
			poseStack.mulPose(Vector3.YP.rotationDegrees(time * 2.0F).toMoj());
			poseStack.translate(0.0F, -bob, 0.0F);
		} else {
			float outX = 0.5F + facing.getStepX() * 0.5F;
			float outZ = 0.5F + facing.getStepZ() * 0.5F;
			poseStack.translate(outX, 0.5F, outZ);
			poseStack.mulPose(Vector3.YP.rotationDegrees(time * 2.0F).toMoj());
			poseStack.translate(0.0F, bob, 0.0F);
		}
		this.itemRenderer.renderStatic(null, hosted, ItemDisplayContext.GROUND, false,
				poseStack, bufferIn, null, combinedLightIn, combinedOverlayIn, 0);
		poseStack.popPose();
	}
}

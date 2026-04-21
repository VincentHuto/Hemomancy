package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.MorphlingCradleModel;
import com.vincenthuto.hemomancy.common.block.functional.MorphlingCradleBlock;
import com.vincenthuto.hemomancy.common.tile.functional.MorphlingCradleBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.AttachFace;

public class MorphlingCradleRenderer implements BlockEntityRenderer<MorphlingCradleBlockEntity> {
	public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/block/hematic_iron_block.png");
	private final MorphlingCradleModel model;
	private final ItemRenderer itemRenderer;

	public MorphlingCradleRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new MorphlingCradleModel(context.bakeLayer(MorphlingCradleModel.LAYER_LOCATION));
		this.itemRenderer = context.getItemRenderer();
	}

	@Override
	public void render(MorphlingCradleBlockEntity te, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn) {
		AttachFace face = te.getBlockState().getValue(MorphlingCradleBlock.FACE);
		Direction facing = te.getBlockState().getValue(MorphlingCradleBlock.FACING);
		float yRot = switch (facing) {
			case NORTH -> 180f;
			case EAST -> 270f;
			case SOUTH -> 0f;
			case WEST -> 90f;
			default -> 0f;
		};

		poseStack.pushPose();
		if (face == AttachFace.FLOOR) {
			// Standard Blockbench Y-down → Y-up flip: root (y=24px=1.5 blocks) lands at y=0.
			poseStack.translate(0.5D, 1.5D, 0.5D);
			poseStack.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());
			poseStack.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());
		} else if (face == AttachFace.CEILING) {
			// No X-flip: without it the model is naturally inverted (root at top = ceiling).
			// translate y = -0.5 places root (1.5 blocks in model space) at y=1.0 (ceiling surface).
			poseStack.translate(0.5D, -0.5D, 0.5D);
			poseStack.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());
		} else { // WALL
			// Offset along the facing axis so the root (1.5 blocks model-depth) sits on the wall face.
			// facing.getStepX/Z is ±1 for horizontal directions, so the translate moves the origin
			// to the attachment face:  0.5 + step*1  gives 1.5 (positive axis) or -0.5 (negative axis).
			double wallTX = 0.5 + facing.getStepX();
			double wallTZ = 0.5 + facing.getStepZ();
			poseStack.translate(wallTX, 0.5D, wallTZ);
			poseStack.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());
			poseStack.mulPose(Vector3.XP.rotationDegrees(-90f).toMoj());
		}

		VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		model.renderToBuffer(poseStack, vertexConsumer, combinedLightIn, OverlayTexture.NO_OVERLAY,
				1.0F, 1.0F, 1.0F, 1.0F);
		poseStack.popPose();

		ItemStack hosted = te.getHostedMorphling();
		if (!hosted.isEmpty()) {
			long gameTime = te.getLevel() != null ? te.getLevel().getGameTime() : (Util.getMillis() / 50L);
			float time = gameTime + partialTicks;
			float bob = Mth.sin(time * 0.1F) * 0.04F;
			poseStack.pushPose();
			if (face == AttachFace.FLOOR) {
				// Arms top at ~world y=0.56; float item clearly above at y=0.9, bob upward.
				poseStack.translate(0.5F, 0.9F, 0.5F);
				poseStack.mulPose(Vector3.YP.rotationDegrees(time * 2.0F).toMoj());
				poseStack.translate(0.0F, bob, 0.0F);
			} else if (face == AttachFace.CEILING) {
				// Hanging arms bottom at ~world y=0.44; float item below at y=0.25, bob downward.
				poseStack.translate(0.5F, 0.25F, 0.5F);
				poseStack.mulPose(Vector3.YP.rotationDegrees(time * 2.0F).toMoj());
				poseStack.translate(0.0F, -bob, 0.0F);
			} else { // WALL
				// Bowl opens in the facing direction; item floats outward along that axis.
				float outX = 0.5F + facing.getStepX() * 0.5F;
				float outZ = 0.5F + facing.getStepZ() * 0.5F;
				poseStack.translate(outX, 0.5F, outZ);
				poseStack.mulPose(Vector3.YP.rotationDegrees(time * 2.0F).toMoj());
				poseStack.translate(0.0F, bob, 0.0F);
			}
			poseStack.scale(0.55F, 0.55F, 0.55F);
			this.itemRenderer.renderStatic(null, hosted, ItemDisplayContext.FIXED, true,
					poseStack, bufferIn, null, combinedLightIn, combinedOverlayIn, 0);
			poseStack.popPose();
		}
	}
}

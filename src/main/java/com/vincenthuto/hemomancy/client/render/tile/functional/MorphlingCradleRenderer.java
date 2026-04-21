package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.MorphlingCradleModel;
import com.vincenthuto.hemomancy.common.block.functional.MorphlingCradleBlock;
import com.vincenthuto.hemomancy.common.tile.functional.MorphlingCradleBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
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

	public MorphlingCradleRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new MorphlingCradleModel(context.bakeLayer(MorphlingCradleModel.LAYER_LOCATION));
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
			float time = ((te.getLevel() != null ? te.getLevel().getGameTime() : (Util.getMillis() / 50.0f)) + partialTicks);
			float bob = Mth.sin(time * 0.1F) * 0.04F;
			poseStack.pushPose();
			if (face == AttachFace.FLOOR) {
				// Float just above the bowl opening (~y=0.5 in world space).
				poseStack.translate(0.5F, 0.65F, 0.5F);
				poseStack.mulPose(Vector3.YP.rotationDegrees(time * 2.0F).toMoj());
				poseStack.translate(0.0F, bob, 0.0F);
			} else if (face == AttachFace.CEILING) {
				// Bowl opens downward; float below the opening (~y=0.5), bob away from ceiling.
				poseStack.translate(0.5F, 0.35F, 0.5F);
				poseStack.mulPose(Vector3.YP.rotationDegrees(time * 2.0F).toMoj());
				poseStack.translate(0.0F, -bob, 0.0F);
			} else { // WALL
				// Bowl opens in the facing direction; offset outward from the wall along that axis.
				float itemX = 0.5F + facing.getStepX() * 0.6F;
				float itemZ = 0.5F + facing.getStepZ() * 0.6F;
				poseStack.translate(itemX, 0.5F, itemZ);
				poseStack.mulPose(Vector3.YP.rotationDegrees(time * 2.0F).toMoj());
				poseStack.translate(facing.getStepX() * bob, 0.0F, facing.getStepZ() * bob);
			}
			poseStack.scale(0.55F, 0.55F, 0.55F);
			Minecraft.getInstance().getItemRenderer().renderStatic(null, hosted, ItemDisplayContext.FIXED, true,
					poseStack, bufferIn, null, combinedLightIn, combinedOverlayIn, 0);
			poseStack.popPose();
		}
	}
}

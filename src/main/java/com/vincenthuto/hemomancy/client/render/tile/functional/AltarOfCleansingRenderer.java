package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.CleansingAltarModel;
import com.vincenthuto.hemomancy.common.tile.functional.AltarOfCleansingBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class AltarOfCleansingRenderer implements BlockEntityRenderer<AltarOfCleansingBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_cleansing_altar.png");

	private final CleansingAltarModel model;

	public AltarOfCleansingRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new CleansingAltarModel(context.bakeLayer(CleansingAltarModel.LAYER_LOCATION));
	}

	@Override
	public boolean shouldRenderOffScreen(AltarOfCleansingBlockEntity te) {
		return true;
	}

	@Override
	public void render(AltarOfCleansingBlockEntity te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

		renderAltarModel(matrixStackIn, bufferIn, te, combinedLightIn, combinedOverlayIn);
	}

	/**
	 * Renders the cleansing altar entity model. The model is authored Y-down
	 * (Blockbench convention) so we flip 180° on X. We rotate around Y to
	 * match the block's FACING direction.
	 */
	private void renderAltarModel(PoseStack poseStack, MultiBufferSource bufferIn,
								  AltarOfCleansingBlockEntity te, int combinedLightIn, int combinedOverlayIn) {
		poseStack.pushPose();

		// Centre on the block
		poseStack.translate(0.5D, 1.5D, 0.5D);

		// Flip model upside-down (Blockbench Y-down → world Y-up)
		poseStack.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());

		// Rotate the model based on the block's facing direction
		Direction facing = te.getBlockState().getValue(FACING);
		float yRot = switch (facing) {
			case NORTH -> 180f;
			case EAST -> 270f;
			case SOUTH -> 0f;
			case WEST -> 90f;
			default -> 0f;
		};

		poseStack.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());

		VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
		model.renderToBuffer(poseStack, vertexConsumer, combinedLightIn, OverlayTexture.NO_OVERLAY,
				1.0F, 1.0F, 1.0F, 1.0F);

		poseStack.popPose();
	}
}

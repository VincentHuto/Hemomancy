package com.vincenthuto.hemomancy.client.render.tile.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.crafting.GhastlyAlembicModel;
import com.vincenthuto.hemomancy.common.tile.crafting.GhastlyAlembicBlockEntity;
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

public class GhastlyAlembicRenderer implements BlockEntityRenderer<GhastlyAlembicBlockEntity> {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_ghastly_alembic.png");

	private final GhastlyAlembicModel model;

	public GhastlyAlembicRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new GhastlyAlembicModel(context.bakeLayer(GhastlyAlembicModel.LAYER_LOCATION));
	}

	@Override
	public boolean shouldRenderOffScreen(GhastlyAlembicBlockEntity te) {
		return true;
	}

	@Override
	public void render(GhastlyAlembicBlockEntity te, float partialTicks, PoseStack poseStack,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		renderAlembicModel(poseStack, bufferIn, te, combinedLightIn, combinedOverlayIn);
	}

	/**
	 * Renders the ghastly alembic entity model. The model is authored Y-down
	 * (Blockbench convention) so we flip 180° on X. We rotate around Y to
	 * match the block's FACING direction.
	 */
	private void renderAlembicModel(PoseStack poseStack, MultiBufferSource bufferIn,
			GhastlyAlembicBlockEntity te, int combinedLightIn, int combinedOverlayIn) {
		poseStack.pushPose();

		// Centre on the block
		poseStack.translate(0.5D, 1.5D, 0.5D);

		// Flip model upside-down (Blockbench Y-down → world Y-up)
		poseStack.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());

		// Rotate to match the block's FACING direction
		Direction facing = te.getBlockState().getValue(FACING);
		float yRot = switch (facing) {
			case NORTH -> 180f;
			case EAST  -> 270f;
			case SOUTH -> 0f;
			case WEST  -> 90f;
			default    -> 0f;
		};
		poseStack.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());

		VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
		model.renderToBuffer(poseStack, vertexConsumer, combinedLightIn, OverlayTexture.NO_OVERLAY,
				1.0F, 1.0F, 1.0F, 1.0F);

		poseStack.popPose();
	}
}

package com.vincenthuto.hemomancy.client.render.tile.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.crafting.PallidRetortModel;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.crafting.PallidRetortBlockEntity;
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

import org.joml.Matrix4f;

public class PallidRetortRenderer implements BlockEntityRenderer<PallidRetortBlockEntity> {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/model_pallid_retort.png");

	private final PallidRetortModel model;

	public PallidRetortRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new PallidRetortModel(context.bakeLayer(PallidRetortModel.LAYER_LOCATION));
	}

	@Override
	public boolean shouldRenderOffScreen(PallidRetortBlockEntity te) {
		return true;
	}

	@Override
	public void render(PallidRetortBlockEntity te, float partialTicks, PoseStack poseStack,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		renderFluidLevel(poseStack, bufferIn, te, combinedLightIn);
		if (bufferIn instanceof MultiBufferSource.BufferSource source) {
			source.endBatch(RenderTypeInit.FLASK_FLUID);
		}
		renderAlembicModel(poseStack, bufferIn, te, combinedLightIn, combinedOverlayIn);
	}

	/**
	 * Renders the ghastly alembic entity model. The model is authored Y-down
	 * (Blockbench convention) so we flip 180° on X. We rotate around Y to
	 * match the block's FACING direction.
	 */
	private void renderAlembicModel(PoseStack poseStack, MultiBufferSource bufferIn,
			PallidRetortBlockEntity te, int combinedLightIn, int combinedOverlayIn) {
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
		model.renderToBuffer(poseStack, vertexConsumer, combinedLightIn, OverlayTexture.NO_OVERLAY, -1);

		poseStack.popPose();
	}

	/**
	 * Renders a translucent pale/white fluid box inside the Flask glass,
	 * whose height is proportional to the white humor fill percentage.
	 */
	private void renderFluidLevel(PoseStack poseStack, MultiBufferSource bufferIn,
			PallidRetortBlockEntity te, int combinedLightIn) {
		double current = te.getWhiteHumorVolume();
		double max = te.getMaxWhiteHumorVolume();
		if (max <= 0 || current <= 0) return;

		float fillPct = (float) Math.min(current / max, 1.0);

		float halfW = 4.5f / 16f;
		float halfD = 4.5f / 16f;
		float flaskBottomY = 4f / 16f;
		float flaskHeight = 15f / 16f;
		float fluidHeight = flaskHeight * fillPct;

		poseStack.pushPose();
		poseStack.translate(0.5f, flaskBottomY, 0.5f);

		// White humor colour: pale milky white, semi-transparent
		int r = 217, g = 217, b = 230, a = 191;

		VertexConsumer vc = bufferIn.getBuffer(RenderTypeInit.FLASK_FLUID);
		Matrix4f mat = poseStack.last().pose();

		renderColorBox(vc, mat, -halfW, 0, -halfD, halfW, fluidHeight, halfD, r, g, b, a);

		poseStack.popPose();
	}

	private static void renderColorBox(VertexConsumer vc, Matrix4f mat,
			float x0, float y0, float z0, float x1, float y1, float z1,
			int r, int g, int b, int a) {
		// Top
		vc.vertex(mat, x0, y1, z0).color(r, g, b, a).endVertex();
		vc.vertex(mat, x0, y1, z1).color(r, g, b, a).endVertex();
		vc.vertex(mat, x1, y1, z1).color(r, g, b, a).endVertex();
		vc.vertex(mat, x1, y1, z0).color(r, g, b, a).endVertex();
		// Bottom
		vc.vertex(mat, x0, y0, z1).color(r, g, b, a).endVertex();
		vc.vertex(mat, x0, y0, z0).color(r, g, b, a).endVertex();
		vc.vertex(mat, x1, y0, z0).color(r, g, b, a).endVertex();
		vc.vertex(mat, x1, y0, z1).color(r, g, b, a).endVertex();
		// North (z0)
		vc.vertex(mat, x1, y1, z0).color(r, g, b, a).endVertex();
		vc.vertex(mat, x1, y0, z0).color(r, g, b, a).endVertex();
		vc.vertex(mat, x0, y0, z0).color(r, g, b, a).endVertex();
		vc.vertex(mat, x0, y1, z0).color(r, g, b, a).endVertex();
		// South (z1)
		vc.vertex(mat, x0, y1, z1).color(r, g, b, a).endVertex();
		vc.vertex(mat, x0, y0, z1).color(r, g, b, a).endVertex();
		vc.vertex(mat, x1, y0, z1).color(r, g, b, a).endVertex();
		vc.vertex(mat, x1, y1, z1).color(r, g, b, a).endVertex();
		// West (x0)
		vc.vertex(mat, x0, y1, z0).color(r, g, b, a).endVertex();
		vc.vertex(mat, x0, y0, z0).color(r, g, b, a).endVertex();
		vc.vertex(mat, x0, y0, z1).color(r, g, b, a).endVertex();
		vc.vertex(mat, x0, y1, z1).color(r, g, b, a).endVertex();
		// East (x1)
		vc.vertex(mat, x1, y1, z1).color(r, g, b, a).endVertex();
		vc.vertex(mat, x1, y0, z1).color(r, g, b, a).endVertex();
		vc.vertex(mat, x1, y0, z0).color(r, g, b, a).endVertex();
		vc.vertex(mat, x1, y1, z0).color(r, g, b, a).endVertex();
	}
}


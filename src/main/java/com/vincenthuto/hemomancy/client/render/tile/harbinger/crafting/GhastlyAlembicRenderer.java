package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import org.joml.Matrix4f;

public class GhastlyAlembicRenderer implements BlockEntityRenderer<GhastlyAlembicBlockEntity> {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public GhastlyAlembicRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public boolean shouldRenderOffScreen(GhastlyAlembicBlockEntity te) {
		return true;
	}

	@Override
	public void render(GhastlyAlembicBlockEntity te, float partialTicks, PoseStack poseStack,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		renderFluidLevel(poseStack, bufferIn, te, combinedLightIn);
		if (bufferIn instanceof MultiBufferSource.BufferSource source) {
			source.endBatch(RenderTypeInit.FLASK_FLUID);
		}
	}

	private void renderFluidLevel(PoseStack poseStack, MultiBufferSource bufferIn,
			GhastlyAlembicBlockEntity te, int combinedLightIn) {
		double current = te.getBloodVolume();
		double max = te.getMaxBloodVolume();
		if (max <= 0 || current <= 0) return;

		float fillPct = (float) Math.min(current / max, 1.0);

		float halfW = 4.5f / 16f;
		float halfD = 4.5f / 16f;
		float flaskBottomY = 4f / 16f;
		float flaskHeight = 15f / 16f;
		float fluidHeight = flaskHeight * fillPct;

		poseStack.pushPose();
		poseStack.translate(0.5f, flaskBottomY, 0.5f);

		int r = 153, g = 13, b = 13, a = 191;

		VertexConsumer vc = bufferIn.getBuffer(RenderTypeInit.FLASK_FLUID);
		Matrix4f mat = poseStack.last().pose();

		renderColorBox(vc, mat, -halfW, 0, -halfD, halfW, fluidHeight, halfD, r, g, b, a);

		poseStack.popPose();
	}

	private static void renderColorBox(VertexConsumer vc, Matrix4f mat,
			float x0, float y0, float z0, float x1, float y1, float z1,
			int r, int g, int b, int a) {
		vc.addVertex(mat, x0, y1, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y1, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y0, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y1, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y1, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y0, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y1, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y0, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y1, z0).setColor(r, g, b, a);
	}
}

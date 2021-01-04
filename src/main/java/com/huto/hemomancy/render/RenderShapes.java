package com.huto.hemomancy.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class RenderShapes {
	public static void renderSizedCube(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
			int combinedOverlayIn, IVertexBuilder builderIn, float xOffset, float yOffset, float zOffset, float xScale,
			float yScale) {

		renderSizedRectangle(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, builderIn, xOffset, yOffset,
				zOffset, xScale, yScale, xScale);
	}

	public static void renderSizedCubes(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
			int combinedOverlayIn, IVertexBuilder builderIn, int amount, float xOffset, float yOffset, float zOffset,
			float xScale, float yScale) {

		for (int i = 0; i < amount; i++) {
			renderSizedCube(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, builderIn, xOffset,
					(yOffset) * i, zOffset, xScale, yScale);
		}

	}

	public static void renderSizedSlantedCube(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
			int combinedLightIn, int combinedOverlayIn, IVertexBuilder builderIn, float xOffset, float yOffset,
			float zOffset, float xScale, float yScale, float xSlant, float ySlant) {

		// Chest Panel
		matrixStackIn.push();
		IVertexBuilder builder = builderIn;
		int color = 0xB6B900;
		int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
		matrixStackIn.translate(xOffset, yOffset, zOffset);

		// Top

		builder.pos(matrixStackIn.getLast().getMatrix(), 0, yScale * ySlant, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * xSlant, yScale * ySlant, 0).color(r, g, b, 255)
				.tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * xSlant, yScale * ySlant, -xScale + xSlant)
				.color(r, g, b, 255).tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, yScale * ySlant, -xScale + xSlant).color(r, g, b, 255)
				.tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// Bottom
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0, -xScale).color(r, g, b, 255).tex(0, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0, -xScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// North
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0.0f, -xScale).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0, -xScale).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale * ySlant, -xScale + xSlant).color(r, g, b, 255)
				.tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * xSlant, yScale * ySlant, -xScale + xSlant)
				.color(r, g, b, 255).tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		// South
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale * ySlant, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * xSlant, yScale * ySlant, 0).color(r, g, b, 255)
				.tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0f, 0).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// East
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * xSlant, yScale * ySlant, 0).color(r, g, b, 255)
				.tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * xSlant, yScale * ySlant, -xScale + xSlant)
				.color(r, g, b, 255).tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0f, -xScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		// West
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale * ySlant, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale * ySlant, -xScale + xSlant).color(r, g, b, 255)
				.tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0f, -xScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		matrixStackIn.pop();

	}

	public static void renderSizedSlantedCubes(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
			int combinedLightIn, int combinedOverlayIn, IVertexBuilder builderIn, int amount, float xOffset,
			float yOffset, float zOffset, float xScale, float yScale, float xSlant, float ySlant) {

		for (int i = 0; i < amount; i++) {
			renderSizedSlantedCube(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, builderIn, xOffset,
					(yOffset) * i, zOffset, xScale, yScale, xSlant, ySlant);
		}

	}

	public static void renderSizedRectangle(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
			int combinedOverlayIn, IVertexBuilder builderIn, float xOffset, float yOffset, float zOffset, float xScale,
			float yScale, float zScale) {

		// Chest Panel
		matrixStackIn.push();
		IVertexBuilder builder = builderIn;
		int color = 0xB6B900;
		int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
		matrixStackIn.translate(xOffset, yOffset, zOffset);

		// Top
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, yScale, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, yScale, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, yScale, -zScale).color(r, g, b, 255).tex(0, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, yScale, -zScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// Bottom
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0, -zScale).color(r, g, b, 255).tex(0, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0, -zScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// North
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0.0f, -zScale).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0, -zScale).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale, -zScale).color(r, g, b, 255).tex(0, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, yScale, -zScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		// South
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, yScale, 0).color(r, g, b, 255).tex(0, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0f, 0).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// East
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, yScale, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, yScale, -zScale).color(r, g, b, 255).tex(0, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0f, -zScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		// West
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale, -zScale).color(r, g, b, 255).tex(0, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0f, -zScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		matrixStackIn.pop();

	}

	public static void renderSizedRectangles(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
			int combinedOverlayIn, IVertexBuilder builderIn, int amount, float xOffset, float yOffset, float zOffset,
			float xScale, float yScale, float zScale) {

		for (int i = 0; i < amount; i++) {
			renderSizedRectangle(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, builderIn, xOffset,
					(yOffset) * i, zOffset, xScale, yScale, zScale);
		}

	}

	public static void renderSizedSlantedRectangle(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
			int combinedLightIn, int combinedOverlayIn, IVertexBuilder builderIn, float xOffset, float yOffset,
			float zOffset, float xScale, float yScale, float zScale, float xSlant, float ySlant, float zSlant) {

		// Chest Panel
		matrixStackIn.push();
		IVertexBuilder builder = builderIn;
		int color = 0xB6B900;
		int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
		matrixStackIn.translate(xOffset, yOffset, zOffset);

		// Top
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, yScale * ySlant, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * xSlant, yScale * ySlant, 0).color(r, g, b, 255)
				.tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * xSlant, yScale * ySlant, -zScale + zSlant)
				.color(r, g, b, 255).tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, yScale * ySlant, -zScale + zSlant).color(r, g, b, 255)
				.tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// Bottom
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0, -zScale).color(r, g, b, 255).tex(0, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0, -zScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// North
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0.0f, -zScale).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0, -zScale).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale * ySlant, -zScale + zSlant).color(r, g, b, 255)
				.tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * xSlant, yScale * ySlant, -zScale + zSlant)
				.color(r, g, b, 255).tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		// South
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale * ySlant, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * xSlant, yScale * ySlant, 0).color(r, g, b, 255)
				.tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0f, 0).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// East
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * xSlant, yScale * ySlant, 0).color(r, g, b, 255)
				.tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * xSlant, yScale * ySlant, -zScale + zSlant)
				.color(r, g, b, 255).tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0f, -zScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		// West
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale * ySlant, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale * ySlant, -zScale + zSlant).color(r, g, b, 255)
				.tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0f, -zScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		matrixStackIn.pop();

	}

	public static void renderSizedSlantedRectangles(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
			int combinedLightIn, int combinedOverlayIn, IVertexBuilder builderIn, int amount, float xOffset,
			float yOffset, float zOffset, float xScale, float yScale, float zScale, float xSlant, float ySlant,
			float zSlant) {

		for (int i = 0; i < amount; i++) {
			renderSizedSlantedRectangle(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, builderIn, xOffset,
					(yOffset) * i, zOffset, xScale, yScale, zScale, xSlant, ySlant, zSlant);
		}

	}

	public static void renderSizedTunnel(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
			int combinedOverlayIn, IVertexBuilder builderIn, float xOffset, float yOffset, float zOffset, float xScale,
			float yScale, float zScale) {

		// Chest Panel
		matrixStackIn.push();
		IVertexBuilder builder = builderIn;
		int color = 0xB6B900;
		int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
		matrixStackIn.translate(xOffset, yOffset, zOffset);

		// Cube
		// Bottom
		// North
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0.0f, -zScale).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0, -zScale).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale, -zScale).color(r, g, b, 255).tex(0, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, yScale, -zScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		// South
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, yScale, 0).color(r, g, b, 255).tex(0, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0f, 0).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// East
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, yScale, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, yScale, -zScale).color(r, g, b, 255).tex(0, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0f, -zScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		// West
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale, -zScale).color(r, g, b, 255).tex(0, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0f, -zScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		matrixStackIn.pop();

	}

	public static void renderSizedTunnels(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
			int combinedOverlayIn, IVertexBuilder builderIn, int amount, float xOffset, float yOffset, float zOffset,
			float xScale, float yScale, float zScale) {

		for (int i = 0; i < amount; i++) {
			renderSizedTunnel(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, builderIn, xOffset,
					(yOffset) * i, zOffset, xScale, yScale, zScale);
		}

	}

	public static void renderSizedSlantedTunnel(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
			int combinedLightIn, int combinedOverlayIn, IVertexBuilder builderIn, float xOffset, float yOffset,
			float zOffset, float xScale, float yScale, float zScale, float xSlant, float ySlant, float zSlant) {

		// Chest Panel
		matrixStackIn.push();
		IVertexBuilder builder = builderIn;
		int color = 0xB6B900;
		int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
		matrixStackIn.translate(xOffset, yOffset, zOffset);

		// North
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0.0f, -zScale).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0, -zScale).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale * ySlant, -zScale + zSlant).color(r, g, b, 255)
				.tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * xSlant, yScale * ySlant, -zScale + zSlant)
				.color(r, g, b, 255).tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		// South
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale * ySlant, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * xSlant, yScale * ySlant, 0).color(r, g, b, 255)
				.tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0f, 0).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// East
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * xSlant, yScale * ySlant, 0).color(r, g, b, 255)
				.tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * xSlant, yScale * ySlant, -zScale + zSlant)
				.color(r, g, b, 255).tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale, 0f, -zScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		// West
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale * ySlant, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, yScale * ySlant, -zScale + zSlant).color(r, g, b, 255)
				.tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0f, -zScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		matrixStackIn.pop();

	}

	public static void renderSizedSlantedTunnels(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
			int combinedLightIn, int combinedOverlayIn, IVertexBuilder builderIn, int amount, float xOffset,
			float yOffset, float zOffset, float xScale, float yScale, float zScale, float xSlant, float ySlant,
			float zSlant) {
		for (int i = 0; i < amount; i++) {
			renderSizedSlantedTunnel(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, builderIn, xOffset,
					(yOffset) * i, zOffset, xScale, yScale, zScale, xSlant, ySlant, zSlant);
		}

	}

	public static void renderSizedOctahedron(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
			int combinedOverlayIn, IVertexBuilder builderIn, float baseScale, float xOffset, float yOffset,
			float zOffset, float xScale, float yScale, float zScale) {

		// Chest Panel
		matrixStackIn.push();
		IVertexBuilder builder = builderIn;
		int color = 0xB6B900;
		int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
		matrixStackIn.translate(xOffset, yOffset, zOffset);

		// Middle
		/*
		 * builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0.0f, 0).color(r, g, b,
		 * 255).tex(1, 1) .overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
		 * .normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		 * builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale, 0,
		 * 0).color(r, g, b, 255).tex(1, 0)
		 * .overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
		 * .normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		 * builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale, 0,
		 * -zScale * baseScale).color(r, g, b, 255) .tex(0,
		 * 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
		 * .normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		 * builder.pos(matrixStackIn.getLast().getMatrix(), 0 * baseScale, 0, -zScale *
		 * baseScale).color(r, g, b, 255) .tex(0,
		 * 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
		 * .normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		 */

		// North
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale, 0.0f, -zScale * baseScale)
				.color(r, g, b, 255).tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0, -zScale * baseScale).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		// South
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f * baseScale, 0.0f, 0 * baseScale).color(r, g, b, 255)
				.tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale, 0f, 0 * baseScale).color(r, g, b, 255)
				.tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// East
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale, 0.0f, 0 * baseScale).color(r, g, b, 255)
				.tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale, 0f, -zScale * baseScale)
				.color(r, g, b, 255).tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// West
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f * baseScale, 0.0f, 0 * baseScale).color(r, g, b, 255)
				.tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0f, -zScale * baseScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// Bottom Half
		// North
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale, 0.0f, -zScale * baseScale)
				.color(r, g, b, 255).tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0, -zScale * baseScale).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, -yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, -yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		// South
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f * baseScale, 0.0f, 0 * baseScale).color(r, g, b, 255)
				.tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, -yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, -yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale, 0f, 0 * baseScale).color(r, g, b, 255)
				.tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// East
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale, 0.0f, 0 * baseScale).color(r, g, b, 255)
				.tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, -yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, -yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale, 0f, -zScale * baseScale)
				.color(r, g, b, 255).tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// West
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f * baseScale, 0.0f, 0 * baseScale).color(r, g, b, 255)
				.tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, -yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, -yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0f, -zScale * baseScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		matrixStackIn.pop();

	}

	public static void renderSizedPyramid(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
			int combinedOverlayIn, IVertexBuilder builderIn, float baseScale, float xOffset, float yOffset,
			float zOffset, float xScale, float yScale, float zScale) {

		// Chest Panel
		matrixStackIn.push();
		IVertexBuilder builder = builderIn;
		int color = 0xB6B900;
		int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
		matrixStackIn.translate(xOffset, yOffset, zOffset);

		// Bottom
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale, 0, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale, 0, -zScale * baseScale).color(r, g, b, 255)
				.tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0 * baseScale, 0, -zScale * baseScale).color(r, g, b, 255)
				.tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// North
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale, 0.0f, -zScale * baseScale)
				.color(r, g, b, 255).tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0, -zScale * baseScale).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		// South
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f * baseScale, 0.0f, 0 * baseScale).color(r, g, b, 255)
				.tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale, 0f, 0 * baseScale).color(r, g, b, 255)
				.tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// East
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale, 0.0f, 0 * baseScale).color(r, g, b, 255)
				.tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale, 0f, -zScale * baseScale)
				.color(r, g, b, 255).tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// West
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f * baseScale, 0.0f, 0 * baseScale).color(r, g, b, 255)
				.tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * baseScale / 2, yScale, -zScale * baseScale / 2)
				.color(r, g, b, 255).tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0f, -zScale * baseScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		matrixStackIn.pop();

	}

	public static void renderSizedHouse(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
			int combinedOverlayIn, IVertexBuilder builderIn, float xOffset, float yOffset, float zOffset, float xScale,
			float yScale, float zScale, float cubeXScale, float cubeYScale, float cubeZScale) {

		matrixStackIn.push();
		IVertexBuilder builder = builderIn;
		int color = 0xB6B900;
		int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
		matrixStackIn.translate(xOffset, yOffset, zOffset);
		float pyramidXOffset = cubeXScale / cubeXScale - (xScale);
		float pyramidYOffset = cubeYScale;
		float pyramidZOffset = -cubeZScale / cubeZScale + (zScale);
		// Bottom
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), cubeXScale, 0, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), cubeXScale, 0, -cubeZScale).color(r, g, b, 255).tex(0, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0, -cubeZScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// North
		builder.pos(matrixStackIn.getLast().getMatrix(), cubeXScale, 0.0f, -cubeZScale).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0, -cubeZScale).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, cubeYScale, -cubeZScale).color(r, g, b, 255).tex(0, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), cubeXScale, cubeYScale, -cubeZScale).color(r, g, b, 255)
				.tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		// South
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, cubeYScale, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), cubeXScale, cubeYScale, 0).color(r, g, b, 255).tex(0, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), cubeXScale, 0f, 0).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// East
		builder.pos(matrixStackIn.getLast().getMatrix(), cubeXScale, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), cubeXScale, cubeYScale, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), cubeXScale, cubeYScale, -cubeZScale).color(r, g, b, 255)
				.tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), cubeXScale, 0f, -cubeZScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		// West
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0.0f, 0).color(r, g, b, 255).tex(1, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, cubeYScale, 0).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, cubeYScale, -cubeZScale).color(r, g, b, 255).tex(0, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0f, -cubeZScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		// Roof
		matrixStackIn.translate(pyramidXOffset, pyramidYOffset, pyramidZOffset);
		// North
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * cubeXScale, 0.0f, -zScale * cubeXScale)
				.color(r, g, b, 255).tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), 0f, 0, -zScale * cubeXScale).color(r, g, b, 255).tex(1, 0)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * cubeXScale / 2, yScale, -zScale * cubeXScale / 2)
				.color(r, g, b, 255).tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * cubeXScale / 2, yScale, -zScale * cubeXScale / 2)
				.color(r, g, b, 255).tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		// South
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f * cubeXScale, 0.0f, 0 * cubeXScale).color(r, g, b, 255)
				.tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * cubeXScale / 2, yScale, -zScale * cubeXScale / 2)
				.color(r, g, b, 255).tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * cubeXScale / 2, yScale, -zScale * cubeXScale / 2)
				.color(r, g, b, 255).tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * cubeXScale, 0f, 0 * cubeXScale).color(r, g, b, 255)
				.tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// East
		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * cubeXScale, 0.0f, 0 * cubeXScale).color(r, g, b, 255)
				.tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * cubeXScale / 2, yScale, -zScale * cubeXScale / 2)
				.color(r, g, b, 255).tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * cubeXScale / 2, yScale, -zScale * cubeXScale / 2)
				.color(r, g, b, 255).tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * cubeXScale, 0f, -zScale * cubeXScale)
				.color(r, g, b, 255).tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		// West
		builder.pos(matrixStackIn.getLast().getMatrix(), 0f * cubeXScale, 0.0f, 0 * cubeXScale).color(r, g, b, 255)
				.tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * cubeXScale / 2, yScale, -zScale * cubeXScale / 2)
				.color(r, g, b, 255).tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), xScale * cubeXScale / 2, yScale, -zScale * cubeXScale / 2)
				.color(r, g, b, 255).tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		builder.pos(matrixStackIn.getLast().getMatrix(), 0, 0f, -zScale * cubeXScale).color(r, g, b, 255).tex(0, 1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

		matrixStackIn.pop();
	}

}

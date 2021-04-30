package com.huto.hemomancy.util;

import com.huto.hemomancy.event.ClientEventSubscriber;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

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

	public static double getSpeed(PlayerEntity e) {
		Vector3d lastPos = new Vector3d(e.lastTickPosX, e.lastTickPosY, e.lastTickPosZ);
		Vector3d pos = new Vector3d(e.getPosX(), e.getPosY(), e.getPosZ());

		return Math.abs(lastPos.distanceTo(pos) * 20d);
	}

	public static void renderSizedPanel(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
			int combinedOverlayIn, IVertexBuilder builderIn, float xOffset, float yOffset, float zOffset, float xScale,
			float yScale, float zScale) {

		matrixStackIn.push();
		matrixStackIn.translate(-0.625, -0.1, 0.0625);
		PlayerEntity player = ClientEventSubscriber.getClientPlayer();
		float rotatSpeed = (float) (getSpeed(player) * 3);
		matrixStackIn.translate(0, rotatSpeed / 360, -rotatSpeed / 360);
		matrixStackIn.rotate(Vector3f.XP.rotationDegrees(rotatSpeed + 25));
		IVertexBuilder builder = builderIn;
		int color = 0xB6B900;
		int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
		matrixStackIn.translate(xOffset, yOffset - 1, zOffset);
		double time = Minecraft.getInstance().world.getGameTime();
		float sin = (float) -Math.abs(Math.sin(time * 0.05) * 0.05);
		int length = 2;

		yScale *= 2;

		Vector3f vec = new Vector3f(xScale, 0.0f, -zScale - sin);
		Vector3f vec1 = new Vector3f(0f, 0, -zScale - sin);
		Vector3f vec2 = new Vector3f(0f + 0, yScale, -zScale + 0);
		Vector3f vec3 = new Vector3f(xScale + 0, yScale, -zScale + 0);

		for (int i = 0; i < length; i++) {

			builder.pos(matrixStackIn.getLast().getMatrix(), vec.getX(), vec.getY(), vec.getZ()).color(r, g, b, 255)
					.tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
					.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

			builder.pos(matrixStackIn.getLast().getMatrix(), vec1.getX(), vec1.getY(), vec1.getZ()).color(r, g, b, 255)
					.tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
					.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

			builder.pos(matrixStackIn.getLast().getMatrix(), vec2.getX(), vec2.getY(), vec2.getZ()).color(r, g, b, 255)
					.tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
					.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

			builder.pos(matrixStackIn.getLast().getMatrix(), vec3.getX(), vec3.getY(), vec3.getZ()).color(r, g, b, 255)
					.tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
					.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

			float speedMult = (float) getSpeed(player) * 0.35f;
			speedMult = speedMult > 4 ? 4 : speedMult;
			float mod = -sin * (i * 2.15f) * speedMult;
			vec.add(0, 0, mod);
			vec1.add(0, 0, mod);
			vec2.add(0, 0.25f, 0);
			vec3.add(0, 0.25f, 0);

			matrixStackIn.translate(0, -0.25, 0);
			vec.setY(vec2.getY() + 0.25f);
			vec1.setY(vec3.getY() + 0.25f);

			builder.pos(matrixStackIn.getLast().getMatrix(), vec2.getX(), vec2.getY(), vec2.getZ()).color(r, g, b, 255)
					.tex(1, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
					.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

			builder.pos(matrixStackIn.getLast().getMatrix(), vec3.getX(), vec3.getY(), vec3.getZ()).color(r, g, b, 255)
					.tex(1, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
					.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

			builder.pos(matrixStackIn.getLast().getMatrix(), vec.getX(), vec.getY(), vec.getZ()).color(r, g, b, 255)
					.tex(0, 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
					.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();

			builder.pos(matrixStackIn.getLast().getMatrix(), vec1.getX(), vec1.getY(), vec1.getZ()).color(r, g, b, 255)
					.tex(0, 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
					.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
			vec.add(0, -0.25f, 0);
			vec1.add(0, -0.25f, 0);
			matrixStackIn.translate(0, 0.25, 0);
			vec2.setY(vec2.getY() + 0.25f);
			vec3.setY(vec3.getY() + 0.25f);
		}

		// Top
		/*
		 * for (int i = 0; i < 3; i++) {
		 * builder.pos(matrixStackIn.getLast().getMatrix(), vec.getX(), vec.getY(),
		 * vec.getZ()).color(r, g, b, 255) .tex(1,
		 * 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
		 * .normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		 * 
		 * builder.pos(matrixStackIn.getLast().getMatrix(), vec1.getX(), vec1.getY(),
		 * vec1.getZ()).color(r, g, b, 255) .tex(1,
		 * 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
		 * .normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		 * 
		 * builder.pos(matrixStackIn.getLast().getMatrix(), vec2.getX(), vec2.getY(),
		 * vec2.getZ()).color(r, g, b, 255) .tex(0,
		 * 0).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
		 * .normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		 * 
		 * builder.pos(matrixStackIn.getLast().getMatrix(), vec3.getX(), vec3.getY(),
		 * vec3.getZ()).color(r, g, b, 255) .tex(0,
		 * 1).overlay(OverlayTexture.NO_OVERLAY).lightmap(combinedLightIn)
		 * .normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
		 * 
		 * vec.add(0, 0.25f, 0); vec1.add(0, 0.25f, 0); vec2.add(0, 0.25f, 0);
		 * vec3.add(0, 0.25f, 0);
		 * 
		 * }
		 */

		matrixStackIn.pop();

	}

	public static void renderSizedRectangle(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
			int combinedOverlayIn, IVertexBuilder builderIn, float xOffset, float yOffset, float zOffset, float xScale,
			float yScale, float zScale) {

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

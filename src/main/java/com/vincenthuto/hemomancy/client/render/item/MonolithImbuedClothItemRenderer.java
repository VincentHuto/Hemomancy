package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.joml.Matrix4f;

public class MonolithImbuedClothItemRenderer extends BlockEntityWithoutLevelRenderer {
	private static final int BLACK = 0xFF020000;
	private static final int TIME_CYCLE_MS = 240000;
	private static final int COLUMNS = 7;
	private static final int ROWS = 5;

	public MonolithImbuedClothItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		applyDisplayTransform(displayContext, poseStack);

		float timeSeconds = (System.currentTimeMillis() % (long) TIME_CYCLE_MS) / 1000.0f;
		float seed = Math.floorMod(System.identityHashCode(stack), 10007) / 10007.0f + stack.getCount() * 0.011f;
		float guiClamp = displayContext == ItemDisplayContext.GUI ? 1.0f : 0.0f;
		VertexConsumer vc = buffer.getBuffer(HemoRenderTypes.monolithFragment(timeSeconds, seed, 0.12f, 0.84f, guiClamp));
		drawCloth(vc, poseStack.last().pose(), LightTexture.FULL_BRIGHT, seed, timeSeconds,
				displayContext == ItemDisplayContext.GUI);

		poseStack.popPose();
	}

	private static void applyDisplayTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
		if (displayContext == ItemDisplayContext.GUI) {
			poseStack.mulPose(Axis.XP.rotationDegrees(22.0f));
			poseStack.mulPose(Axis.YP.rotationDegrees(-28.0f));
			poseStack.mulPose(Axis.ZP.rotationDegrees(-7.0f));
			poseStack.scale(1.44f, 1.44f, 1.44f);
		} else if (displayContext == ItemDisplayContext.GROUND) {
			poseStack.mulPose(Axis.XP.rotationDegrees(74.0f));
			poseStack.scale(0.86f, 0.86f, 0.86f);
		} else if (displayContext == ItemDisplayContext.FIXED) {
			poseStack.mulPose(Axis.XP.rotationDegrees(18.0f));
			poseStack.scale(1.18f, 1.18f, 1.18f);
		} else if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
				|| displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
			poseStack.mulPose(Axis.XP.rotationDegrees(16.0f));
			poseStack.mulPose(Axis.ZP.rotationDegrees(-9.0f));
			poseStack.scale(0.78f, 0.78f, 0.78f);
		} else {
			poseStack.mulPose(Axis.XP.rotationDegrees(18.0f));
			poseStack.scale(0.76f, 0.76f, 0.76f);
		}
	}

	private static void drawCloth(VertexConsumer vc, Matrix4f mat, int light, float seed, float timeSeconds,
			boolean gui) {
		Point[][] points = new Point[ROWS + 1][COLUMNS + 1];
		for (int row = 0; row <= ROWS; row++) {
			float v = row / (float) ROWS;
			for (int column = 0; column <= COLUMNS; column++) {
				float u = column / (float) COLUMNS;
				points[row][column] = clothPoint(u, v, seed, timeSeconds, gui);
			}
		}

		for (int row = 0; row < ROWS; row++) {
			for (int column = 0; column < COLUMNS; column++) {
				Point p00 = points[row][column];
				Point p10 = points[row][column + 1];
				Point p01 = points[row + 1][column];
				Point p11 = points[row + 1][column + 1];
				drawTriangle(vc, mat, p00, p01, p11, light);
				drawTriangle(vc, mat, p00, p11, p10, light);
			}
		}
	}

	private static Point clothPoint(float u, float v, float seed, float timeSeconds, boolean gui) {
		float width = gui ? 0.44f : 0.50f;
		float height = gui ? 0.34f : 0.40f;
		float x = (u - 0.5f) * width;
		float y = (0.5f - v) * height;

		float edge = Math.min(Math.min(u, 1.0f - u), Math.min(v, 1.0f - v));
		float rag = (1.0f - smoothStep(edge * 5.0f)) * 0.028f;
		x += seedNoise(seed, (int) (u * 97.0f) + (int) (v * 131.0f) + 5) * rag;
		y += seedNoise(seed, (int) (u * 157.0f) + (int) (v * 83.0f) + 11) * rag;

		float sag = (float) Math.sin(u * Math.PI) * v * v * 0.075f;
		float flutter = (float) Math.sin(timeSeconds * 1.7f + u * 8.0f + seed * 6.28318f) * 0.012f;
		float z = sag + flutter * (0.35f + v * 0.65f);
		y -= sag * 0.42f;

		return new Point(x, y, z, u, v);
	}

	private static void drawTriangle(VertexConsumer vc, Matrix4f mat, Point a, Point b, Point c, int light) {
		int alpha = (BLACK >>> 24) & 0xFF;
		int red = (BLACK >>> 16) & 0xFF;
		int green = (BLACK >>> 8) & 0xFF;
		int blue = BLACK & 0xFF;
		vc.addVertex(mat, a.x(), a.y(), a.z()).setColor(red, green, blue, alpha).setUv(a.u(), a.v()).setLight(light);
		vc.addVertex(mat, b.x(), b.y(), b.z()).setColor(red, green, blue, alpha).setUv(b.u(), b.v()).setLight(light);
		vc.addVertex(mat, c.x(), c.y(), c.z()).setColor(red, green, blue, alpha).setUv(c.u(), c.v()).setLight(light);
	}

	private static float smoothStep(float value) {
		float clamped = Math.max(0.0f, Math.min(1.0f, value));
		return clamped * clamped * (3.0f - 2.0f * clamped);
	}

	private static float seedNoise(float seed, int salt) {
		int x = (int) (seed * 1_000_000.0f) + salt * 73428767;
		x ^= x >>> 13;
		x *= 1274126177;
		x ^= x >>> 16;
		return ((x & 0xFFFF) / 32767.5f) - 1.0f;
	}

	private record Point(float x, float y, float z, float u, float v) {
	}
}

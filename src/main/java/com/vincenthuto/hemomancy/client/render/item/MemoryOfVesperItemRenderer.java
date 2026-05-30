package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class MemoryOfVesperItemRenderer extends BlockEntityWithoutLevelRenderer {
	private static final int BLACK = 0xFF020000;
	private static final int LAT = 24;
	private static final int LON = 7;
	private static final float POME_RADIUS = 0.11f;
	private static final float TIME_CYCLE_MS = 240000.0f;

	public MemoryOfVesperItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		applyDisplayScale(displayContext, poseStack);

		float timeSeconds = (System.currentTimeMillis() % (long) TIME_CYCLE_MS) / 1000.0f;
		float shardSeed = Math.floorMod(System.identityHashCode(stack), 1543) / 1543.0f + 0.41f;
		float guiClamp = displayContext == ItemDisplayContext.GUI ? 1.0f : 0.0f;
		VertexConsumer vc = buffer.getBuffer(HemoRenderTypes.monolithFragment(timeSeconds, shardSeed,
				0.0f, 1.0f, guiClamp));
		drawPomeSilhouette(vc, poseStack.last().pose(), combinedLight, timeSeconds);

		poseStack.popPose();
	}

	private static void applyDisplayScale(ItemDisplayContext displayContext, PoseStack poseStack) {
		if (displayContext == ItemDisplayContext.GUI) {
			poseStack.scale(1.7f, 1.7f, 1.7f);
		} else if (displayContext == ItemDisplayContext.FIXED) {
			poseStack.scale(1.45f, 1.45f, 1.45f);
		} else if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
				|| displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
			poseStack.scale(1.05f, 1.05f, 1.05f);
		} else if (displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
				|| displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
			poseStack.scale(0.9f, 0.9f, 0.9f);
		}
	}

	private static void drawPomeSilhouette(VertexConsumer vc, Matrix4f mat, int combinedLight, float timeSeconds) {
		float breath = (float) Math.sin(timeSeconds * 0.9f) * 0.006f;
		for (int lat = 0; lat < LAT; lat++) {
			double theta0 = Math.PI * lat / LAT;
			double theta1 = Math.PI * (lat + 1) / LAT;
			for (int lon = 0; lon < LON; lon++) {
				double phi0 = 2.0 * Math.PI * lon / LON;
				double phi1 = 2.0 * Math.PI * (lon + 1) / LON;

				Point p00 = point(theta0, phi0, breath);
				Point p01 = point(theta0, phi1, breath);
				Point p11 = point(theta1, phi1, breath);
				Point p10 = point(theta1, phi0, breath);

				drawTriangle(vc, mat, p00, p01, p11, combinedLight);
				drawTriangle(vc, mat, p00, p11, p10, combinedLight);
			}
		}
	}

	private static Point point(double theta, double phi, float breath) {
		float sinT = (float) Math.sin(theta);
		float cosT = (float) Math.cos(theta);
		float cosP = (float) Math.cos(phi);
		float sinP = (float) Math.sin(phi);
		float taper = 1.0f - Math.max(0.0f, cosT) * 0.18f;
		float waist = 1.0f - (float) Math.pow(Math.abs(cosT), 2.0D) * 0.07f;
		float radius = (POME_RADIUS + breath) * taper * waist;
		float yScale = 1.12f;
		float x = sinT * cosP * radius;
		float y = cosT * (POME_RADIUS + breath) * yScale;
		float z = sinT * sinP * radius;
		float u = (float) (phi / (Math.PI * 2.0));
		float v = (float) (theta / Math.PI);
		return new Point(x, y, z, u, v);
	}

	private static void drawTriangle(VertexConsumer vc, Matrix4f mat, Point p0, Point p1, Point p2,
			int combinedLight) {
		int a = (BLACK >>> 24) & 0xFF;
		int r = (BLACK >>> 16) & 0xFF;
		int g = (BLACK >>> 8) & 0xFF;
		int b = BLACK & 0xFF;
		vc.addVertex(mat, p0.x(), p0.y(), p0.z()).setColor(r, g, b, a).setUv(p0.u(), p0.v()).setLight(combinedLight);
		vc.addVertex(mat, p1.x(), p1.y(), p1.z()).setColor(r, g, b, a).setUv(p1.u(), p1.v()).setLight(combinedLight);
		vc.addVertex(mat, p2.x(), p2.y(), p2.z()).setColor(r, g, b, a).setUv(p2.u(), p2.v()).setLight(combinedLight);
	}

	private record Point(float x, float y, float z, float u, float v) {
	}
}

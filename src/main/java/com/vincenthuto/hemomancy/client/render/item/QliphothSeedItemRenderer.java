package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class QliphothSeedItemRenderer extends BlockEntityWithoutLevelRenderer {
	private static final int LAT = 24;
	private static final int LON = 7;
	private static final float SEED_RADIUS = 0.072f;
	private static final float GLOW_RADIUS = SEED_RADIUS * 1.18f;
	private static final float HORIZONTAL_SCALE = 1.38f;
	private static final float VERTICAL_SCALE = 0.92f;
	private static final float DEPTH_SCALE = 0.96f;

	public QliphothSeedItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		applyDisplayScale(displayContext, poseStack);
		renderSeedBody(displayContext, poseStack, buffer, combinedLight, combinedOverlay);
		poseStack.popPose();
	}

	public static void renderSeedBody(ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		Matrix4f mat = poseStack.last().pose();
		float time = (System.currentTimeMillis() % 120000L) / 50.0f;
		float pulse = (float) (Math.sin(time * 0.055f) + 1.0f) * 0.5f;

		double hotAz = time * 0.061 + Math.sin(time * 0.027) * 0.9;
		double hotEl = Math.PI * 0.5 + Math.sin(time * 0.041 + 1.8) * 0.95;
		float hotX = (float) Math.cos(hotEl);
		float hotY = (float) (Math.cos(hotAz) * Math.sin(hotEl));
		float hotZ = (float) (Math.sin(hotAz) * Math.sin(hotEl));

		double hot2Az = time * -0.047 + 2.3 + Math.sin(time * 0.019) * 0.7;
		double hot2El = Math.PI * 0.5 + Math.sin(time * 0.033 + 3.7) * 0.8;
		float hot2X = (float) Math.cos(hot2El);
		float hot2Y = (float) (Math.cos(hot2Az) * Math.sin(hot2El));
		float hot2Z = (float) (Math.sin(hot2Az) * Math.sin(hot2El));

		VertexConsumer core = buffer.getBuffer(HemoRenderTypes.QLIPHOTH_CORE);
		drawSeedCore(core, mat, pulse);

		VertexConsumer glow = buffer.getBuffer(HemoRenderTypes.QLIPHOTH_GLOW);
		drawSeedGlow(glow, mat, pulse, hotX, hotY, hotZ, hot2X, hot2Y, hot2Z);
	}

	private static void applyDisplayScale(ItemDisplayContext displayContext, PoseStack poseStack) {
		if (displayContext == ItemDisplayContext.GUI) {
			poseStack.scale(1.75f, 1.75f, 1.75f);
		} else if (displayContext == ItemDisplayContext.FIXED) {
			poseStack.scale(1.35f, 1.35f, 1.35f);
		} else if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
				|| displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
			poseStack.scale(0.95f, 0.95f, 0.95f);
		} else if (displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
				|| displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
			poseStack.scale(0.82f, 0.82f, 0.82f);
		}
	}

	private static void drawSeedCore(VertexConsumer vc, Matrix4f mat, float pulse) {
		float r = 0.018f + 0.014f * pulse;
		float g = 0.002f;
		float b = 0.002f;
		int ri = toByte(r);
		int gi = toByte(g);
		int bi = toByte(b);
		int ai = toByte(0.96f);

		for (int lat = 0; lat < LAT; lat++) {
			double theta0 = Math.PI * lat / LAT;
			double theta1 = Math.PI * (lat + 1) / LAT;
			for (int lon = 0; lon < LON; lon++) {
				double phi0 = 2.0D * Math.PI * lon / LON;
				double phi1 = 2.0D * Math.PI * (lon + 1) / LON;
				Point p00 = point(theta0, phi0, SEED_RADIUS);
				Point p01 = point(theta0, phi1, SEED_RADIUS);
				Point p11 = point(theta1, phi1, SEED_RADIUS);
				Point p10 = point(theta1, phi0, SEED_RADIUS);
				quad(vc, mat, p00, p01, p11, p10, ri, gi, bi, ai);
			}
		}
	}

	private static void drawSeedGlow(VertexConsumer vc, Matrix4f mat, float pulse,
			float hotX, float hotY, float hotZ, float hot2X, float hot2Y, float hot2Z) {
		for (int lat = 0; lat < LAT; lat++) {
			double theta0 = Math.PI * lat / LAT;
			double theta1 = Math.PI * (lat + 1) / LAT;
			double thetaMid = Math.PI * (lat + 0.5D) / LAT;
			for (int lon = 0; lon < LON; lon++) {
				double phi0 = 2.0D * Math.PI * lon / LON;
				double phi1 = 2.0D * Math.PI * (lon + 1) / LON;
				double phiMid = 2.0D * Math.PI * (lon + 0.5D) / LON;

				Point normal = unitPoint(thetaMid, phiMid);
				float dot1 = normal.x() * hotX + normal.y() * hotY + normal.z() * hotZ;
				float dot2 = normal.x() * hot2X + normal.y() * hot2Y + normal.z() * hot2Z;
				float bloom1 = Math.max(0.0f, dot1);
				bloom1 = bloom1 * bloom1 * bloom1;
				float bloom2 = Math.max(0.0f, dot2);
				bloom2 = bloom2 * bloom2;
				float bloom = Math.min(1.0f, bloom1 + bloom2 * 0.55f);
				if (bloom < 0.005f) {
					continue;
				}

				int ri = toByte(0.58f + 0.37f * bloom * pulse);
				int gi = toByte(0.018f + 0.025f * bloom);
				int bi = toByte(0.02f);
				int ai = toByte(bloom * (0.28f + 0.18f * pulse));
				Point p00 = point(theta0, phi0, GLOW_RADIUS);
				Point p01 = point(theta0, phi1, GLOW_RADIUS);
				Point p11 = point(theta1, phi1, GLOW_RADIUS);
				Point p10 = point(theta1, phi0, GLOW_RADIUS);
				quad(vc, mat, p00, p01, p11, p10, ri, gi, bi, ai);
			}
		}
	}

	private static Point point(double theta, double phi, float radius) {
		float sinT = (float) Math.sin(theta);
		float cosT = (float) Math.cos(theta);
		float cosP = (float) Math.cos(phi);
		float sinP = (float) Math.sin(phi);
		float endPinch = 1.0f - (float) Math.pow(Math.abs(cosT), 2.6D) * 0.11f;
		float x = cosT * radius * HORIZONTAL_SCALE;
		float y = sinT * cosP * radius * VERTICAL_SCALE * endPinch;
		float z = sinT * sinP * radius * DEPTH_SCALE * endPinch;
		return new Point(x, y, z);
	}

	private static Point unitPoint(double theta, double phi) {
		float sinT = (float) Math.sin(theta);
		float cosT = (float) Math.cos(theta);
		float cosP = (float) Math.cos(phi);
		float sinP = (float) Math.sin(phi);
		return new Point(cosT, sinT * cosP, sinT * sinP);
	}

	private static void quad(VertexConsumer vc, Matrix4f mat, Point p0, Point p1, Point p2, Point p3,
			int r, int g, int b, int a) {
		vc.addVertex(mat, p0.x(), p0.y(), p0.z()).setColor(r, g, b, a);
		vc.addVertex(mat, p1.x(), p1.y(), p1.z()).setColor(r, g, b, a);
		vc.addVertex(mat, p2.x(), p2.y(), p2.z()).setColor(r, g, b, a);
		vc.addVertex(mat, p3.x(), p3.y(), p3.z()).setColor(r, g, b, a);
	}

	private static int toByte(float component) {
		return Mth.clamp((int) (component * 255.0f), 0, 255);
	}

	private record Point(float x, float y, float z) {
	}
}

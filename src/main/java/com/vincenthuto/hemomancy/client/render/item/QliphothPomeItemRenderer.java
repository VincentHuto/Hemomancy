  package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.joml.Matrix4f;

/**
 * Custom item renderer for the Qliphoth Pome. Draws a small dark sphere
 * with a crimson bloom hotspot that slowly wanders across its surface,
 * matching the void-orbs embedded in the Qliphoth tree trunk.
 */
public class QliphothPomeItemRenderer extends BlockEntityWithoutLevelRenderer {

	/** Sphere resolution. */
	private static final int LAT = 24;
	private static final int LON = 24;
	/** Orb visual radius — kept small so it reads as a collectable fruit. */
	private static final float ORB_RADIUS = 0.1f;
	/** Radius of the glow shell (slightly larger than the orb). */
	private static final float GLOW_RADIUS = ORB_RADIUS * 1.15f;

	public QliphothPomeItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

		poseStack.pushPose();
		poseStack.translate(0.5, 0.5, 0.5);

		// Context-appropriate scaling
		if (displayContext == ItemDisplayContext.GUI) {
			poseStack.scale(1.8f, 1.8f, 1.8f);
		} else if (displayContext == ItemDisplayContext.FIXED) {
			poseStack.scale(1.4f, 1.4f, 1.4f);
		} else if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
				|| displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
			poseStack.scale(1.0f, 1.0f, 1.0f);
		} else if (displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
				|| displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
			poseStack.scale(0.9f, 0.9f, 0.9f);
		}
		// GROUND is 1.0 by default

		Matrix4f mat = poseStack.last().pose();

		float time = (float) (System.currentTimeMillis() % 100000L) / 50.0f;
		float pulse = (float) (Math.sin(time * 0.06) + 1.0) * 0.5f;

		// ── Wandering bloom hotspot on the sphere surface ──
		// Two layered Lissajous-style orbits so it doesn't just loop in a circle
		double hotAz = time * 0.07 + Math.sin(time * 0.031) * 1.2;
		double hotEl = Math.PI * 0.5 + Math.sin(time * 0.043 + 1.0) * 1.1;
		float hotX = (float) (Math.cos(hotAz) * Math.sin(hotEl));
		float hotY = (float) Math.cos(hotEl);
		float hotZ = (float) (Math.sin(hotAz) * Math.sin(hotEl));

		// Second smaller hotspot for extra life
		double hot2Az = time * -0.05 + 2.0 + Math.sin(time * 0.023) * 0.9;
		double hot2El = Math.PI * 0.5 + Math.sin(time * 0.037 + 3.0);
		float hot2X = (float) (Math.cos(hot2Az) * Math.sin(hot2El));
		float hot2Y = (float) Math.cos(hot2El);
		float hot2Z = (float) (Math.sin(hot2Az) * Math.sin(hot2El));

		VertexConsumer coreVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
		VertexConsumer glowVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);

		drawDarkSphere(coreVC, mat, pulse);
		drawBloomSphere(glowVC, mat, pulse, hotX, hotY, hotZ, hot2X, hot2Y, hot2Z);

		poseStack.popPose();
	}

	/**
	 * Draws the near-black core sphere.
	 */
	private static void drawDarkSphere(VertexConsumer vc, Matrix4f mat, float pulse) {
		float oR = 0.02f + 0.015f * pulse;
		float oG = 0.004f;
		float oB = 0.004f;
		float oA = 0.95f;

		for (int lat = 0; lat < LAT; lat++) {
			double theta0 = Math.PI * lat / LAT;
			double theta1 = Math.PI * (lat + 1) / LAT;

			float cosT0 = (float) Math.cos(theta0), sinT0 = (float) Math.sin(theta0);
			float cosT1 = (float) Math.cos(theta1), sinT1 = (float) Math.sin(theta1);

			for (int lon = 0; lon < LON; lon++) {
				double phi0 = 2.0 * Math.PI * lon / LON;
				double phi1 = 2.0 * Math.PI * (lon + 1) / LON;

				float cosP0 = (float) Math.cos(phi0), sinP0 = (float) Math.sin(phi0);
				float cosP1 = (float) Math.cos(phi1), sinP1 = (float) Math.sin(phi1);

				vc.vertex(mat, sinT0*cosP0*ORB_RADIUS, cosT0*ORB_RADIUS, sinT0*sinP0*ORB_RADIUS).color(oR,oG,oB,oA).endVertex();
				vc.vertex(mat, sinT0*cosP1*ORB_RADIUS, cosT0*ORB_RADIUS, sinT0*sinP1*ORB_RADIUS).color(oR,oG,oB,oA).endVertex();
				vc.vertex(mat, sinT1*cosP1*ORB_RADIUS, cosT1*ORB_RADIUS, sinT1*sinP1*ORB_RADIUS).color(oR,oG,oB,oA).endVertex();
				vc.vertex(mat, sinT1*cosP0*ORB_RADIUS, cosT1*ORB_RADIUS, sinT1*sinP0*ORB_RADIUS).color(oR,oG,oB,oA).endVertex();
			}
		}
	}

	/**
	 * Draws a glow shell slightly larger than the core sphere, where each quad's
	 * alpha and color intensity depends on proximity to the wandering bloom
	 * hotspot(s). This makes the red glow appear to crawl across the surface.
	 */
	private static void drawBloomSphere(VertexConsumer vc, Matrix4f mat, float pulse,
			float hotX, float hotY, float hotZ,
			float hot2X, float hot2Y, float hot2Z) {

		for (int lat = 0; lat < LAT; lat++) {
			double theta0 = Math.PI * lat / LAT;
			double theta1 = Math.PI * (lat + 1) / LAT;
			double thetaMid = Math.PI * (lat + 0.5) / LAT;

			float cosT0 = (float) Math.cos(theta0), sinT0 = (float) Math.sin(theta0);
			float cosT1 = (float) Math.cos(theta1), sinT1 = (float) Math.sin(theta1);
			float cosTM = (float) Math.cos(thetaMid), sinTM = (float) Math.sin(thetaMid);

			for (int lon = 0; lon < LON; lon++) {
				double phi0 = 2.0 * Math.PI * lon / LON;
				double phi1 = 2.0 * Math.PI * (lon + 1) / LON;
				double phiMid = 2.0 * Math.PI * (lon + 0.5) / LON;

				float cosP0 = (float) Math.cos(phi0), sinP0 = (float) Math.sin(phi0);
				float cosP1 = (float) Math.cos(phi1), sinP1 = (float) Math.sin(phi1);

				// Quad center normal direction (unit vector on sphere)
				float nx = (float) (Math.cos(phiMid) * sinTM);
				float ny = cosTM;
				float nz = (float) (Math.sin(phiMid) * sinTM);

				// Dot product with hotspot direction = cosine of angle between them
				// 1.0 = directly at hotspot, -1.0 = opposite side
				float dot1 = nx * hotX + ny * hotY + nz * hotZ;
				float dot2 = nx * hot2X + ny * hot2Y + nz * hot2Z;

				// Convert to bloom intensity: tight falloff so the glow is a focused patch
				// Primary hotspot: larger, brighter
				float bloom1 = Math.max(0f, dot1);
				bloom1 = bloom1 * bloom1 * bloom1; // cubic falloff — tight bright spot
				// Secondary hotspot: dimmer, softer
				float bloom2 = Math.max(0f, dot2);
				bloom2 = bloom2 * bloom2; // quadratic — slightly broader

				float bloom = Math.min(1f, bloom1 + bloom2 * 0.5f);

				if (bloom < 0.005f) continue; // skip quads with negligible glow

				float gR = 0.55f + 0.40f * bloom * pulse;
				float gG = 0.02f + 0.03f * bloom;
				float gB = 0.02f;
				float gA = bloom * (0.30f + 0.20f * pulse);

				vc.vertex(mat, sinT0*cosP0*GLOW_RADIUS, cosT0*GLOW_RADIUS, sinT0*sinP0*GLOW_RADIUS).color(gR,gG,gB,gA).endVertex();
				vc.vertex(mat, sinT0*cosP1*GLOW_RADIUS, cosT0*GLOW_RADIUS, sinT0*sinP1*GLOW_RADIUS).color(gR,gG,gB,gA).endVertex();
				vc.vertex(mat, sinT1*cosP1*GLOW_RADIUS, cosT1*GLOW_RADIUS, sinT1*sinP1*GLOW_RADIUS).color(gR,gG,gB,gA).endVertex();
				vc.vertex(mat, sinT1*cosP0*GLOW_RADIUS, cosT1*GLOW_RADIUS, sinT1*sinP0*GLOW_RADIUS).color(gR,gG,gB,gA).endVertex();
			}
		}
	}
}

package com.vincenthuto.hemomancy.client.render.world;

import java.util.List;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Renders a living, undulating glowing red boundary circle for active cardinal
 * rites. The ring breathes — its radius wobbles per-segment — and vein-like
 * branches sprout inward, tapering to nothing, giving an organic capillary
 * feel. All geometry is flat POSITION_COLOR quads with proper depth testing.
 */
public class CardinalRiteBoundaryRenderer {

	// ── Ring geometry ──
	private static final int SEGMENTS = 96;
	private static final float CORE_WIDTH = 0.09f;
	private static final float GLOW_WIDTH = 0.22f;

	// ── Undulation parameters ──
	/** How many "bumps" around the circumference at any moment. */
	private static final double UNDULATE_FREQ = 6.0;
	/** Max radial displacement of the undulation (blocks). */
	private static final float UNDULATE_AMP = 0.12f;
	/** How fast the undulation pattern crawls around the ring. */
	private static final double UNDULATE_SPEED = 0.045;
	/** Secondary higher-frequency wobble layered on top. */
	private static final double UNDULATE_FREQ2 = 13.0;
	private static final float UNDULATE_AMP2 = 0.04f;
	private static final double UNDULATE_SPEED2 = 0.09;

	// ── Vein parameters ──
	/** How many vein branches around the ring. */
	private static final int VEIN_COUNT = 12;
	/** Number of quad segments per vein branch. */
	private static final int VEIN_SEGS = 6;
	/** Max inward reach of a vein (blocks). */
	private static final float VEIN_LENGTH = 0.7f;
	/** Width at root of vein. */
	private static final float VEIN_ROOT_WIDTH = 0.04f;
	/** How fast veins crawl around the ring. */
	private static final double VEIN_CRAWL_SPEED = 0.012;

	public static void render(PoseStack poseStack, float partialTick) {
		List<ActiveRiteClientData.RiteEntry> rites = ActiveRiteClientData.getActiveRites();
		if (rites.isEmpty()) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		float currentTime = mc.level.getGameTime() + partialTick;
		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

		for (ActiveRiteClientData.RiteEntry rite : rites) {
			drawBoundaryRing(poseStack, buffer, rite, currentTime, cam);
		}

		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_CORE);
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_GLOW);
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Main ring + veins
	// ════════════════════════════════════════════════════════════════════════

	private static void drawBoundaryRing(PoseStack stack, MultiBufferSource buffer,
			ActiveRiteClientData.RiteEntry rite, float currentTime, Vec3 cam) {

		BlockPos center = rite.getCenter();
		float baseRadius = (float) (rite.getRiteSize() / 2.0 + 1.0);
		double cx = center.getX() + 0.5;
		double cy = center.getY() + 0.065;
		double cz = center.getZ() + 0.5;

		// Global breathing pulse (0..1)
		double pulse = (Math.sin(currentTime * 0.08) + 1.0) * 0.5;
		float coreAlpha = (float) (0.65 + 0.35 * pulse);
		float glowAlpha = (float) (0.15 + 0.15 * pulse);

		float coreR = (float) (0.85 + 0.15 * pulse);
		float coreG = 0.05f;
		float coreB = 0.04f;
		float glowR = (float) (0.55 + 0.2 * pulse);
		float glowG = 0.02f;
		float glowB = 0.02f;

		stack.pushPose();
		stack.translate(cx - cam.x, cy - cam.y, cz - cam.z);
		Matrix4f mat = stack.last().pose();

		VertexConsumer coreVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
		VertexConsumer glowVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);

		// ── Draw the undulating ring ──
		for (int i = 0; i < SEGMENTS; i++) {
			double a1 = Math.toRadians((360.0 / SEGMENTS) * i);
			double a2 = Math.toRadians((360.0 / SEGMENTS) * (i + 1));

			float r1 = baseRadius + undulation(a1, currentTime);
			float r2 = baseRadius + undulation(a2, currentTime);

			// Subtle Y wave so it's not perfectly flat
			float y1 = (float) (Math.sin(a1 * 5.0 + currentTime * 0.06) * 0.012);
			float y2 = (float) (Math.sin(a2 * 5.0 + currentTime * 0.06) * 0.012);

			float cos1 = (float) Math.cos(a1);
			float sin1 = (float) Math.sin(a1);
			float cos2 = (float) Math.cos(a2);
			float sin2 = (float) Math.sin(a2);

			float iGlow1 = r1 - GLOW_WIDTH - CORE_WIDTH * 0.5f;
			float iCore1 = r1 - CORE_WIDTH * 0.5f;
			float oCore1 = r1 + CORE_WIDTH * 0.5f;
			float oGlow1 = r1 + GLOW_WIDTH + CORE_WIDTH * 0.5f;

			float iGlow2 = r2 - GLOW_WIDTH - CORE_WIDTH * 0.5f;
			float iCore2 = r2 - CORE_WIDTH * 0.5f;
			float oCore2 = r2 + CORE_WIDTH * 0.5f;
			float oGlow2 = r2 + GLOW_WIDTH + CORE_WIDTH * 0.5f;

			// Inner glow
			emitQuad(glowVC, mat,
					cos1 * iGlow1, y1, sin1 * iGlow1, glowR, glowG, glowB, 0f,
					cos1 * iCore1, y1, sin1 * iCore1, glowR, glowG, glowB, glowAlpha,
					cos2 * iCore2, y2, sin2 * iCore2, glowR, glowG, glowB, glowAlpha,
					cos2 * iGlow2, y2, sin2 * iGlow2, glowR, glowG, glowB, 0f);

			// Core
			emitQuad(coreVC, mat,
					cos1 * iCore1, y1, sin1 * iCore1, coreR, coreG, coreB, coreAlpha,
					cos1 * oCore1, y1, sin1 * oCore1, coreR, coreG, coreB, coreAlpha,
					cos2 * oCore2, y2, sin2 * oCore2, coreR, coreG, coreB, coreAlpha,
					cos2 * iCore2, y2, sin2 * iCore2, coreR, coreG, coreB, coreAlpha);

			// Outer glow
			emitQuad(glowVC, mat,
					cos1 * oCore1, y1, sin1 * oCore1, glowR, glowG, glowB, glowAlpha,
					cos1 * oGlow1, y1, sin1 * oGlow1, glowR, glowG, glowB, 0f,
					cos2 * oGlow2, y2, sin2 * oGlow2, glowR, glowG, glowB, 0f,
					cos2 * oCore2, y2, sin2 * oCore2, glowR, glowG, glowB, glowAlpha);
		}

		// ── Draw vein branches sprouting inward ──
		drawVeins(glowVC, coreVC, mat, baseRadius, currentTime,
				coreR, coreG, coreB, coreAlpha, glowR, glowG, glowB);

		stack.popPose();
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Undulation — layered sine waves that make the ring breathe
	// ════════════════════════════════════════════════════════════════════════

	/** Returns radial offset for a given angle at the current time. */
	private static float undulation(double angleRad, float time) {
		// Primary slow, large wave
		double w1 = Math.sin(angleRad * UNDULATE_FREQ + time * UNDULATE_SPEED) * UNDULATE_AMP;
		// Secondary faster, smaller wave for complexity
		double w2 = Math.sin(angleRad * UNDULATE_FREQ2 - time * UNDULATE_SPEED2) * UNDULATE_AMP2;
		// Tertiary ultra-slow global throb (all segments expand/contract together)
		double throb = Math.sin(time * 0.04) * 0.035;
		return (float) (w1 + w2 + throb);
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Vein branches — tapered quads that sprout inward from the ring
	// ════════════════════════════════════════════════════════════════════════

	private static void drawVeins(VertexConsumer glowVC, VertexConsumer coreVC, Matrix4f mat,
			float baseRadius, float time,
			float cR, float cG, float cB, float cA,
			float gR, float gG, float gB) {

		// Veins are evenly spaced but their angular position crawls slowly over time
		double crawlOffset = time * VEIN_CRAWL_SPEED;

		for (int v = 0; v < VEIN_COUNT; v++) {
			double baseAngle = crawlOffset + (Math.PI * 2.0 / VEIN_COUNT) * v;

			// Each vein has a slightly different length and curve based on its index
			// Use a deterministic hash-like value from the vein index for variety
			double veinSeed = hashVein(v);
			float length = VEIN_LENGTH * (0.6f + 0.4f * (float) veinSeed);

			// Vein pulsing — individual veins throb at slightly offset phases
			double veinPulse = (Math.sin(time * 0.1 + v * 1.7) + 1.0) * 0.5;
			float veinAlpha = (float) (0.3 + 0.5 * veinPulse);

			// Slight angular wander as it grows inward (gives curve)
			double curvature = Math.sin(veinSeed * 17.3) * 0.35;

			for (int s = 0; s < VEIN_SEGS; s++) {
				float t0 = (float) s / VEIN_SEGS;
				float t1 = (float) (s + 1) / VEIN_SEGS;

				// Radius decreases inward from the ring
				float rad0 = baseRadius - t0 * length + undulation(baseAngle, time);
				float rad1 = baseRadius - t1 * length + undulation(baseAngle, time);

				// Angular position curves slightly
				double ang0 = baseAngle + curvature * t0 * t0;
				double ang1 = baseAngle + curvature * t1 * t1;

				// Width tapers from root to tip
				float w0 = VEIN_ROOT_WIDTH * (1.0f - t0 * 0.85f);
				float w1 = VEIN_ROOT_WIDTH * (1.0f - t1 * 0.85f);

				// Alpha fades toward the tip
				float a0 = veinAlpha * (1.0f - t0 * 0.6f);
				float a1 = veinAlpha * (1.0f - t1 * 0.9f);

				float cos0 = (float) Math.cos(ang0);
				float sin0 = (float) Math.sin(ang0);
				float cos1 = (float) Math.cos(ang1);
				float sin1 = (float) Math.sin(ang1);

				// Perpendicular direction for width (tangent to the circle)
				float px0 = (float) -Math.sin(ang0);
				float pz0 = (float) Math.cos(ang0);
				float px1 = (float) -Math.sin(ang1);
				float pz1 = (float) Math.cos(ang1);

				// Core vein quad
				emitQuad(coreVC, mat,
						cos0 * rad0 - px0 * w0, 0.001f, sin0 * rad0 - pz0 * w0, cR, cG, cB, a0 * cA,
						cos0 * rad0 + px0 * w0, 0.001f, sin0 * rad0 + pz0 * w0, cR, cG, cB, a0 * cA,
						cos1 * rad1 + px1 * w1, 0.001f, sin1 * rad1 + pz1 * w1, cR, cG, cB, a1 * cA,
						cos1 * rad1 - px1 * w1, 0.001f, sin1 * rad1 - pz1 * w1, cR, cG, cB, a1 * cA);

				// Glow halo around vein (wider, more transparent)
				float gw0 = w0 * 3.0f;
				float gw1 = w1 * 3.0f;
				float ga0 = a0 * 0.35f;
				float ga1 = a1 * 0.25f;

				emitQuad(glowVC, mat,
						cos0 * rad0 - px0 * gw0, 0.001f, sin0 * rad0 - pz0 * gw0, gR, gG, gB, 0f,
						cos0 * rad0 + px0 * gw0, 0.001f, sin0 * rad0 + pz0 * gw0, gR, gG, gB, 0f,
						cos1 * rad1 + px1 * gw1, 0.001f, sin1 * rad1 + pz1 * gw1, gR, gG, gB, 0f,
						cos1 * rad1 - px1 * gw1, 0.001f, sin1 * rad1 - pz1 * gw1, gR, gG, gB, 0f);

				// Inner part of glow (brighter core of the halo)
				emitQuad(glowVC, mat,
						cos0 * rad0 - px0 * gw0, 0.001f, sin0 * rad0 - pz0 * gw0, gR, gG, gB, 0f,
						cos0 * rad0 - px0 * w0, 0.001f, sin0 * rad0 - pz0 * w0, gR, gG, gB, ga0,
						cos1 * rad1 - px1 * w1, 0.001f, sin1 * rad1 - pz1 * w1, gR, gG, gB, ga1,
						cos1 * rad1 - px1 * gw1, 0.001f, sin1 * rad1 - pz1 * gw1, gR, gG, gB, 0f);

				emitQuad(glowVC, mat,
						cos0 * rad0 + px0 * w0, 0.001f, sin0 * rad0 + pz0 * w0, gR, gG, gB, ga0,
						cos0 * rad0 + px0 * gw0, 0.001f, sin0 * rad0 + pz0 * gw0, gR, gG, gB, 0f,
						cos1 * rad1 + px1 * gw1, 0.001f, sin1 * rad1 + pz1 * gw1, gR, gG, gB, 0f,
						cos1 * rad1 + px1 * w1, 0.001f, sin1 * rad1 + pz1 * w1, gR, gG, gB, ga1);
			}
		}
	}

	/** Deterministic pseudo-random 0..1 value for a given vein index. */
	private static double hashVein(int index) {
		// Simple golden-ratio hash
		return (index * 0.6180339887) % 1.0;
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Quad helper
	// ════════════════════════════════════════════════════════════════════════

	private static void emitQuad(VertexConsumer vc, Matrix4f mat,
			float x1, float y1, float z1, float r1, float g1, float b1, float a1,
			float x2, float y2, float z2, float r2, float g2, float b2, float a2,
			float x3, float y3, float z3, float r3, float g3, float b3, float a3,
			float x4, float y4, float z4, float r4, float g4, float b4, float a4) {
		vc.vertex(mat, x1, y1, z1).color(r1, g1, b1, a1).endVertex();
		vc.vertex(mat, x2, y2, z2).color(r2, g2, b2, a2).endVertex();
		vc.vertex(mat, x3, y3, z3).color(r3, g3, b3, a3).endVertex();
		vc.vertex(mat, x4, y4, z4).color(r4, g4, b4, a4).endVertex();
	}
}

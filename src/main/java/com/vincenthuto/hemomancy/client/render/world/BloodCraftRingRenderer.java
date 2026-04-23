package com.vincenthuto.hemomancy.client.render.world;

import java.util.List;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveBloodCraftClientData;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Renders a collapsing, undulating bloody ring when a blood structure
 * recipe is activated. Very similar to {@link CardinalRiteBoundaryRenderer}
 * but the ring starts 2 blocks wider than the structure and collapses
 * inward to the center over the animation duration, then fades out.
 */
public class BloodCraftRingRenderer {

	// ── Ring geometry ──
	private static final int SEGMENTS = 72;
	private static final float CORE_WIDTH = 0.10f;
	private static final float GLOW_WIDTH = 0.28f;

	// ── Undulation parameters ──
	private static final double UNDULATE_FREQ = 7.0;
	private static final float UNDULATE_AMP = 0.10f;
	private static final double UNDULATE_SPEED = 0.07;
	private static final double UNDULATE_FREQ2 = 15.0;
	private static final float UNDULATE_AMP2 = 0.035f;
	private static final double UNDULATE_SPEED2 = 0.14;

	// ── Vein parameters ──
	private static final int VEIN_COUNT = 10;
	private static final int VEIN_SEGS = 5;
	private static final float VEIN_LENGTH = 0.5f;
	private static final float VEIN_ROOT_WIDTH = 0.035f;
	private static final double VEIN_CRAWL_SPEED = 0.02;

	public static void render(PoseStack poseStack, float partialTick) {
		List<ActiveBloodCraftClientData.CraftRingEntry> rings = ActiveBloodCraftClientData.getActiveRings();
		if (rings.isEmpty()) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		float currentTime = mc.level.getGameTime() + partialTick;
		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

		for (ActiveBloodCraftClientData.CraftRingEntry ring : rings) {
			drawCollapsingRing(poseStack, buffer, ring, currentTime, cam, partialTick);
		}

		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_CORE);
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_GLOW);
	}

	private static void drawCollapsingRing(PoseStack stack, MultiBufferSource buffer,
			ActiveBloodCraftClientData.CraftRingEntry ring, float currentTime, Vec3 cam, float partialTick) {

		BlockPos center = ring.getCenter();
		float startRadius = ring.getStartRadius();

		// Progress: 0 = just started (full radius), 1 = fully collapsed
		// Smooth out remaining ticks with partial tick for fluid animation
		float smoothRemaining = ring.getRemainingTicks() - partialTick;
		float smoothProgress = 1.0f - (smoothRemaining / ring.getTotalTicks());
		smoothProgress = Math.max(0f, Math.min(1f, smoothProgress));

		// Ease-in-out curve for smooth collapse
		float easedProgress = smoothProgress * smoothProgress * (3f - 2f * smoothProgress);

		// Radius collapses from startRadius → 0
		float currentRadius = startRadius * (1f - easedProgress);
		if (currentRadius < 0.05f) return; // too small to render

		// Fade out near the end
		float fadeAlpha = 1f;
		if (smoothProgress > 0.75f) {
			fadeAlpha = (1f - smoothProgress) / 0.25f;
		}

		double cx = center.getX() + 0.5;
		double cy = ring.getCenterY();
		double cz = center.getZ() + 0.5;

		// Breathing pulse
		double pulse = (Math.sin(currentTime * 0.12) + 1.0) * 0.5;
		float coreAlpha = (float) (0.70 + 0.30 * pulse) * fadeAlpha;
		float glowAlpha = (float) (0.18 + 0.18 * pulse) * fadeAlpha;

		// Intensify color as it collapses (brighter crimson → white-hot at end)
		float intensity = 1f + easedProgress * 0.5f;
		float coreR = Math.min(1f, (float) (0.90 + 0.10 * pulse) * intensity);
		float coreG = Math.min(1f, 0.05f * intensity);
		float coreB = Math.min(1f, 0.04f * intensity);
		float glowR = Math.min(1f, (float) (0.60 + 0.20 * pulse) * intensity);
		float glowG = 0.02f;
		float glowB = 0.02f;

		// Scale undulation amplitude down as ring shrinks
		float undulateScale = Math.max(0.2f, 1f - easedProgress * 0.7f);

		stack.pushPose();
		stack.translate(cx - cam.x, cy - cam.y, cz - cam.z);
		Matrix4f mat = stack.last().pose();

		VertexConsumer coreVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
		VertexConsumer glowVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);

		// ── Draw the undulating, collapsing ring ──
		for (int i = 0; i < SEGMENTS; i++) {
			double a1 = Math.toRadians((360.0 / SEGMENTS) * i);
			double a2 = Math.toRadians((360.0 / SEGMENTS) * (i + 1));

			float r1 = currentRadius + undulation(a1, currentTime) * undulateScale;
			float r2 = currentRadius + undulation(a2, currentTime) * undulateScale;

			// Subtle Y wave so it's not perfectly flat
			float y1 = (float) (Math.sin(a1 * 5.0 + currentTime * 0.08) * 0.015);
			float y2 = (float) (Math.sin(a2 * 5.0 + currentTime * 0.08) * 0.015);

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
		drawVeins(glowVC, coreVC, mat, currentRadius, currentTime, undulateScale,
				coreR, coreG, coreB, coreAlpha, glowR, glowG, glowB);

		stack.popPose();
	}

	// ═══════════════════════════════════════════════════════════════
	//  Undulation — layered sine waves
	// ═══════════════════════════════════════════════════════════════

	private static float undulation(double angleRad, float time) {
		double w1 = Math.sin(angleRad * UNDULATE_FREQ + time * UNDULATE_SPEED) * UNDULATE_AMP;
		double w2 = Math.sin(angleRad * UNDULATE_FREQ2 - time * UNDULATE_SPEED2) * UNDULATE_AMP2;
		double throb = Math.sin(time * 0.06) * 0.03;
		return (float) (w1 + w2 + throb);
	}

	// ═══════════════════════════════════════════════════════════════
	//  Vein branches — tapered quads sprouting inward from the ring
	// ═══════════════════════════════════════════════════════════════

	private static void drawVeins(VertexConsumer glowVC, VertexConsumer coreVC, Matrix4f mat,
			float baseRadius, float time, float undulateScale,
			float cR, float cG, float cB, float cA,
			float gR, float gG, float gB) {

		double crawlOffset = time * VEIN_CRAWL_SPEED;

		for (int v = 0; v < VEIN_COUNT; v++) {
			double baseAngle = crawlOffset + (Math.PI * 2.0 / VEIN_COUNT) * v;
			double veinSeed = hashVein(v);
			float length = VEIN_LENGTH * (0.5f + 0.5f * (float) veinSeed);

			// Scale vein length with the current radius
			float scaledLength = Math.min(length, baseRadius * 0.6f);
			if (scaledLength < 0.02f) continue;

			double veinPulse = (Math.sin(time * 0.12 + v * 1.7) + 1.0) * 0.5;
			float veinAlpha = (float) (0.3 + 0.5 * veinPulse);
			double curvature = Math.sin(veinSeed * 17.3) * 0.35;

			for (int s = 0; s < VEIN_SEGS; s++) {
				float t0 = (float) s / VEIN_SEGS;
				float t1 = (float) (s + 1) / VEIN_SEGS;

				float rad0 = baseRadius - t0 * scaledLength + undulation(baseAngle, time) * undulateScale;
				float rad1 = baseRadius - t1 * scaledLength + undulation(baseAngle, time) * undulateScale;

				double ang0 = baseAngle + curvature * t0 * t0;
				double ang1 = baseAngle + curvature * t1 * t1;

				float w0 = VEIN_ROOT_WIDTH * (1.0f - t0 * 0.85f);
				float w1 = VEIN_ROOT_WIDTH * (1.0f - t1 * 0.85f);

				float a0 = veinAlpha * (1.0f - t0 * 0.6f);
				float a1 = veinAlpha * (1.0f - t1 * 0.9f);

				float cos0 = (float) Math.cos(ang0);
				float sin0 = (float) Math.sin(ang0);
				float cos1 = (float) Math.cos(ang1);
				float sin1 = (float) Math.sin(ang1);

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

				// Glow halo
				float gw0 = w0 * 3.0f;
				float gw1 = w1 * 3.0f;
				float ga0 = a0 * 0.3f;
				float ga1 = a1 * 0.2f;

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

	private static double hashVein(int index) {
		return (index * 0.6180339887) % 1.0;
	}

	// ═══════════════════════════════════════════════════════════════
	//  Quad helper
	// ═══════════════════════════════════════════════════════════════

	private static void emitQuad(VertexConsumer vc, Matrix4f mat,
			float x1, float y1, float z1, float r1, float g1, float b1, float a1,
			float x2, float y2, float z2, float r2, float g2, float b2, float a2,
			float x3, float y3, float z3, float r3, float g3, float b3, float a3,
			float x4, float y4, float z4, float r4, float g4, float b4, float a4) {
		vc.addVertex(mat, x1, y1, z1).setColor(r1, g1, b1, a1);
		vc.addVertex(mat, x2, y2, z2).setColor(r2, g2, b2, a2);
		vc.addVertex(mat, x3, y3, z3).setColor(r3, g3, b3, a3);
		vc.addVertex(mat, x4, y4, z4).setColor(r4, g4, b4, a4);
	}
}

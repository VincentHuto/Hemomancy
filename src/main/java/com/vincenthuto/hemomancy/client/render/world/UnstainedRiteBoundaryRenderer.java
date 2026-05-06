package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Renders the boundary indicator for active Unstained cardinal rites.
 * Uses concentric hollow rhombuses (diamond shapes) that rotate slowly in
 * alternating directions. The number of rhombus layers matches the rite tier,
 * mirroring the ring-count logic of the Harbinger boundary renderer.
 * Colors shift from bright white on the innermost layer toward ice blue on the
 * outermost, giving a cold, crystalline feel distinct from the blood-red circles.
 */
public class UnstainedRiteBoundaryRenderer {

	// ── Ring geometry ──
	/** Width of the solid core band on each rhombus side. */
	private static final float CORE_WIDTH = 0.08f;
	/** Width of the translucent halo on each side of the core. */
	private static final float GLOW_WIDTH = 0.20f;

	// ── Rotation ──
	/** Radians per tick for the slowest (innermost) ring. */
	private static final float ROTATION_SPEED = 0.0030f;
	/** Additional angular stagger between successive rings (45°). */
	private static final double RING_ANGLE_STAGGER = Math.PI / 4.0;

	// ── Corner spike parameters ──
	/** How far the outward spike extends beyond the corner (blocks). */
	private static final float SPIKE_LENGTH = 0.40f;
	/** Half-width of the spike quad at its base. */
	private static final float SPIKE_BASE_HALF_WIDTH = 0.045f;
	/** Width multiplier for the spike's glow halo. */
	private static final float SPIKE_GLOW_MULT = 3.5f;

	// ── Y offset ──
	private static final float GROUND_Y = 0.065f;

	public static void render(PoseStack poseStack, float partialTick) {
		List<ActiveRiteClientData.RiteEntry> rites = ActiveRiteClientData.getActiveRites();
		if (rites.isEmpty()) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		float currentTime = mc.level.getGameTime() + partialTick;
		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

		// Two passes: glow first, then core — prevents buffer-flush issues in NeoForge
		VertexConsumer glowVC = buffer.getBuffer(RenderTypeInit.UNSTAINED_RITE_BOUNDARY_GLOW);
		for (ActiveRiteClientData.RiteEntry rite : rites) {
			if (!rite.isUnstained()) continue;
			drawRhombusLayers(poseStack, glowVC, null, rite, currentTime, cam);
		}
		buffer.endBatch(RenderTypeInit.UNSTAINED_RITE_BOUNDARY_GLOW);

		VertexConsumer coreVC = buffer.getBuffer(RenderTypeInit.UNSTAINED_RITE_BOUNDARY_CORE);
		for (ActiveRiteClientData.RiteEntry rite : rites) {
			if (!rite.isUnstained()) continue;
			drawRhombusLayers(poseStack, null, coreVC, rite, currentTime, cam);
		}
		buffer.endBatch(RenderTypeInit.UNSTAINED_RITE_BOUNDARY_CORE);
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Per-rite: iterate rings, translate to world position
	// ════════════════════════════════════════════════════════════════════════

	private static void drawRhombusLayers(PoseStack stack, VertexConsumer glowVC, VertexConsumer coreVC,
			ActiveRiteClientData.RiteEntry rite, float currentTime, Vec3 cam) {

		BlockPos center = rite.getCenter();
		float baseRadius = (float) (rite.getRiteSize() / 2.0 + 1.0);
		double cx = center.getX() + 0.5;
		double cy = center.getY() + GROUND_Y;
		double cz = center.getZ() + 0.5;

		// Same tier→ring-count formula as the Harbinger renderer
		int riteTier = (rite.getRiteSize() - 1) / 2;
		int ringCount = Math.max(1, riteTier);

		double pulse = (Math.sin(currentTime * 0.07) + 1.0) * 0.5;

		stack.pushPose();
		stack.translate(cx - cam.x, cy - cam.y, cz - cam.z);
		Matrix4f mat = stack.last().pose();

		for (int ring = 0; ring < ringCount; ring++) {
			float ringRadius = baseRadius + ring * 2.0f;
			float dirSign = (ring % 2 == 0) ? 1.0f : -1.0f;

			// Stagger rings by 45° so they don't sit on top of each other at t=0
			float rotation = (float) (ring * RING_ANGLE_STAGGER + currentTime * ROTATION_SPEED * dirSign);

			float ringFade = 1.0f - ring * 0.14f;

			// Inner ring: bright white.  Outer rings: cool ice-blue shift.
			float blueShift = (ringCount > 1) ? (float) ring / (ringCount - 1) : 0f;
			float coreR = 1.0f  - blueShift * 0.30f;
			float coreG = 1.0f  - blueShift * 0.14f;
			float coreB = 1.0f;

			float glowR = 0.50f - blueShift * 0.08f;
			float glowG = 0.75f - blueShift * 0.05f;
			float glowB = 1.0f;

			float coreAlpha = (float) (0.70 + 0.30 * pulse) * ringFade;
			float glowAlpha = (float) (0.13 + 0.10 * pulse) * ringFade;

			drawRhombus(glowVC, coreVC, mat, ringRadius, rotation,
					coreR, coreG, coreB, coreAlpha,
					glowR, glowG, glowB, glowAlpha,
					currentTime);
		}

		stack.popPose();
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Single hollow rhombus: 4 sides + 4 corner spikes
	// ════════════════════════════════════════════════════════════════════════

	private static void drawRhombus(VertexConsumer glowVC, VertexConsumer coreVC, Matrix4f mat,
			float radius, float rotation,
			float cR, float cG, float cB, float cA,
			float gR, float gG, float gB, float gA,
			float time) {

		// 4 corners, evenly spaced at 90° intervals, offset by current rotation
		float[] cx = new float[4];
		float[] cz = new float[4];
		for (int i = 0; i < 4; i++) {
			double angle = rotation + i * (Math.PI / 2.0);
			cx[i] = (float) (radius * Math.cos(angle));
			cz[i] = (float) (radius * Math.sin(angle));
		}

		// 4 sides: corner[i] → corner[(i+1)%4]
		for (int i = 0; i < 4; i++) {
			int next = (i + 1) % 4;
			float sx = cx[i], sz = cz[i];
			float ex = cx[next], ez = cz[next];

			// Direction along the side (unit vector in XZ)
			float dx = ex - sx;
			float dz = ez - sz;
			float len = (float) Math.sqrt(dx * dx + dz * dz);
			dx /= len;
			dz /= len;

			// Perpendicular, oriented outward from centre
			float px = -dz;
			float pz = dx;
			float midX = (sx + ex) * 0.5f;
			float midZ = (sz + ez) * 0.5f;
			if (px * midX + pz * midZ < 0f) { px = -px; pz = -pz; }

			float cHalf = CORE_WIDTH * 0.5f;

			if (glowVC != null) {
				// Inner glow: from (core inner edge, opaque) → (inner edge, transparent)
				emitQuad(glowVC, mat,
						sx - px * (cHalf + GLOW_WIDTH), 0f, sz - pz * (cHalf + GLOW_WIDTH), gR, gG, gB, 0f,
						sx - px * cHalf,                0f, sz - pz * cHalf,                gR, gG, gB, gA,
						ex - px * cHalf,                0f, ez - pz * cHalf,                gR, gG, gB, gA,
						ex - px * (cHalf + GLOW_WIDTH), 0f, ez - pz * (cHalf + GLOW_WIDTH), gR, gG, gB, 0f);

				// Outer glow: from (core outer edge, opaque) → (outer edge, transparent)
				emitQuad(glowVC, mat,
						sx + px * cHalf,                0f, sz + pz * cHalf,                gR, gG, gB, gA,
						sx + px * (cHalf + GLOW_WIDTH), 0f, sz + pz * (cHalf + GLOW_WIDTH), gR, gG, gB, 0f,
						ex + px * (cHalf + GLOW_WIDTH), 0f, ez + pz * (cHalf + GLOW_WIDTH), gR, gG, gB, 0f,
						ex + px * cHalf,                0f, ez + pz * cHalf,                gR, gG, gB, gA);
			}

			if (coreVC != null) {
				emitQuad(coreVC, mat,
						sx - px * cHalf, 0f, sz - pz * cHalf, cR, cG, cB, cA,
						sx + px * cHalf, 0f, sz + pz * cHalf, cR, cG, cB, cA,
						ex + px * cHalf, 0f, ez + pz * cHalf, cR, cG, cB, cA,
						ex - px * cHalf, 0f, ez - pz * cHalf, cR, cG, cB, cA);
			}
		}

		// Corner spikes: thin outward-pointing quads at each of the 4 corners
		for (int i = 0; i < 4; i++) {
			double cornerAngle = rotation + i * (Math.PI / 2.0);
			float radialX = (float) Math.cos(cornerAngle);
			float radialZ = (float) Math.sin(cornerAngle);
			// Perpendicular to the radial (tangent direction at corner)
			float tangX = -radialZ;
			float tangZ = radialX;

			float baseX = cx[i];
			float baseZ = cz[i];
			float tipX  = baseX + radialX * SPIKE_LENGTH;
			float tipZ  = baseZ + radialZ * SPIKE_LENGTH;

			float spikeAlpha = (float) (0.55 + 0.35 * ((Math.sin(time * 0.11 + i * 1.57) + 1.0) * 0.5));

			if (glowVC != null) {
				float gw = SPIKE_BASE_HALF_WIDTH * SPIKE_GLOW_MULT;
				// Glow halo around the spike — wide at base, tapers to zero
				emitQuad(glowVC, mat,
						baseX - tangX * gw, 0f, baseZ - tangZ * gw, gR, gG, gB, 0f,
						baseX + tangX * gw, 0f, baseZ + tangZ * gw, gR, gG, gB, 0f,
						tipX,               0f, tipZ,               gR, gG, gB, 0f,
						tipX,               0f, tipZ,               gR, gG, gB, 0f);

				emitQuad(glowVC, mat,
						baseX - tangX * SPIKE_BASE_HALF_WIDTH, 0f, baseZ - tangZ * SPIKE_BASE_HALF_WIDTH,
						gR, gG, gB, spikeAlpha * gA / cA,
						baseX + tangX * SPIKE_BASE_HALF_WIDTH, 0f, baseZ + tangZ * SPIKE_BASE_HALF_WIDTH,
						gR, gG, gB, spikeAlpha * gA / cA,
						tipX, 0f, tipZ, gR, gG, gB, 0f,
						tipX, 0f, tipZ, gR, gG, gB, 0f);
			}

			if (coreVC != null) {
				// Core spike: solid at base, vanishes at tip
				emitQuad(coreVC, mat,
						baseX - tangX * SPIKE_BASE_HALF_WIDTH, 0f, baseZ - tangZ * SPIKE_BASE_HALF_WIDTH,
						cR, cG, cB, cA * spikeAlpha,
						baseX + tangX * SPIKE_BASE_HALF_WIDTH, 0f, baseZ + tangZ * SPIKE_BASE_HALF_WIDTH,
						cR, cG, cB, cA * spikeAlpha,
						tipX, 0f, tipZ, cR, cG, cB, 0f,
						tipX, 0f, tipZ, cR, cG, cB, 0f);
			}
		}
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Quad helper
	// ════════════════════════════════════════════════════════════════════════

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

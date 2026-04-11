package com.vincenthuto.hemomancy.client.render.world;

import java.util.List;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.QliphothBloomClientData;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Renders the Qliphoth Bloom — a large, blooming black and red tree-like
 * structure made of rendered geometry (not particles), surrounded by pulsing
 * red rings similar to the cardinal rite boundary renderer.
 * <p>
 * The tree grows from the ground at the rite center. Its trunk is dark/black,
 * branches spread outward and upward with red-tipped foliage rendered as
 * glowing quad geometry. Pulsing concentric rings radiate outward from the base.
 */
public class QliphothBloomRenderer {

	// ── Tree geometry parameters ──
	/** Total height of the tree in blocks. */
	private static final float TREE_HEIGHT = 8.0f;
	/** Trunk base radius. */
	private static final float TRUNK_BASE_RADIUS = 0.45f;
	/** Trunk top radius. */
	private static final float TRUNK_TOP_RADIUS = 0.15f;
	/** Number of vertical segments for the trunk. */
	private static final int TRUNK_SEGMENTS_V = 12;
	/** Number of circumference segments for the trunk. */
	private static final int TRUNK_SEGMENTS_H = 10;
	/** Number of major branches. */
	private static final int BRANCH_COUNT = 7;
	/** Number of segments per branch. */
	private static final int BRANCH_SEGS = 6;
	/** Branch maximum length. */
	private static final float BRANCH_LENGTH = 3.5f;
	/** Canopy bloom sphere count. */
	private static final int CANOPY_BLOOMS = 14;
	/** Canopy bloom radius. */
	private static final float BLOOM_RADIUS = 1.2f;
	/** Root count spreading on the ground. */
	private static final int ROOT_COUNT = 8;

	// ── Pulsing ring parameters ──
	/** Number of concentric rings. */
	private static final int RING_COUNT = 3;
	/** Ring segment count. */
	private static final int RING_SEGMENTS = 72;
	/** Ring core width. */
	private static final float RING_CORE_WIDTH = 0.08f;
	/** Ring glow width. */
	private static final float RING_GLOW_WIDTH = 0.20f;
	/** Maximum ring radius in blocks. */
	private static final float RING_MAX_RADIUS = 6.0f;
	/** How fast rings pulse outward. */
	private static final double RING_PULSE_SPEED = 0.03;

	public static void render(PoseStack poseStack, float partialTick) {
		List<QliphothBloomClientData.BloomEntry> blooms = QliphothBloomClientData.getActiveBlooms();
		if (blooms.isEmpty()) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		float currentTime = mc.level.getGameTime() + partialTick;
		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

		for (QliphothBloomClientData.BloomEntry bloom : blooms) {
			drawQliphothTree(poseStack, buffer, bloom, currentTime, cam);
			drawPulsingRings(poseStack, buffer, bloom, currentTime, cam);
		}

		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_CORE);
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_GLOW);
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Tree Rendering — trunk, branches, canopy, roots
	// ════════════════════════════════════════════════════════════════════════

	private static void drawQliphothTree(PoseStack stack, MultiBufferSource buffer,
			QliphothBloomClientData.BloomEntry bloom, float time, Vec3 cam) {

		BlockPos center = bloom.getCenter();
		double cx = center.getX() + 0.5;
		double cy = center.getY() + 0.1;
		double cz = center.getZ() + 0.5;

		stack.pushPose();
		stack.translate(cx - cam.x, cy - cam.y, cz - cam.z);
		Matrix4f mat = stack.last().pose();

		VertexConsumer coreVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
		VertexConsumer glowVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);

		// Global breathing pulse
		double pulse = (Math.sin(time * 0.06) + 1.0) * 0.5;

		drawTrunk(coreVC, glowVC, mat, time, pulse);
		drawRoots(coreVC, glowVC, mat, time, pulse);
		drawBranches(coreVC, glowVC, mat, time, pulse);
		drawCanopy(coreVC, glowVC, mat, time, pulse);

		stack.popPose();
	}

	/**
	 * Draws the main trunk — a tapered column of dark quads with subtle red veining.
	 */
	private static void drawTrunk(VertexConsumer coreVC, VertexConsumer glowVC,
			Matrix4f mat, float time, double pulse) {

		for (int v = 0; v < TRUNK_SEGMENTS_V; v++) {
			float t0 = (float) v / TRUNK_SEGMENTS_V;
			float t1 = (float) (v + 1) / TRUNK_SEGMENTS_V;

			float y0 = t0 * TREE_HEIGHT;
			float y1 = t1 * TREE_HEIGHT;

			float r0 = TRUNK_BASE_RADIUS * (1.0f - t0 * 0.7f);
			float r1 = TRUNK_BASE_RADIUS * (1.0f - t1 * 0.7f);

			// Subtle sway
			float swayX0 = (float) (Math.sin(time * 0.02 + t0 * 2.0) * 0.05 * t0);
			float swayZ0 = (float) (Math.cos(time * 0.025 + t0 * 1.5) * 0.04 * t0);
			float swayX1 = (float) (Math.sin(time * 0.02 + t1 * 2.0) * 0.05 * t1);
			float swayZ1 = (float) (Math.cos(time * 0.025 + t1 * 1.5) * 0.04 * t1);

			for (int h = 0; h < TRUNK_SEGMENTS_H; h++) {
				double a1 = Math.toRadians((360.0 / TRUNK_SEGMENTS_H) * h);
				double a2 = Math.toRadians((360.0 / TRUNK_SEGMENTS_H) * (h + 1));

				float cos1 = (float) Math.cos(a1);
				float sin1 = (float) Math.sin(a1);
				float cos2 = (float) Math.cos(a2);
				float sin2 = (float) Math.sin(a2);

				// Dark trunk color (near-black with subtle dark red)
				float trunkR = (float) (0.08 + 0.03 * pulse);
				float trunkG = 0.02f;
				float trunkB = 0.02f;
				float trunkA = 0.90f;

				// Red vein pulsing along trunk
				float veinPulse = (float) (Math.sin(time * 0.08 + h * 1.3 + v * 0.5) + 1.0) * 0.5f;
				float veinR = (float) (0.35 + 0.25 * veinPulse * pulse);
				float veinG = 0.02f;
				float veinB = 0.02f;
				float veinA = 0.15f * veinPulse;

				// Core trunk quad
				emitQuad(coreVC, mat,
						cos1 * r0 + swayX0, y0, sin1 * r0 + swayZ0, trunkR, trunkG, trunkB, trunkA,
						cos2 * r0 + swayX0, y0, sin2 * r0 + swayZ0, trunkR, trunkG, trunkB, trunkA,
						cos2 * r1 + swayX1, y1, sin2 * r1 + swayZ1, trunkR, trunkG, trunkB, trunkA,
						cos1 * r1 + swayX1, y1, sin1 * r1 + swayZ1, trunkR, trunkG, trunkB, trunkA);

				// Glow overlay for veins
				emitQuad(glowVC, mat,
						cos1 * (r0 + 0.02f) + swayX0, y0, sin1 * (r0 + 0.02f) + swayZ0, veinR, veinG, veinB, veinA,
						cos2 * (r0 + 0.02f) + swayX0, y0, sin2 * (r0 + 0.02f) + swayZ0, veinR, veinG, veinB, veinA,
						cos2 * (r1 + 0.02f) + swayX1, y1, sin2 * (r1 + 0.02f) + swayZ1, veinR, veinG, veinB, veinA,
						cos1 * (r1 + 0.02f) + swayX1, y1, sin1 * (r1 + 0.02f) + swayZ1, veinR, veinG, veinB, veinA);
			}
		}
	}

	/**
	 * Draws surface roots that spread outward from the trunk base.
	 */
	private static void drawRoots(VertexConsumer coreVC, VertexConsumer glowVC,
			Matrix4f mat, float time, double pulse) {

		for (int i = 0; i < ROOT_COUNT; i++) {
			double baseAngle = (Math.PI * 2.0 / ROOT_COUNT) * i;
			double seed = hashIndex(i) * 0.5 + 0.5;
			float rootLength = (float) (1.5 + seed * 1.5);
			double curvature = Math.sin(seed * 13.7) * 0.3;

			for (int s = 0; s < 5; s++) {
				float t0 = (float) s / 5;
				float t1 = (float) (s + 1) / 5;

				float rad0 = TRUNK_BASE_RADIUS + t0 * rootLength;
				float rad1 = TRUNK_BASE_RADIUS + t1 * rootLength;

				double ang0 = baseAngle + curvature * t0 * t0;
				double ang1 = baseAngle + curvature * t1 * t1;

				float y0 = (float) Math.max(-0.3, -0.08 - t0 * 0.15);
				float y1 = (float) Math.max(-0.3, -0.08 - t1 * 0.15);

				float w0 = 0.08f * (1.0f - t0 * 0.7f);
				float w1 = 0.08f * (1.0f - t1 * 0.7f);

				float rootR = (float) (0.06 + 0.04 * pulse);
				float rootG = 0.01f;
				float rootB = 0.01f;
				float rootA = 0.85f * (1.0f - t0 * 0.4f);

				float cos0 = (float) Math.cos(ang0);
				float sin0 = (float) Math.sin(ang0);
				float cos1 = (float) Math.cos(ang1);
				float sin1 = (float) Math.sin(ang1);

				float px0 = (float) -Math.sin(ang0);
				float pz0 = (float) Math.cos(ang0);
				float px1 = (float) -Math.sin(ang1);
				float pz1 = (float) Math.cos(ang1);

				emitQuad(coreVC, mat,
						cos0 * rad0 - px0 * w0, y0, sin0 * rad0 - pz0 * w0, rootR, rootG, rootB, rootA,
						cos0 * rad0 + px0 * w0, y0, sin0 * rad0 + pz0 * w0, rootR, rootG, rootB, rootA,
						cos1 * rad1 + px1 * w1, y1, sin1 * rad1 + pz1 * w1, rootR, rootG, rootB, rootA * 0.8f,
						cos1 * rad1 - px1 * w1, y1, sin1 * rad1 - pz1 * w1, rootR, rootG, rootB, rootA * 0.8f);
			}
		}
	}

	/**
	 * Draws branches that spread from the upper trunk, curving upward and outward.
	 */
	private static void drawBranches(VertexConsumer coreVC, VertexConsumer glowVC,
			Matrix4f mat, float time, double pulse) {

		for (int b = 0; b < BRANCH_COUNT; b++) {
			double baseAngle = (Math.PI * 2.0 / BRANCH_COUNT) * b;
			double seed = hashIndex(b + 100);

			// Branches start between 40%-80% up the trunk
			float startHeight = TREE_HEIGHT * (0.4f + (float) seed * 0.4f);
			float branchLen = BRANCH_LENGTH * (0.6f + (float) seed * 0.4f);

			// Initial direction: outward and upward
			float elevAngle = (float) (0.3 + seed * 0.5); // radians from horizontal

			for (int s = 0; s < BRANCH_SEGS; s++) {
				float t0 = (float) s / BRANCH_SEGS;
				float t1 = (float) (s + 1) / BRANCH_SEGS;

				// Branch curves upward more as it extends
				float curveUp = t0 * t0 * 0.6f;

				float outDist0 = t0 * branchLen * (float) Math.cos(elevAngle);
				float outDist1 = t1 * branchLen * (float) Math.cos(elevAngle);

				float yOff0 = t0 * branchLen * (float) Math.sin(elevAngle) + curveUp;
				float yOff1 = t1 * branchLen * (float) Math.sin(elevAngle) + t1 * t1 * 0.6f;

				// Subtle sway
				float swayX = (float) (Math.sin(time * 0.015 + b * 1.5) * 0.03 * t0);
				float swayZ = (float) (Math.cos(time * 0.02 + b * 2.1) * 0.025 * t0);

				float cos0 = (float) Math.cos(baseAngle);
				float sin0 = (float) Math.sin(baseAngle);

				float x0 = cos0 * outDist0 + swayX;
				float z0 = sin0 * outDist0 + swayZ;
				float x1 = cos0 * outDist1 + swayX;
				float z1 = sin0 * outDist1 + swayZ;

				float branchY0 = startHeight + yOff0;
				float branchY1 = startHeight + yOff1;

				// Branch width tapers
				float w0 = 0.06f * (1.0f - t0 * 0.75f);
				float w1 = 0.06f * (1.0f - t1 * 0.75f);

				// Dark branch color
				float brR = (float) (0.06 + 0.03 * pulse);
				float brG = 0.01f;
				float brB = 0.01f;
				float brA = 0.85f * (1.0f - t0 * 0.3f);

				// Perpendicular direction
				float px = (float) -Math.sin(baseAngle);
				float pz = (float) Math.cos(baseAngle);

				emitQuad(coreVC, mat,
						x0 - px * w0, branchY0, z0 - pz * w0, brR, brG, brB, brA,
						x0 + px * w0, branchY0, z0 + pz * w0, brR, brG, brB, brA,
						x1 + px * w1, branchY1, z1 + pz * w1, brR, brG, brB, brA * 0.9f,
						x1 - px * w1, branchY1, z1 - pz * w1, brR, brG, brB, brA * 0.9f);

				// Red glow along branches
				float glowPulse = (float) (Math.sin(time * 0.07 + b * 1.2 + s * 0.8) + 1.0) * 0.5f;
				float gR = (float) (0.6 + 0.3 * glowPulse);
				float gA = 0.12f * glowPulse * (1.0f - t0 * 0.5f);

				emitQuad(glowVC, mat,
						x0 - px * w0 * 2, branchY0, z0 - pz * w0 * 2, gR, 0.02f, 0.02f, gA,
						x0 + px * w0 * 2, branchY0, z0 + pz * w0 * 2, gR, 0.02f, 0.02f, gA,
						x1 + px * w1 * 2, branchY1, z1 + pz * w1 * 2, gR, 0.02f, 0.02f, gA * 0.7f,
						x1 - px * w1 * 2, branchY1, z1 - pz * w1 * 2, gR, 0.02f, 0.02f, gA * 0.7f);
			}
		}
	}

	/**
	 * Draws canopy blooms — clusters of red/dark red glowing diamond shapes
	 * at the ends of branches and around the crown, giving a "blooming" tree look.
	 */
	private static void drawCanopy(VertexConsumer coreVC, VertexConsumer glowVC,
			Matrix4f mat, float time, double pulse) {

		for (int i = 0; i < CANOPY_BLOOMS; i++) {
			double seed = hashIndex(i + 200);
			double seed2 = hashIndex(i + 300);

			// Position blooms in a spherical shell around the tree crown
			double azimuth = (Math.PI * 2.0 / CANOPY_BLOOMS) * i + seed * 0.5;
			float elevation = TREE_HEIGHT * 0.55f + (float) seed * TREE_HEIGHT * 0.5f;
			float outward = BLOOM_RADIUS + (float) seed2 * 1.5f;

			float bx = (float) (Math.cos(azimuth) * outward);
			float bz = (float) (Math.sin(azimuth) * outward);
			float by = elevation;

			// Subtle bob
			by += (float) (Math.sin(time * 0.04 + i * 1.3) * 0.08);
			bx += (float) (Math.sin(time * 0.02 + i * 0.7) * 0.05);

			// Bloom size varies
			float bloomSize = 0.25f + (float) seed * 0.35f;

			// Bloom pulse — each bloom throbs individually
			float bloomPulse = (float) ((Math.sin(time * 0.08 + i * 2.1) + 1.0) * 0.5);
			float alpha = (float) (0.5 + 0.4 * bloomPulse);

			// Red/dark-red bloom color with variation
			float coreR = (float) (0.65 + 0.35 * bloomPulse);
			float coreG = (float) (0.02 + 0.03 * seed);
			float coreB = (float) (0.02 + 0.02 * seed);

			// Draw diamond-shaped bloom (4 triangles as quads)
			// Top
			emitQuad(coreVC, mat,
					bx - bloomSize * 0.5f, by, bz, coreR, coreG, coreB, alpha * 0.7f,
					bx, by + bloomSize, bz, coreR, coreG, coreB, alpha,
					bx + bloomSize * 0.5f, by, bz, coreR, coreG, coreB, alpha * 0.7f,
					bx, by, bz - bloomSize * 0.3f, coreR, coreG, coreB, alpha * 0.5f);

			// Bottom
			emitQuad(coreVC, mat,
					bx - bloomSize * 0.5f, by, bz, coreR, coreG, coreB, alpha * 0.7f,
					bx, by - bloomSize * 0.6f, bz, coreR * 0.5f, coreG, coreB, alpha * 0.5f,
					bx + bloomSize * 0.5f, by, bz, coreR, coreG, coreB, alpha * 0.7f,
					bx, by, bz + bloomSize * 0.3f, coreR, coreG, coreB, alpha * 0.5f);

			// Glow halo around each bloom
			float glowSize = bloomSize * 2.0f;
			float gA = alpha * 0.2f;
			float gR = (float) (0.5 + 0.3 * bloomPulse);

			emitQuad(glowVC, mat,
					bx - glowSize, by, bz, gR, 0.01f, 0.01f, 0f,
					bx, by + glowSize, bz, gR, 0.01f, 0.01f, gA,
					bx + glowSize, by, bz, gR, 0.01f, 0.01f, 0f,
					bx, by - glowSize, bz, gR, 0.01f, 0.01f, 0f);
		}
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Pulsing Rings — concentric red rings radiating from the tree base
	// ════════════════════════════════════════════════════════════════════════

	private static void drawPulsingRings(PoseStack stack, MultiBufferSource buffer,
			QliphothBloomClientData.BloomEntry bloom, float time, Vec3 cam) {

		BlockPos center = bloom.getCenter();
		double cx = center.getX() + 0.5;
		double cy = center.getY() + 0.08;
		double cz = center.getZ() + 0.5;

		stack.pushPose();
		stack.translate(cx - cam.x, cy - cam.y, cz - cam.z);
		Matrix4f mat = stack.last().pose();

		VertexConsumer coreVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
		VertexConsumer glowVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);

		double pulse = (Math.sin(time * 0.08) + 1.0) * 0.5;

		for (int ring = 0; ring < RING_COUNT; ring++) {
			// Each ring pulses outward at a different phase, creating a radiating effect
			double phase = (time * RING_PULSE_SPEED + ring * (1.0 / RING_COUNT)) % 1.0;
			float ringRadius = (float) (1.5 + phase * RING_MAX_RADIUS);

			// Rings fade as they expand outward
			float fadeProgress = (float) phase;
			float fadeAlpha = 1.0f - fadeProgress * fadeProgress; // quadratic fade

			if (fadeAlpha < 0.02f) continue;

			float coreAlpha = (float) (0.55 + 0.25 * pulse) * fadeAlpha;
			float glowAlpha = (float) (0.12 + 0.10 * pulse) * fadeAlpha;

			// Red core color
			float coreR = (float) Math.min(1.0, 0.85 + 0.15 * pulse);
			float coreG = 0.04f;
			float coreB = 0.03f;
			float glowR = (float) Math.min(1.0, 0.55 + 0.2 * pulse);
			float glowG = 0.02f;
			float glowB = 0.02f;

			// Undulation for organic feel
			for (int i = 0; i < RING_SEGMENTS; i++) {
				double a1 = Math.toRadians((360.0 / RING_SEGMENTS) * i);
				double a2 = Math.toRadians((360.0 / RING_SEGMENTS) * (i + 1));

				float undulate1 = (float) (Math.sin(a1 * 6.0 + time * 0.05 + ring * 2.0) * 0.08 * (1.0 - fadeProgress * 0.5));
				float undulate2 = (float) (Math.sin(a2 * 6.0 + time * 0.05 + ring * 2.0) * 0.08 * (1.0 - fadeProgress * 0.5));

				float r1 = ringRadius + undulate1;
				float r2 = ringRadius + undulate2;

				float y1 = (float) (Math.sin(a1 * 4.0 + time * 0.06) * 0.01);
				float y2 = (float) (Math.sin(a2 * 4.0 + time * 0.06) * 0.01);

				float cos1 = (float) Math.cos(a1);
				float sin1 = (float) Math.sin(a1);
				float cos2 = (float) Math.cos(a2);
				float sin2 = (float) Math.sin(a2);

				float iGlow1 = r1 - RING_GLOW_WIDTH - RING_CORE_WIDTH * 0.5f;
				float iCore1 = r1 - RING_CORE_WIDTH * 0.5f;
				float oCore1 = r1 + RING_CORE_WIDTH * 0.5f;
				float oGlow1 = r1 + RING_GLOW_WIDTH + RING_CORE_WIDTH * 0.5f;

				float iGlow2 = r2 - RING_GLOW_WIDTH - RING_CORE_WIDTH * 0.5f;
				float iCore2 = r2 - RING_CORE_WIDTH * 0.5f;
				float oCore2 = r2 + RING_CORE_WIDTH * 0.5f;
				float oGlow2 = r2 + RING_GLOW_WIDTH + RING_CORE_WIDTH * 0.5f;

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
		}

		stack.popPose();
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Helpers
	// ════════════════════════════════════════════════════════════════════════

	/** Golden ratio conjugate — used for deterministic pseudo-random distribution of geometry. */
	private static final double GOLDEN_RATIO_CONJUGATE = 0.6180339887;

	/** Deterministic pseudo-random 0..1 value for a given index. */
	private static double hashIndex(int index) {
		return ((index * GOLDEN_RATIO_CONJUGATE + 0.3) % 1.0 + 1.0) % 1.0;
	}

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

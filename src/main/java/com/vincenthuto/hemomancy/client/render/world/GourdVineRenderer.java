package com.vincenthuto.hemomancy.client.render.world;

import java.util.List;
import java.util.Set;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * Renders a growing vine effect rising from the ground during gourd vessel
 * Cardinal Rites (Pallid, Crimson, and Ashen Vessel). Vines emerge from the
 * earth as tendrils of dark-brown-to-green organic matter, growing taller as
 * the rite progresses. Rendered as flat POSITION_COLOR quads with depth
 * testing, reusing the existing rite boundary render types.
 */
public class GourdVineRenderer {

	// ── Vessel rite recipe paths ──
	private static final Set<String> VESSEL_RITE_PATHS = Set.of(
			"cardinal_rite/pallid_vessel_rite",
			"cardinal_rite/crimson_vessel_rite",
			"cardinal_rite/ashen_vessel_rite");

	// ── Vine layout ──
	/** Number of vine tendrils per rite. */
	private static final int VINE_COUNT = 20;
	/** Quad segments per vine (higher = smoother curve). */
	private static final int VINE_SEGS = 10;
	/** Maximum height a fully-grown vine reaches (in blocks). */
	private static final float VINE_MAX_HEIGHT = 2.8f;
	/** Base width at the root of a vine. */
	private static final float VINE_ROOT_WIDTH = 0.055f;
	/** Fraction of the rite radius used when scattering vine roots. */
	private static final float SCATTER_FRACTION = 0.85f;

	// ── Sway parameters ──
	private static final double SWAY_FREQ = 1.4;
	private static final float SWAY_AMP = 0.18f;
	private static final double SWAY_SPEED = 0.05;

	// ── Colors (base → tip) ──
	// Base: dark earthy brown
	private static final float BASE_R = 0.28f;
	private static final float BASE_G = 0.16f;
	private static final float BASE_B = 0.07f;
	// Tip: forest green
	private static final float TIP_R = 0.18f;
	private static final float TIP_G = 0.52f;
	private static final float TIP_B = 0.12f;
	// Glow tint (soft green haze)
	private static final float GLOW_R = 0.15f;
	private static final float GLOW_G = 0.45f;
	private static final float GLOW_B = 0.10f;

	// ──────────────────────────────────────────────────────────────────────────

	public static void render(PoseStack poseStack, float partialTick) {
		List<ActiveRiteClientData.RiteEntry> rites = ActiveRiteClientData.getActiveRites();
		if (rites.isEmpty()) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		float currentTime = mc.level.getGameTime() + partialTick;
		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

		for (ActiveRiteClientData.RiteEntry rite : rites) {
			if (!isVesselRite(rite.getRecipeId())) continue;
			drawVines(poseStack, buffer, rite, currentTime, cam);
		}

		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_CORE);
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_GLOW);
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Per-rite vine rendering
	// ════════════════════════════════════════════════════════════════════════

	private static void drawVines(PoseStack stack, MultiBufferSource buffer,
			ActiveRiteClientData.RiteEntry rite, float time, Vec3 cam) {

		BlockPos center = rite.getCenter();
		float halfSize = (float) (rite.getRiteSize() / 2.0);
		float scatterRadius = halfSize * SCATTER_FRACTION;

		double cx = center.getX() + 0.5;
		double cy = center.getY();   // ground level
		double cz = center.getZ() + 0.5;

		// progress ∈ [0,1]: how far the rite has advanced
		float progress = (float) Math.min(1.0, rite.getProgress());

		stack.pushPose();
		stack.translate(cx - cam.x, cy - cam.y, cz - cam.z);
		Matrix4f mat = stack.last().pose();

		VertexConsumer coreVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
		VertexConsumer glowVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);

		for (int v = 0; v < VINE_COUNT; v++) {
			// Deterministic per-vine offsets
			double h1 = hash(v * 3);
			double h2 = hash(v * 3 + 1);
			double h3 = hash(v * 3 + 2);

			// Root position scattered within the rite area (polar coordinates)
			double angle = h1 * Math.PI * 2.0;
			double dist = Math.sqrt(h2) * scatterRadius;    // square root for uniform distribution
			float rootX = (float) (Math.cos(angle) * dist);
			float rootZ = (float) (Math.sin(angle) * dist);

			// Individual vine properties
			float lengthMult = 0.55f + 0.45f * (float) h3;
			float vineHeight = VINE_MAX_HEIGHT * lengthMult * progress;

			// Vine pulsing alpha (each vine breathes at its own phase)
			double vinePulse = (Math.sin(time * 0.08 + v * 2.3) + 1.0) * 0.5;
			float vineAlpha = (float) (0.55 + 0.35 * vinePulse);

			// Sway direction (each vine leans in a unique direction)
			double swayAngle = h2 * Math.PI * 2.0;
			float swayDirX = (float) Math.cos(swayAngle);
			float swayDirZ = (float) Math.sin(swayAngle);

			drawSingleVine(coreVC, glowVC, mat, rootX, rootZ,
					vineHeight, swayDirX, swayDirZ,
					vineAlpha, time, v);
		}

		stack.popPose();
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Single vine: tapered quad strip rising upward with sway
	// ════════════════════════════════════════════════════════════════════════

	private static void drawSingleVine(VertexConsumer coreVC, VertexConsumer glowVC, Matrix4f mat,
			float rootX, float rootZ,
			float vineHeight, float swayDirX, float swayDirZ,
			float vineAlpha, float time, int vineIndex) {

		if (vineHeight <= 0.0001f) return;

		// Perpendicular direction (for giving width to the vine in the horizontal plane)
		// We'll use the right-angle of the sway direction
		float perpX = -swayDirZ;
		float perpZ = swayDirX;

		for (int s = 0; s < VINE_SEGS; s++) {
			float t0 = (float) s / VINE_SEGS;
			float t1 = (float) (s + 1) / VINE_SEGS;

			float y0 = t0 * vineHeight;
			float y1 = t1 * vineHeight;

			// Sway: horizontal displacement that increases with height
			double swayPhase = time * SWAY_SPEED + vineIndex * 0.87;
			float sway0 = (float) (Math.sin(t0 * SWAY_FREQ * Math.PI + swayPhase) * SWAY_AMP * t0 * t0);
			float sway1 = (float) (Math.sin(t1 * SWAY_FREQ * Math.PI + swayPhase) * SWAY_AMP * t1 * t1);

			float cx0 = rootX + swayDirX * sway0;
			float cz0 = rootZ + swayDirZ * sway0;
			float cx1 = rootX + swayDirX * sway1;
			float cz1 = rootZ + swayDirZ * sway1;

			// Width tapers from root to tip
			float w0 = VINE_ROOT_WIDTH * (1.0f - t0 * 0.80f);
			float w1 = VINE_ROOT_WIDTH * (1.0f - t1 * 0.80f);

			// Alpha: solid at root, fades near tip (especially the partially grown tip)
			float growFade0 = Math.min(1.0f, 1.0f - Math.max(0.0f, t0 - 0.85f) / 0.15f);
			float growFade1 = Math.min(1.0f, 1.0f - Math.max(0.0f, t1 - 0.85f) / 0.15f);
			float a0 = vineAlpha * (1.0f - t0 * 0.3f) * growFade0;
			float a1 = vineAlpha * (1.0f - t1 * 0.3f) * growFade1;

			// Color: lerp from brown (base) to green (tip)
			float coreR0 = lerp(BASE_R, TIP_R, t0);
			float coreG0 = lerp(BASE_G, TIP_G, t0);
			float coreB0 = lerp(BASE_B, TIP_B, t0);
			float coreR1 = lerp(BASE_R, TIP_R, t1);
			float coreG1 = lerp(BASE_G, TIP_G, t1);
			float coreB1 = lerp(BASE_B, TIP_B, t1);

			// Core quad (the solid vine body)
			emitQuad(coreVC, mat,
					cx0 - perpX * w0, y0, cz0 - perpZ * w0, coreR0, coreG0, coreB0, a0,
					cx0 + perpX * w0, y0, cz0 + perpZ * w0, coreR0, coreG0, coreB0, a0,
					cx1 + perpX * w1, y1, cz1 + perpZ * w1, coreR1, coreG1, coreB1, a1,
					cx1 - perpX * w1, y1, cz1 - perpZ * w1, coreR1, coreG1, coreB1, a1);

			// Glow halo (wider, dimmer)
			float gw0 = w0 * 2.8f;
			float gw1 = w1 * 2.8f;
			float ga0 = a0 * 0.30f;
			float ga1 = a1 * 0.22f;

			emitQuad(glowVC, mat,
					cx0 - perpX * gw0, y0, cz0 - perpZ * gw0, GLOW_R, GLOW_G, GLOW_B, 0f,
					cx0 - perpX * w0,  y0, cz0 - perpZ * w0,  GLOW_R, GLOW_G, GLOW_B, ga0,
					cx1 - perpX * w1,  y1, cz1 - perpZ * w1,  GLOW_R, GLOW_G, GLOW_B, ga1,
					cx1 - perpX * gw1, y1, cz1 - perpZ * gw1, GLOW_R, GLOW_G, GLOW_B, 0f);

			emitQuad(glowVC, mat,
					cx0 + perpX * w0,  y0, cz0 + perpZ * w0,  GLOW_R, GLOW_G, GLOW_B, ga0,
					cx0 + perpX * gw0, y0, cz0 + perpZ * gw0, GLOW_R, GLOW_G, GLOW_B, 0f,
					cx1 + perpX * gw1, y1, cz1 + perpZ * gw1, GLOW_R, GLOW_G, GLOW_B, 0f,
					cx1 + perpX * w1,  y1, cz1 + perpZ * w1,  GLOW_R, GLOW_G, GLOW_B, ga1);
		}
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Helpers
	// ════════════════════════════════════════════════════════════════════════

	private static boolean isVesselRite(ResourceLocation recipeId) {
		if (recipeId == null) return false;
		return VESSEL_RITE_PATHS.contains(recipeId.getPath());
	}

	/** Deterministic pseudo-random 0..1 value for a given seed integer. */
	private static double hash(int seed) {
		return ((seed * 1664525 + 1013904223) & 0x7FFFFFFF) / (double) 0x7FFFFFFF;
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
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

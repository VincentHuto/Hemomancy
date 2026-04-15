package com.vincenthuto.hemomancy.client.render.world;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.BloodBallClientData;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

/**
 * Renders the cosmetic Sanguine Blob orb when the player holds a {@code sanguine_blob} item
 * and has reached Initiatory Degree 3.
 *
 * <p>The sphere is built from latitude / longitude quad-bands.  Each vertex radius is perturbed
 * by layered sine waves to give the surface an organic, sloshing liquid feel.  The overall ball
 * pulses gently in size.  When dropped it fades out as it falls under gravity.</p>
 *
 * <p><b>Squish on impact:</b> ~20 ticks after being dropped a damped sinusoidal Y-compression
 * / XZ-expansion plays out, simulating the ball hitting the ground and recovering.</p>
 *
 * <p><b>Stretch while swinging:</b> while following the player the sphere is elongated along
 * its velocity direction (and correspondingly squeezed perpendicular) — the faster the ball
 * moves, the more it stretches.</p>
 *
 * <p>Uses the same POSITION_COLOR render types as {@link BloodCraftRingRenderer} so it
 * integrates cleanly with the existing pipeline (LEQUAL depth, lightning shader).</p>
 */
public class BloodBallRenderer {

	// ── Sphere geometry ──
	private static final int LAT_BANDS = 16;
	private static final int LON_BANDS = 24;
	private static final float BASE_RADIUS = 0.35f;
	private static final float GLOW_EXTRA = 0.08f;

	// ── Surface undulation — layered sine waves ──
	private static final float A1 = 0.05f;
	private static final double N1 = 3.0;
	private static final double W1 = 0.08;

	private static final float A2 = 0.03f;
	private static final double N2 = 5.0;
	private static final double W2 = 0.05;

	private static final float A3 = 0.02f;
	private static final double W3 = 0.11;

	// ── Global pulse ──
	private static final float PULSE_BASE = 0.95f;
	private static final float PULSE_AMP = 0.05f;
	private static final double PULSE_SPEED = 0.07;

	// ── Squish on impact ──
	/** Maximum Y-axis compression at the peak of the squish animation. */
	private static final float SQUISH_AMP = 0.45f;

	// ── Stretch while swinging ──
	/**
	 * Multiplier mapping ball speed (blocks/tick) to stretch factor.
	 * At speed 0.1 b/t the stretch is 1 + 5 × 0.1 = 1.5×; capped at 3.0× overall.
	 */
	private static final float STRETCH_K = 5.0f;
	private static final float STRETCH_MAX = 3.0f;

	// ── Core colour (deep crimson) ──
	private static final float CORE_R = 0.85f;
	private static final float CORE_G = 0.04f;
	private static final float CORE_B = 0.04f;
	private static final float CORE_A = 0.88f;

	// ── Glow colour (translucent dark red) ──
	private static final float GLOW_R = 0.55f;
	private static final float GLOW_G = 0.02f;
	private static final float GLOW_B = 0.02f;
	private static final float GLOW_A = 0.18f;

	// ─────────────────────────────────────────────────────────────────────
	//  Entry point
	// ─────────────────────────────────────────────────────────────────────

	public static void render(PoseStack poseStack, float partialTick) {
		Vec3 pos = BloodBallClientData.getPosition();
		Vec3 prevPos = BloodBallClientData.getPrevPosition();
		if (pos == null) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		float currentTime = mc.level.getGameTime() + partialTick;

		// Interpolate between previous and current position for smooth motion
		Vec3 renderPos = prevPos != null ? prevPos.lerp(pos, partialTick) : pos;
		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

		// Fade alpha when dropped
		float fadeAlpha = 1.0f;
		if (BloodBallClientData.isDropped()) {
			fadeAlpha = (float) BloodBallClientData.getFadeTicks() / BloodBallClientData.MAX_FADE_TICKS;
			fadeAlpha = Math.max(0f, Math.min(1f, fadeAlpha));
		}

		float jiggle = BloodBallClientData.getJigglePhase();
		float pulse = PULSE_BASE + PULSE_AMP * (float) Math.sin(currentTime * PULSE_SPEED);

		// ── Squish-on-impact: Y compression + XZ expansion ──────────────────
		int squishTicks = BloodBallClientData.getSquishTicks();
		float squishYScale = 1f;
		float squishXZScale = 1f;
		if (squishTicks > 0) {
			// t runs 0→1 over the animation; sin(π·t) peaks at the midpoint
			float t = 1f - (float) squishTicks / BloodBallClientData.MAX_SQUISH_TICKS;
			float squishAmt = SQUISH_AMP * (float) Math.sin(Math.PI * t);
			squishYScale = Math.max(0.05f, 1f - squishAmt);
			// Volume-preserving: XZ expands to compensate Y compression
			squishXZScale = (float) (1.0 / Math.sqrt(squishYScale));
		}

		// ── Stretch-while-swinging: elongate along velocity direction ─────
		Vec3 swingDelta = BloodBallClientData.getSwingDelta();
		double swingSpeed = swingDelta.length();
		float stretchFactor = Math.min(STRETCH_MAX, 1f + STRETCH_K * (float) swingSpeed);
		// Normalised direction; fall back to up-axis if nearly stationary
		float sdX = 0f, sdY = 1f, sdZ = 0f;
		if (swingSpeed > 1e-4) {
			sdX = (float) (swingDelta.x / swingSpeed);
			sdY = (float) (swingDelta.y / swingSpeed);
			sdZ = (float) (swingDelta.z / swingSpeed);
		}

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

		poseStack.pushPose();
		poseStack.translate(renderPos.x - cam.x, renderPos.y - cam.y, renderPos.z - cam.z);
		// Apply squish scale (always Y-axis, simulates ground impact)
		if (squishTicks > 0) {
			poseStack.scale(squishXZScale, squishYScale, squishXZScale);
		}
		Matrix4f mat = poseStack.last().pose();

		VertexConsumer coreVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
		VertexConsumer glowVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);

		renderSphere(coreVC, mat, BASE_RADIUS * pulse, currentTime, jiggle,
				CORE_R, CORE_G, CORE_B, CORE_A * fadeAlpha,
				sdX, sdY, sdZ, stretchFactor);
		renderSphere(glowVC, mat, (BASE_RADIUS + GLOW_EXTRA) * pulse, currentTime, jiggle,
				GLOW_R, GLOW_G, GLOW_B, GLOW_A * fadeAlpha,
				sdX, sdY, sdZ, stretchFactor);

		poseStack.popPose();

		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_CORE);
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_GLOW);
	}

	// ─────────────────────────────────────────────────────────────────────
	//  Sphere geometry
	// ─────────────────────────────────────────────────────────────────────

	/**
	 * Emits a full sphere as latitude×longitude quad strips.
	 * Each vertex's radius is slightly perturbed by {@link #undulation} so the
	 * surface looks organic rather than perfectly smooth.
	 *
	 * <p>The sphere is additionally stretched along the direction {@code (dX, dY, dZ)} by
	 * {@code stretchFactor}, with an inverse-sqrt squeeze applied perpendicular to that axis
	 * to keep the volume roughly constant — this produces the motion-stretch effect.</p>
	 *
	 * @param stretchFactor  1.0 = sphere; > 1.0 = elongated along (dX,dY,dZ)
	 */
	private static void renderSphere(VertexConsumer vc, Matrix4f mat,
			float baseRadius, float time, float jiggle,
			float r, float g, float b, float a,
			float dX, float dY, float dZ, float stretchFactor) {

		// Perpendicular squeeze to keep volume roughly constant
		float squeeze = (stretchFactor > 0.01f)
				? (float) (1.0 / Math.sqrt(stretchFactor))
				: 1f;

		for (int lat = 0; lat < LAT_BANDS; lat++) {
			double theta0 = Math.PI * lat / LAT_BANDS;
			double theta1 = Math.PI * (lat + 1) / LAT_BANDS;

			double sinT0 = Math.sin(theta0);
			double cosT0 = Math.cos(theta0);
			double sinT1 = Math.sin(theta1);
			double cosT1 = Math.cos(theta1);

			for (int lon = 0; lon < LON_BANDS; lon++) {
				double phi0 = 2.0 * Math.PI * lon / LON_BANDS;
				double phi1 = 2.0 * Math.PI * (lon + 1) / LON_BANDS;

				double cosP0 = Math.cos(phi0);
				double sinP0 = Math.sin(phi0);
				double cosP1 = Math.cos(phi1);
				double sinP1 = Math.sin(phi1);

				float r00 = baseRadius + undulation(theta0, phi0, time, jiggle);
				float r10 = baseRadius + undulation(theta1, phi0, time, jiggle);
				float r11 = baseRadius + undulation(theta1, phi1, time, jiggle);
				float r01 = baseRadius + undulation(theta0, phi1, time, jiggle);

				// Raw sphere corners
				float x00 = (float) (sinT0 * cosP0) * r00;
				float y00 = (float) cosT0 * r00;
				float z00 = (float) (sinT0 * sinP0) * r00;

				float x10 = (float) (sinT1 * cosP0) * r10;
				float y10 = (float) cosT1 * r10;
				float z10 = (float) (sinT1 * sinP0) * r10;

				float x11 = (float) (sinT1 * cosP1) * r11;
				float y11 = (float) cosT1 * r11;
				float z11 = (float) (sinT1 * sinP1) * r11;

				float x01 = (float) (sinT0 * cosP1) * r01;
				float y01 = (float) cosT0 * r01;
				float z01 = (float) (sinT0 * sinP1) * r01;

				// Apply directional stretch — decompose each vertex into the component
				// along the swing direction and the perpendicular, scale each independently.
				float[] v00 = applyStretch(x00, y00, z00, dX, dY, dZ, stretchFactor, squeeze);
				float[] v10 = applyStretch(x10, y10, z10, dX, dY, dZ, stretchFactor, squeeze);
				float[] v11 = applyStretch(x11, y11, z11, dX, dY, dZ, stretchFactor, squeeze);
				float[] v01 = applyStretch(x01, y01, z01, dX, dY, dZ, stretchFactor, squeeze);

				vc.vertex(mat, v00[0], v00[1], v00[2]).color(r, g, b, a).endVertex();
				vc.vertex(mat, v10[0], v10[1], v10[2]).color(r, g, b, a).endVertex();
				vc.vertex(mat, v11[0], v11[1], v11[2]).color(r, g, b, a).endVertex();
				vc.vertex(mat, v01[0], v01[1], v01[2]).color(r, g, b, a).endVertex();
			}
		}
	}

	/**
	 * Decomposes vertex {@code (x, y, z)} into a component along the normalised
	 * direction {@code (dX, dY, dZ)} and a perpendicular remainder, then scales each
	 * by {@code stretchFactor} and {@code squeeze} respectively.
	 *
	 * @return a three-element float array {@code [rx, ry, rz]}.
	 */
	private static float[] applyStretch(float x, float y, float z,
			float dX, float dY, float dZ,
			float stretchFactor, float squeeze) {
		// Dot product with the (normalised) direction
		float dot = x * dX + y * dY + z * dZ;
		// v_along = dot · dir,  v_perp = v - v_along
		float alX = dot * dX, alY = dot * dY, alZ = dot * dZ;
		float pX = x - alX,   pY = y - alY,   pZ = z - alZ;
		return new float[]{
			alX * stretchFactor + pX * squeeze,
			alY * stretchFactor + pY * squeeze,
			alZ * stretchFactor + pZ * squeeze
		};
	}

	// ─────────────────────────────────────────────────────────────────────
	//  Surface undulation — layered sine waves
	// ─────────────────────────────────────────────────────────────────────

	/**
	 * Computes a radial displacement for a given surface point (theta, phi) at time t.
	 * Three sine waves at different frequencies and speeds are superimposed to produce
	 * an organic, sloshing liquid effect.
	 *
	 * @param theta   latitude angle (0–π)
	 * @param phi     longitude angle (0–2π)
	 * @param time    current game time + partial tick
	 * @param jiggle  continuously advancing jiggle phase from {@link BloodBallClientData}
	 * @return signed radial offset in blocks
	 */
	private static float undulation(double theta, double phi, float time, float jiggle) {
		double w1 = A1 * Math.sin(N1 * theta + W1 * time + jiggle);
		double w2 = A2 * Math.cos(N2 * phi + W2 * time);
		double w3 = A3 * Math.sin(W3 * time);
		return (float) (w1 + w2 + w3);
	}
}

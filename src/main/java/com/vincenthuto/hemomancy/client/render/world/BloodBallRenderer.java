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

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

		poseStack.pushPose();
		poseStack.translate(renderPos.x - cam.x, renderPos.y - cam.y, renderPos.z - cam.z);
		Matrix4f mat = poseStack.last().pose();

		VertexConsumer coreVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
		VertexConsumer glowVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);

		renderSphere(coreVC, mat, BASE_RADIUS * pulse, currentTime, jiggle,
				CORE_R, CORE_G, CORE_B, CORE_A * fadeAlpha);
		renderSphere(glowVC, mat, (BASE_RADIUS + GLOW_EXTRA) * pulse, currentTime, jiggle,
				GLOW_R, GLOW_G, GLOW_B, GLOW_A * fadeAlpha);

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
	 */
	private static void renderSphere(VertexConsumer vc, Matrix4f mat,
			float baseRadius, float time, float jiggle,
			float r, float g, float b, float a) {

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

				// Four corners of this quad (theta0/lon0, theta1/lon0, theta1/lon1, theta0/lon1)
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

				vc.vertex(mat, x00, y00, z00).color(r, g, b, a).endVertex();
				vc.vertex(mat, x10, y10, z10).color(r, g, b, a).endVertex();
				vc.vertex(mat, x11, y11, z11).color(r, g, b, a).endVertex();
				vc.vertex(mat, x01, y01, z01).color(r, g, b, a).endVertex();
			}
		}
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

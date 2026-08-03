package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.functional.SanguineConduitBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix4f;

/**
 * BER for the placed Sanguine Conduit.
 *
 * <p>Renders two subtle effects centered on the conduit: a small hovering blood
 * core and, more importantly, dim crimson oath-rings that repeatedly expand
 * outward along the ground. The previous implementation only rendered the core,
 * so the advertised outward pulse never actually existed.</p>
 */
public class SanguineConduitBlockRenderer implements BlockEntityRenderer<SanguineConduitBlockEntity> {

	// ── Central orb ──
	private static final int LAT_BANDS = 16;
	private static final int LON_BANDS = 24;
	private static final float BASE_RADIUS = 0.15f;
	private static final float GLOW_EXTRA = 0.04f;

	private static final float A1 = 0.08f;
	private static final double N1 = 3.0;
	private static final double W1 = 0.06;
	private static final float A2 = 0.018f;
	private static final double N2 = 7.0;
	private static final double W2 = 0.3;
	private static final float A3 = 0.020f;
	private static final double W3 = 0.3;

	private static final float PULSE_BASE = 2f;
	private static final float PULSE_AMP = 0.12f;
	private static final double PULSE_SPEED = 0.08;

	// ── Expanding ring-wave pulses ──
	private static final int RING_SEGMENTS = 72;
	private static final int RING_COUNT = 3;
	private static final float RING_START_RADIUS = 0.55f;
	private static final float RING_MAX_RADIUS = -1.5f;
	private static final float RING_CORE_WIDTH = 0.07f;
	private static final float RING_GLOW_WIDTH = 0.24f;
	private static final double RING_PULSE_SPEED = 0.0075;
	private static final float RING_BASE_Y = 0.5f;
	private static final double RING_ROTATION_SPEED = 1.04; // radians per tick

	private static final int PHASE_SEED_MASK = 0xFFF;

	// ── Shared crimson palette ──
	private static final float CORE_R = 0.85f;
	private static final float CORE_G = 0.04f;
	private static final float CORE_B = 0.04f;
	private static final float GLOW_R = 0.60f;
	private static final float GLOW_G = 0.02f;
	private static final float GLOW_B = 0.02f;

	public SanguineConduitBlockRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(SanguineConduitBlockEntity be, float partialTick, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return;
		}

		double time = mc.level.getGameTime() + partialTick;
		long seed = be.getBlockPos().asLong();
		float phaseOffset = (float) ((seed & PHASE_SEED_MASK) / (double) PHASE_SEED_MASK);
		float phaseRadians = (float) (phaseOffset * Math.PI * 2.0);

		renderCentralCore(poseStack, buffer, time, phaseRadians);
		renderOutwardPulses(poseStack, buffer, time, phaseOffset);
	}

	private static void renderCentralCore(PoseStack poseStack, MultiBufferSource buffer, double time, float phaseRadians) {
		poseStack.pushPose();
		poseStack.translate(0.5, 0.53, 0.5);

		float pulseSin = (float) Math.sin(time * PULSE_SPEED + phaseRadians);
		float pulse = PULSE_BASE + PULSE_AMP * pulseSin;
		Matrix4f mat = poseStack.last().pose();
		VertexConsumer vc = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);

		renderSphere(vc, mat, BASE_RADIUS * pulse, time, phaseRadians,
				CORE_R, CORE_G, CORE_B, 0.82f * (0.85f + 0.15f * pulseSin));
		renderSphere(vc, mat, (BASE_RADIUS + GLOW_EXTRA) * pulse, time, phaseRadians,
				GLOW_R, GLOW_G, GLOW_B, 0.26f * (0.80f + 0.20f * pulseSin));

		poseStack.popPose();
	}

	private static void renderOutwardPulses(PoseStack poseStack, MultiBufferSource buffer, double time, float phaseOffset) {
		poseStack.pushPose();
		poseStack.translate(0.5, 0.065, 0.5);

		Matrix4f mat = poseStack.last().pose();
		renderRingPass(buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), mat, time, phaseOffset, true);
		renderRingPass(buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), mat, time, phaseOffset, false);

		poseStack.popPose();
	}

	private static void renderRingPass(VertexConsumer vc, Matrix4f mat, double time, float phaseOffset, boolean glowPass) {
		double globalPulse = (Math.sin(time * 0.08 + phaseOffset * Math.PI * 2.0) + 1.0) * 0.5;

		for (int ring = 0; ring < RING_COUNT; ring++) {
			double phase = (time * RING_PULSE_SPEED + phaseOffset + ring * (1.0 / RING_COUNT)) % 1.0;
			float fadeProgress = (float) phase;
			float fadeAlpha = 1.0f - fadeProgress * fadeProgress;
			if (fadeAlpha < 0.03f) {
				continue;
			}

			float ringRadius = RING_START_RADIUS + fadeProgress * RING_MAX_RADIUS;
			float amplitudeScale = 1.0f - fadeProgress * 0.45f;
			float ringPulse = (float) ((Math.sin(time * 0.11 + ring * 1.7 + phaseOffset * Math.PI * 2.0) + 1.0) * 0.5);

			float coreAlpha = (float) (0.42 + 0.22 * globalPulse) * fadeAlpha;
			float glowAlpha = (float) (0.18 + 0.12 * ringPulse) * fadeAlpha;
			float coreR = Math.min(1.0f, (float) (CORE_R + 0.12 * globalPulse));
			float glowR = Math.min(1.0f, (float) (GLOW_R + 0.18 * globalPulse));

			// Each ring rotates at its own speed (alternating directions for visual interest)
			double rotationDirection = (ring % 2 == 0) ? 1.0 : -1.0;
			double ringRotation = time * RING_ROTATION_SPEED * (1.0 + ring * 0.15) * rotationDirection;

			for (int i = 0; i < RING_SEGMENTS; i++) {
				double a1 = Math.toRadians((360.0 / RING_SEGMENTS) * i) + ringRotation;
				double a2 = Math.toRadians((360.0 / RING_SEGMENTS) * (i + 1)) + ringRotation;

				float r1 = ringRadius + ringUndulation(a1, time, ring, phaseOffset) * amplitudeScale;
				float r2 = ringRadius + ringUndulation(a2, time, ring, phaseOffset) * amplitudeScale;

				float y1 = RING_BASE_Y + (float) (Math.sin(a1 * 4.0 + time * 0.05 + ring) * 0.008);
				float y2 = RING_BASE_Y + (float) (Math.sin(a2 * 4.0 + time * 0.05 + ring) * 0.008);

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

				if (glowPass) {
					emitQuad(vc, mat,
							cos1 * iGlow1, y1, sin1 * iGlow1, glowR, GLOW_G, GLOW_B, 0f,
							cos1 * iCore1, y1, sin1 * iCore1, glowR, GLOW_G, GLOW_B, glowAlpha,
							cos2 * iCore2, y2, sin2 * iCore2, glowR, GLOW_G, GLOW_B, glowAlpha,
							cos2 * iGlow2, y2, sin2 * iGlow2, glowR, GLOW_G, GLOW_B, 0f);

					emitQuad(vc, mat,
							cos1 * oCore1, y1, sin1 * oCore1, glowR, GLOW_G, GLOW_B, glowAlpha,
							cos1 * oGlow1, y1, sin1 * oGlow1, glowR, GLOW_G, GLOW_B, 0f,
							cos2 * oGlow2, y2, sin2 * oGlow2, glowR, GLOW_G, GLOW_B, 0f,
							cos2 * oCore2, y2, sin2 * oCore2, glowR, GLOW_G, GLOW_B, glowAlpha);
				} else {
					emitQuad(vc, mat,
							cos1 * iCore1, y1, sin1 * iCore1, coreR, CORE_G, CORE_B, coreAlpha,
							cos1 * oCore1, y1, sin1 * oCore1, coreR, CORE_G, CORE_B, coreAlpha,
							cos2 * oCore2, y2, sin2 * oCore2, coreR, CORE_G, CORE_B, coreAlpha,
							cos2 * iCore2, y2, sin2 * iCore2, coreR, CORE_G, CORE_B, coreAlpha);
				}
			}
		}
	}

	private static float ringUndulation(double angle, double time, int ring, float phaseOffset) {
		double w1 = Math.sin(angle * 6.0 + time * 0.05 + ring * 1.9 + phaseOffset * Math.PI * 2.0) * 0.055;
		double w2 = Math.sin(angle * 13.0 - time * 0.09 - ring * 0.8 + phaseOffset * 3.0) * 0.020;
		double w3 = Math.sin(time * 0.04 + ring * 0.7) * 0.012;
		return (float) (w1 + w2 + w3);
	}

	private static void renderSphere(VertexConsumer vc, Matrix4f mat,
			float baseRadius, double time, float phaseRadians,
			float r, float g, float b, float a) {
		for (int lat = 0; lat < LAT_BANDS; lat++) {
			double theta0 = Math.PI * lat / LAT_BANDS;
			double theta1 = Math.PI * (lat + 1) / LAT_BANDS;
			double sinT0 = Math.sin(theta0), cosT0 = Math.cos(theta0);
			double sinT1 = Math.sin(theta1), cosT1 = Math.cos(theta1);

			for (int lon = 0; lon < LON_BANDS; lon++) {
				double phi0 = 2.0 * Math.PI * lon / LON_BANDS;
				double phi1 = 2.0 * Math.PI * (lon + 1) / LON_BANDS;
				double cosP0 = Math.cos(phi0), sinP0 = Math.sin(phi0);
				double cosP1 = Math.cos(phi1), sinP1 = Math.sin(phi1);

				float r00 = baseRadius + undulation(theta0, phi0, time, phaseRadians);
				float r10 = baseRadius + undulation(theta1, phi0, time, phaseRadians);
				float r11 = baseRadius + undulation(theta1, phi1, time, phaseRadians);
				float r01 = baseRadius + undulation(theta0, phi1, time, phaseRadians);

				vc.addVertex(mat,
						(float) (sinT0 * cosP0) * r00, (float) cosT0 * r00, (float) (sinT0 * sinP0) * r00)
						.setColor(r, g, b, a);
				vc.addVertex(mat,
						(float) (sinT1 * cosP0) * r10, (float) cosT1 * r10, (float) (sinT1 * sinP0) * r10)
						.setColor(r, g, b, a);
				vc.addVertex(mat,
						(float) (sinT1 * cosP1) * r11, (float) cosT1 * r11, (float) (sinT1 * sinP1) * r11)
						.setColor(r, g, b, a);
				vc.addVertex(mat,
						(float) (sinT0 * cosP1) * r01, (float) cosT0 * r01, (float) (sinT0 * sinP1) * r01)
						.setColor(r, g, b, a);
			}
		}
	}

	private static float undulation(double theta, double phi, double time, float phaseRadians) {
		double w1 = A1 * Math.sin(N1 * theta + W1 * time + phaseRadians);
		double w2 = A2 * Math.cos(N2 * phi + W2 * time);
		double w3 = A3 * Math.sin(W3 * time);
		return (float) (w1 + w2 + w3);
	}

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

	@Override
	public int getViewDistance() {
		return 64;
	}

	@Override
	public boolean shouldRenderOffScreen(SanguineConduitBlockEntity be) {
		return false;
	}
}

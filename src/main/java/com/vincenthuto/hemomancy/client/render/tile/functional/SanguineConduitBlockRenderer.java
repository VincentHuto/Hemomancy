package com.vincenthuto.hemomancy.client.render.tile.functional;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.functional.SanguineConduitBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Block entity renderer for the {@link SanguineConduitBlockEntity}.
 *
 * <p>Renders a stationary glowing pulsing ball of living blood hovering at the
 * center of the block.  The sphere uses the same latitude/longitude quad-band
 * geometry and layered-sine undulation as
 * {@link com.vincenthuto.hemomancy.client.render.world.BloodBallRenderer}, but
 * without any motion, drop, squish, or stretch behaviour — it simply sits and
 * breathes.</p>
 *
 * <p>Two passes are drawn (core then glow) using
 * {@link RenderTypeInit#RITE_BOUNDARY_CORE} and
 * {@link RenderTypeInit#RITE_BOUNDARY_GLOW} so the ball integrates cleanly with
 * the existing translucent pipeline.</p>
 */
public class SanguineConduitBlockRenderer implements BlockEntityRenderer<SanguineConduitBlockEntity> {

	// ── Sphere geometry ──
	private static final int LAT_BANDS = 16;
	private static final int LON_BANDS = 24;
	private static final float BASE_RADIUS = 0.22f;
	private static final float GLOW_EXTRA = 0.06f;

	// ── Surface undulation — layered sine waves ──
	private static final float A1 = 0.04f;
	private static final double N1 = 3.0;
	private static final double W1 = 0.06;

	private static final float A2 = 0.025f;
	private static final double N2 = 5.0;
	private static final double W2 = 0.04;

	private static final float A3 = 0.015f;
	private static final double W3 = 0.09;

	/** Bit-mask used to extract a 12-bit sub-value from a block-pos seed,
	 *  producing a per-conduit phase offset in the range [0, 2π). */
	private static final int PHASE_SEED_MASK = 0xFFF;

	// ── Global pulse ──
	private static final float PULSE_BASE = 0.92f;
	private static final float PULSE_AMP  = 0.08f;
	private static final double PULSE_SPEED = 0.05;

	// ── Core colour (deep crimson) ──
	private static final float CORE_R = 0.85f;
	private static final float CORE_G = 0.04f;
	private static final float CORE_B = 0.04f;
	private static final float CORE_A = 0.90f;

	// ── Glow colour (translucent dark red) ──
	private static final float GLOW_R = 0.55f;
	private static final float GLOW_G = 0.02f;
	private static final float GLOW_B = 0.02f;
	private static final float GLOW_A = 0.22f;

	public SanguineConduitBlockRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(SanguineConduitBlockEntity be, float partialTick, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		float time = mc.level.getGameTime() + partialTick;

		// A slow per-block jiggle phase so nearby conduits don't pulse in sync
		long seed = be.getBlockPos().asLong();
		float jiggle = (float) ((seed & PHASE_SEED_MASK) * (Math.PI * 2.0 / PHASE_SEED_MASK));

		poseStack.pushPose();
		// Center the ball in the block, slightly above center so it appears to hover
		poseStack.translate(0.5, 0.55, 0.5);

		float pulse = PULSE_BASE + PULSE_AMP * (float) Math.sin(time * PULSE_SPEED + jiggle);
		Matrix4f mat = poseStack.last().pose();

		VertexConsumer coreVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
		renderSphere(coreVC, mat, BASE_RADIUS * pulse, time, jiggle,
				CORE_R, CORE_G, CORE_B, CORE_A);

		VertexConsumer glowVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);
		renderSphere(glowVC, mat, (BASE_RADIUS + GLOW_EXTRA) * pulse, time, jiggle,
				GLOW_R, GLOW_G, GLOW_B, GLOW_A);

		poseStack.popPose();
	}

	// ── Sphere geometry ──────────────────────────────────────────────────────────

	private static void renderSphere(VertexConsumer vc, Matrix4f mat,
			float baseRadius, float time, float jiggle,
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

				float r00 = baseRadius + undulation(theta0, phi0, time, jiggle);
				float r10 = baseRadius + undulation(theta1, phi0, time, jiggle);
				float r11 = baseRadius + undulation(theta1, phi1, time, jiggle);
				float r01 = baseRadius + undulation(theta0, phi1, time, jiggle);

				vc.addVertex(mat,
						(float)(sinT0 * cosP0) * r00, (float)cosT0 * r00, (float)(sinT0 * sinP0) * r00)
						.setColor(r, g, b, a);
				vc.addVertex(mat,
						(float)(sinT1 * cosP0) * r10, (float)cosT1 * r10, (float)(sinT1 * sinP0) * r10)
						.setColor(r, g, b, a);
				vc.addVertex(mat,
						(float)(sinT1 * cosP1) * r11, (float)cosT1 * r11, (float)(sinT1 * sinP1) * r11)
						.setColor(r, g, b, a);
				vc.addVertex(mat,
						(float)(sinT0 * cosP1) * r01, (float)cosT0 * r01, (float)(sinT0 * sinP1) * r01)
						.setColor(r, g, b, a);
			}
		}
	}

	private static float undulation(double theta, double phi, float time, float jiggle) {
		double w1 = A1 * Math.sin(N1 * theta + W1 * time + jiggle);
		double w2 = A2 * Math.cos(N2 * phi  + W2 * time);
		double w3 = A3 * Math.sin(W3 * time);
		return (float)(w1 + w2 + w3);
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

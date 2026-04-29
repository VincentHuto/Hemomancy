package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.functional.SanguineConduitBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import org.joml.Matrix4f;

/**
 * Block entity renderer for the {@link SanguineConduitBlockEntity}. Draws a
 * slow, dim pulsing ring that expands outward from the conduit's center —
 * visually suggesting the covenant reach of the anchor without the full
 * grandeur of a Qliphoth Bloom.
 * <p>
 * Uses the same geometry technique as {@link com.vincenthuto.hemomancy.client.render.world.QliphothBloomRenderer}
 * but with a single ring, slower pulse, and reduced opacity so the effect
 * reads as a quiet, persistent claim rather than an active rite.
 */
public class SanguineConduitBlockRenderer implements BlockEntityRenderer<SanguineConduitBlockEntity> {

	// ── Ring parameters (subdued compared to QliphothBloom) ──
	private static final int RING_SEGMENTS = 64;
	private static final float RING_CORE_WIDTH = 0.05f;
	private static final float RING_GLOW_WIDTH = 0.18f;
	/** The ring expands from 0.8 to RING_MAX_RADIUS and then resets. */
	private static final float RING_START_RADIUS = 0.8f;
	private static final float RING_MAX_RADIUS = 4.5f;
	/** Slower pulse speed than the Bloom (0.005 → 0.0018). */
	private static final double RING_PULSE_SPEED = 0.0018;

	public SanguineConduitBlockRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(SanguineConduitBlockEntity be, float partialTick, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		float time = mc.level.getGameTime() + partialTick;
		drawConduitRing(poseStack, buffer, time);
	}

	private static void drawConduitRing(PoseStack stack, MultiBufferSource buffer, float time) {
		stack.pushPose();
		try {
			// Offset to block center at a slight elevation above ground
			stack.translate(0.5, 0.08, 0.5);

			Matrix4f mat = stack.last().pose();

			// Slow sine pulse controlling opacity
			double pulse = (Math.sin(time * 0.04) + 1.0) * 0.5;

			// Single outward-expanding ring
			double phase = (time * RING_PULSE_SPEED) % 1.0;
			float ringRadius = (float) (RING_START_RADIUS + phase * (RING_MAX_RADIUS - RING_START_RADIUS));

			// Fade out as the ring approaches max radius
			float fadeAlpha = (float) (1.0 - phase * phase);
			if (fadeAlpha < 0.015f) {
				return;
			}

			float coreAlpha = (float) (0.30 + 0.14 * pulse) * fadeAlpha;
			float glowAlpha = (float) (0.06 + 0.06 * pulse) * fadeAlpha;

			// Deep crimson — slightly darker than the Bloom to feel "quieter"
			float coreR = (float) Math.min(1.0, 0.70 + 0.12 * pulse);
			float coreG = 0.03f;
			float coreB = 0.03f;
			float glowR = (float) Math.min(1.0, 0.40 + 0.15 * pulse);
			float glowG = 0.01f;
			float glowB = 0.01f;

			// Pass 1: glow (do not interleave with core on shared BufferSource builders)
			for (int i = 0; i < RING_SEGMENTS; i++) {
				double a1 = Math.toRadians((360.0 / RING_SEGMENTS) * i);
				double a2 = Math.toRadians((360.0 / RING_SEGMENTS) * (i + 1));

				float undulate1 = (float) (Math.sin(a1 * 5.0 + time * 0.03) * 0.06);
				float undulate2 = (float) (Math.sin(a2 * 5.0 + time * 0.03) * 0.06);

				float r1 = ringRadius + undulate1;
				float r2 = ringRadius + undulate2;

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
				emitQuad(buffer, RenderTypeInit.RITE_BOUNDARY_GLOW, mat,
						cos1 * iGlow1, 0f, sin1 * iGlow1, glowR, glowG, glowB, 0f,
						cos1 * iCore1, 0f, sin1 * iCore1, glowR, glowG, glowB, glowAlpha,
						cos2 * iCore2, 0f, sin2 * iCore2, glowR, glowG, glowB, glowAlpha,
						cos2 * iGlow2, 0f, sin2 * iGlow2, glowR, glowG, glowB, 0f);

				// Outer glow
				emitQuad(buffer, RenderTypeInit.RITE_BOUNDARY_GLOW, mat,
						cos1 * oCore1, 0f, sin1 * oCore1, glowR, glowG, glowB, glowAlpha,
						cos1 * oGlow1, 0f, sin1 * oGlow1, glowR, glowG, glowB, 0f,
						cos2 * oGlow2, 0f, sin2 * oGlow2, glowR, glowG, glowB, 0f,
						cos2 * oCore2, 0f, sin2 * oCore2, glowR, glowG, glowB, glowAlpha);
			}

			// Pass 2: core
			for (int i = 0; i < RING_SEGMENTS; i++) {
				double a1 = Math.toRadians((360.0 / RING_SEGMENTS) * i);
				double a2 = Math.toRadians((360.0 / RING_SEGMENTS) * (i + 1));

				float undulate1 = (float) (Math.sin(a1 * 5.0 + time * 0.03) * 0.06);
				float undulate2 = (float) (Math.sin(a2 * 5.0 + time * 0.03) * 0.06);

				float r1 = ringRadius + undulate1;
				float r2 = ringRadius + undulate2;

				float cos1 = (float) Math.cos(a1);
				float sin1 = (float) Math.sin(a1);
				float cos2 = (float) Math.cos(a2);
				float sin2 = (float) Math.sin(a2);

				float iCore1 = r1 - RING_CORE_WIDTH * 0.5f;
				float oCore1 = r1 + RING_CORE_WIDTH * 0.5f;

				float iCore2 = r2 - RING_CORE_WIDTH * 0.5f;
				float oCore2 = r2 + RING_CORE_WIDTH * 0.5f;

				emitQuad(buffer, RenderTypeInit.RITE_BOUNDARY_CORE, mat,
						cos1 * iCore1, 0f, sin1 * iCore1, coreR, coreG, coreB, coreAlpha,
						cos1 * oCore1, 0f, sin1 * oCore1, coreR, coreG, coreB, coreAlpha,
						cos2 * oCore2, 0f, sin2 * oCore2, coreR, coreG, coreB, coreAlpha,
						cos2 * iCore2, 0f, sin2 * iCore2, coreR, coreG, coreB, coreAlpha);
			}

		} catch (Exception e) {
			// Log the error but don't crash
			System.err.println("Error rendering Sanguine Conduit ring: " + e.getMessage());
			e.printStackTrace();
		} finally {
			stack.popPose();
		}
	}

	private static void emitQuad(MultiBufferSource buffer, RenderType type, Matrix4f mat,
			float x0, float y0, float z0, float r0, float g0, float b0, float a0,
			float x1, float y1, float z1, float r1, float g1, float b1, float a1,
			float x2, float y2, float z2, float r2, float g2, float b2, float a2,
			float x3, float y3, float z3, float r3, float g3, float b3, float a3) {
		VertexConsumer vc = buffer.getBuffer(type);
		vc.addVertex(mat, x0, y0, z0).setColor(r0, g0, b0, a0);
		vc.addVertex(mat, x1, y1, z1).setColor(r1, g1, b1, a1);
		vc.addVertex(mat, x2, y2, z2).setColor(r2, g2, b2, a2);
		vc.addVertex(mat, x3, y3, z3).setColor(r3, g3, b3, a3);
	}

	@Override
	public int getViewDistance() {
		return 128;
	}

	@Override
	public boolean shouldRenderOffScreen(SanguineConduitBlockEntity be) {
		return true;
	}
}

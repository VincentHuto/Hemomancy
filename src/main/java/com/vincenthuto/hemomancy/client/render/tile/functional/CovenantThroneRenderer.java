package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.CovenantThroneModel;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.CovenantThroneBlock;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.functional.CovenantThroneBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * Renders the Covenant Throne as four solid black cubes assembled into a
 * throne silhouette, with an animated overlay of crimson veins, glow washes,
 * blood-drip tendrils and a watching blood-eye on the backrest.
 *
 * <h3>Pass 1 – Black geometry</h3>
 * Four {@link CovenantThroneModel} parts drawn via
 * {@link RenderType#entityTranslucentCull} at near-black {@code 0xFF0D0505},
 * identical to the Sanguine Monolith technique.
 *
 * <h3>Pass 2 – Spooky effects</h3>
 * Uses the same world-aligned coordinate system as
 * {@link SanguineMonolithRenderer}'s vein overlay — {@code translate(0.5, 0, 0.5)},
 * {@code rotateY(-yRot)} — so effect coordinates are in block-local units
 * where Y=0 is the ground surface, Y=0.375 is the seat top, and Y=1.75 is
 * the backrest top.  Key surfaces (SOUTH-facing orientation, other facings
 * handled by the Y-rotation):
 * <ul>
 *   <li>Backrest front face: Z=-0.1875 plane, X ∈ [-0.375, 0.375], Y ∈ [0.375, 1.75]</li>
 *   <li>Seat top: Y=0.375 plane, X/Z ∈ [-0.3125, 0.3125]</li>
 *   <li>Armrest inner faces: X=±0.3125, Y ∈ [0.375, 1.125]</li>
 * </ul>
 */
public class CovenantThroneRenderer implements BlockEntityRenderer<CovenantThroneBlockEntity> {

	/** Reuses the monolith texture — rendered solid black so UVs are not visible. */
	public static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/model_sanguine_monolith.png");

	/** Near-black with a faint crimson tint, matching the Sanguine Monolith. */
	private static final int BLACK_COLOR = 0xFF0D0505;

	// ── Effect-pass surface constants ────────────────────────────────────────────

	/** Z coordinate of the backrest's visible front face in effect-pass local space. */
	private static final float BK_FRONT_Z   = -0.1875f;
	private static final float BK_X_HALF    =  0.375f;  // backrest half-width
	private static final float BK_Y_BOTTOM  =  0.375f;  // backrest base Y
	private static final float BK_Y_TOP     =  1.75f;   // backrest top Y
	private static final float BK_HEIGHT    =  BK_Y_TOP - BK_Y_BOTTOM; // 1.375

	private static final float SEAT_X_HALF  =  0.3125f;
	private static final float SEAT_Y       =  0.375f;  // seat top Y
	private static final float SEAT_Z_HALF  =  0.3125f;

	private static final float ARM_X_INNER  =  0.3125f; // |X| of armrest inner face
	private static final float ARM_Y_BOTTOM =  0.375f;
	private static final float ARM_Y_TOP    =  1.125f;

	// ── Vein tendril parameters ───────────────────────────────────────────────
	private static final int TENDRIL_COUNT = 9;

	// Pre-seeded tendril parameters (same seed → same shape every load)
	private final float[][] tendrilParams;

	// ── Model ────────────────────────────────────────────────────────────────
	private final CovenantThroneModel model;

	public CovenantThroneRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new CovenantThroneModel(context.bakeLayer(CovenantThroneModel.LAYER_LOCATION));

		// Deterministic seeded parameters so the throne always looks the same
		java.util.Random rand = new java.util.Random(0xC04E_7A12L);
		tendrilParams = new float[TENDRIL_COUNT][7];
		for (int i = 0; i < TENDRIL_COUNT; i++) {
			tendrilParams[i][0] = (rand.nextFloat() - 0.5f) * 2f; // start X in [-1,1] normalised
			tendrilParams[i][1] = rand.nextFloat() * 0.35f;        // start Y offset (lower zone)
			tendrilParams[i][2] = (float)(rand.nextFloat() * Math.PI * 2); // base angle
			tendrilParams[i][3] = 0.15f + rand.nextFloat() * 0.35f;        // anim speed
			tendrilParams[i][4] = 0.012f + rand.nextFloat() * 0.022f;       // amplitude (world)
			tendrilParams[i][5] = 3.5f + rand.nextFloat() * 5f;             // sine frequency
			tendrilParams[i][6] = 18 + rand.nextInt(28);                    // length (steps)
		}
	}

	// ── Main render ──────────────────────────────────────────────────────────

	@Override
	public void render(CovenantThroneBlockEntity te, float partialTicks, PoseStack ms,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		float time = mc.level.getGameTime() + partialTicks;

		Direction facing = te.getBlockState().getValue(CovenantThroneBlock.FACING);
		float yRot = switch (facing) {
			case NORTH -> 180f;
			case EAST  -> 270f;
			case WEST  ->  90f;
			default    ->   0f; // SOUTH
		};

		// ── Pass 1: black geometry ────────────────────────────────────────────
		ms.pushPose();
		ms.translate(0.5D, 1.5D, 0.5D);
		ms.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());
		ms.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());

		VertexConsumer geomVC = bufferIn.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
		model.renderToBuffer(ms, geomVC, combinedLightIn, OverlayTexture.NO_OVERLAY, BLACK_COLOR);
		ms.popPose();

		// ── Pass 2: effect overlay ────────────────────────────────────────────
		ms.pushPose();
		ms.translate(0.5D, 0.0D, 0.5D);
		ms.mulPose(Vector3.YP.rotationDegrees(-yRot).toMoj());

		renderEffects(ms, bufferIn, time);

		ms.popPose();
	}

	// ── Effect rendering ─────────────────────────────────────────────────────

	/**
	 * Renders all animated spooky overlay effects in the effect-pass coordinate
	 * space.  Order: glow washes first (semi-transparent backgrounds), then
	 * tendrils (additive lines), then the blood eye (on top).
	 */
	private void renderEffects(PoseStack ms, MultiBufferSource buf, float time) {
		renderGlowWashes(buf, ms.last().pose(), time);
		renderTendrils(buf, ms, time);
		renderBloodEye(buf, ms.last().pose(), time);
		renderDrips(buf, ms.last().pose(), time);
	}

	// ── Glow washes ──────────────────────────────────────────────────────────

	/**
	 * Semi-transparent crimson glow panels on the backrest front face, the seat
	 * top, and the armrest inner faces.  Each panel fades at its edges and pulses
	 * independently.
	 */
	private static void renderGlowWashes(MultiBufferSource buf, Matrix4f mat, float time) {
		VertexConsumer vc = buf.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);

		// ── Backrest front-face glow (fades toward the top) ──
		float bPulse = 0.5f + 0.5f * Mth.sin(time * 0.048f);
		float bAlpha = 0.09f + 0.06f * bPulse;
		float bZ = BK_FRONT_Z - 0.002f;
		// Bottom strip (full opacity)
		vc.addVertex(mat, -BK_X_HALF, BK_Y_BOTTOM,        bZ).setColor(0.80f, 0.04f, 0.04f, bAlpha);
		vc.addVertex(mat, -BK_X_HALF, BK_Y_BOTTOM + 0.45f, bZ).setColor(0.55f, 0.02f, 0.02f, bAlpha * 0.5f);
		vc.addVertex(mat,  BK_X_HALF, BK_Y_BOTTOM + 0.45f, bZ).setColor(0.55f, 0.02f, 0.02f, bAlpha * 0.5f);
		vc.addVertex(mat,  BK_X_HALF, BK_Y_BOTTOM,        bZ).setColor(0.80f, 0.04f, 0.04f, bAlpha);
		// Upper strip (fades to nothing)
		vc.addVertex(mat, -BK_X_HALF, BK_Y_BOTTOM + 0.45f, bZ).setColor(0.40f, 0.01f, 0.01f, bAlpha * 0.35f);
		vc.addVertex(mat, -BK_X_HALF, BK_Y_TOP,           bZ).setColor(0.20f, 0.01f, 0.01f, 0f);
		vc.addVertex(mat,  BK_X_HALF, BK_Y_TOP,           bZ).setColor(0.20f, 0.01f, 0.01f, 0f);
		vc.addVertex(mat,  BK_X_HALF, BK_Y_BOTTOM + 0.45f, bZ).setColor(0.40f, 0.01f, 0.01f, bAlpha * 0.35f);

		// ── Seat top glow ──
		float sPulse = 0.5f + 0.5f * Mth.sin(time * 0.038f + 1.3f);
		float sAlpha = 0.06f + 0.04f * sPulse;
		float sY = SEAT_Y + 0.002f;
		vc.addVertex(mat, -SEAT_X_HALF, sY, -SEAT_Z_HALF).setColor(0.70f, 0.03f, 0.03f, sAlpha);
		vc.addVertex(mat, -SEAT_X_HALF, sY,  SEAT_Z_HALF).setColor(0.50f, 0.02f, 0.02f, sAlpha * 0.5f);
		vc.addVertex(mat,  SEAT_X_HALF, sY,  SEAT_Z_HALF).setColor(0.50f, 0.02f, 0.02f, sAlpha * 0.5f);
		vc.addVertex(mat,  SEAT_X_HALF, sY, -SEAT_Z_HALF).setColor(0.70f, 0.03f, 0.03f, sAlpha);

		// ── Armrest inner-face glow ──
		float aPulse = 0.5f + 0.5f * Mth.sin(time * 0.055f + 2.7f);
		float aAlpha = 0.05f + 0.04f * aPulse;
		for (float armX : new float[]{ -ARM_X_INNER - 0.001f, ARM_X_INNER + 0.001f }) {
			vc.addVertex(mat, armX, ARM_Y_BOTTOM, -0.3125f).setColor(0.65f, 0.03f, 0.03f, aAlpha);
			vc.addVertex(mat, armX, ARM_Y_TOP,   -0.3125f).setColor(0.35f, 0.01f, 0.01f, 0f);
			vc.addVertex(mat, armX, ARM_Y_TOP,    0.3125f).setColor(0.35f, 0.01f, 0.01f, 0f);
			vc.addVertex(mat, armX, ARM_Y_BOTTOM,  0.3125f).setColor(0.65f, 0.03f, 0.03f, aAlpha);
		}
	}

	// ── Animated tendrils ─────────────────────────────────────────────────────

	/**
	 * Draws animated sinusoidal vein tendrils crawling upward on the backrest
	 * front face.  A subset of tendrils also spreads onto the armrest inner
	 * faces, giving the impression of living tissue spreading across the throne.
	 */
	private void renderTendrils(MultiBufferSource buf, PoseStack ms, float time) {
		VertexConsumer vc = buf.getBuffer(RenderTypeInit.RADIANT_RENDER_TYPE);
		Matrix4f mat = ms.last().pose();

		for (int i = 0; i < TENDRIL_COUNT; i++) {
			float[] p = tendrilParams[i];
			float startX  = p[0] * BK_X_HALF * 0.85f;  // X on backrest face
			float startY  = BK_Y_BOTTOM + p[1] * BK_HEIGHT * 0.4f;
			float baseAngle = p[2];
			float speed     = p[3];
			float amplitude = p[4];
			float frequency = p[5];
			int   steps     = (int) p[6];

			float angleDrift = baseAngle + 0.12f * Mth.sin(time * speed * 0.018f + i * 0.9f);
			float cosA = Mth.cos(angleDrift);
			float sinA = Mth.sin(angleDrift);
			float tOffset = time * speed * 0.09f;

			for (int step = 0; step < steps; step++) {
				float t = step * 0.016f;
				float squiggle = amplitude * Mth.sin(frequency * step + tOffset);
				float micro    = (amplitude * 0.4f) * Mth.sin(frequency * 2.3f * step + tOffset * 1.3f + i);
				float disp     = squiggle + micro;

				float px = startX + t * cosA - disp * sinA;
				float py = startY + t * sinA + disp * cosA;

				// Clip to backrest face bounds
				if (py < BK_Y_BOTTOM || py > BK_Y_TOP)       continue;
				if (Math.abs(px) > BK_X_HALF * 0.98f)         continue;

				float tipFade = 1f;
				if (step < 5)              tipFade = step / 5f;
				else if (step > steps - 5) tipFade = (steps - step) / 5f;

				float pulse = 0.55f + 0.45f * Mth.sin(time * 0.075f + i * 0.6f + step * 0.04f);
				int alpha = (int) Mth.clamp(tipFade * pulse * 190, 15, 210);
				int r = (int) Mth.clamp((95 + 85 * pulse) * pulse, 0, 255);
				int g = (int) Mth.clamp( 6 + 12 * pulse, 0, 255);
				int b = (int) Mth.clamp( 5 +  8 * pulse, 0, 255);

				float sz = 0.013f;
				float pz = BK_FRONT_Z - 0.003f;
				vc.addVertex(mat, px - sz, py - sz, pz).setColor(r, g, b, alpha);
				vc.addVertex(mat, px - sz, py + sz, pz).setColor(r, g, b, alpha);
				vc.addVertex(mat, px + sz, py + sz, pz).setColor(r, g, b, alpha);
				vc.addVertex(mat, px + sz, py - sz, pz).setColor(r, g, b, alpha);
			}
		}

		// Sparse tendrils on armrest inner faces (using half of the tendril set)
		for (int i = 0; i < TENDRIL_COUNT / 2; i++) {
			float[] p = tendrilParams[i + TENDRIL_COUNT / 2];
			float startZ  = (p[0] * 0.5f) * 0.3125f;
			float startY  = ARM_Y_BOTTOM + p[1] * 0.4f;
			float speed   = p[3] * 0.8f;
			float amp     = p[4] * 0.6f;
			float freq    = p[5];
			int   steps   = Math.max(8, (int)(p[6] * 0.55f));
			float tOff    = (float)(i * 3.7f) + time * speed * 0.09f;

			for (int side = -1; side <= 1; side += 2) { // left=-1, right=+1
				float px = side * ARM_X_INNER - side * 0.003f;

				for (int step = 0; step < steps; step++) {
					float t  = step * 0.014f;
					float dz = amp * Mth.sin(freq * step + tOff);
					float pz = startZ + t;
					float py = startY + dz;

					if (py < ARM_Y_BOTTOM || py > ARM_Y_TOP) continue;
					if (Math.abs(pz) > 0.31f) continue;

					float tipFade = 1f;
					if (step < 4)              tipFade = step / 4f;
					else if (step > steps - 4) tipFade = (steps - step) / 4f;

					float pulse = 0.5f + 0.5f * Mth.sin(time * 0.065f + side * i + step * 0.05f);
					int alpha = (int) Mth.clamp(tipFade * pulse * 155, 10, 170);
					int r = (int) Mth.clamp(80 + 70 * pulse, 0, 255);
					int g = (int) Mth.clamp(5 + 8 * pulse, 0, 255);
					int b = (int) Mth.clamp(4 + 6 * pulse, 0, 255);
					float sz = 0.011f;
					vc.addVertex(mat, px, py - sz, pz - sz).setColor(r, g, b, alpha);
					vc.addVertex(mat, px, py + sz, pz - sz).setColor(r, g, b, alpha);
					vc.addVertex(mat, px, py + sz, pz + sz).setColor(r, g, b, alpha);
					vc.addVertex(mat, px, py - sz, pz + sz).setColor(r, g, b, alpha);
				}
			}
		}
	}

	// ── Blood eye ─────────────────────────────────────────────────────────────

	/**
	 * A simplified blood-eye centred on the upper portion of the backrest's front
	 * face — a watching crimson iris behind a dark pupil, ringing with slow spokes.
	 * Much simpler than the monolith's three-eye arrangement; just one deep-set eye
	 * staring out from the darkness.
	 *
	 * <p>Scale {@code sc=0.30} gives an iris ~0.24 blocks wide — clearly visible at
	 * typical interaction distance but not overwhelming on a 0.75-block-wide
	 * backrest.  All colours are vivid blood-red so the eye reads against the
	 * near-black throne geometry.</p>
	 */
	private static void renderBloodEye(MultiBufferSource buf, Matrix4f mat, float time) {
		VertexConsumer vc = buf.getBuffer(RenderTypeInit.RADIANT_RENDER_TYPE);

		// Eye centre: upper third of backrest, dead centre horizontally
		float cx = 0f;
		float cy = BK_Y_BOTTOM + BK_HEIGHT * 0.72f; // ~1.37 blocks up
		float cz = BK_FRONT_Z - 0.004f;

		float pulse = 0.70f + 0.30f * Mth.sin(time * 0.052f);
		float beat  = 0.55f + 0.45f * Mth.sin(time * 0.11f + 0.8f);

		// sc=0.30 → iris ~0.24 wide; was 0.065 (microscopic).
		float sc = 0.30f;

		// ── Outer diffuse glow ring ──
		int SEGS = 20;
		float rIn = sc * 1.15f, rOut = sc * 1.70f;
		for (int s = 0; s < SEGS; s++) {
			double a0 = Math.PI * 2 * s / SEGS;
			double a1 = Math.PI * 2 * (s + 1) / SEGS;
			float c0 = (float)Math.cos(a0), s0 = (float)Math.sin(a0);
			float c1 = (float)Math.cos(a1), s1 = (float)Math.sin(a1);
			int ga = (int)(85 * pulse); // was 25 — barely visible; now clearly glows
			vc.addVertex(mat, cx + c0 * rIn,  cy + s0 * rIn  * 1.5f, cz).setColor(165, 14, 8, ga);
			vc.addVertex(mat, cx + c0 * rOut, cy + s0 * rOut * 1.5f, cz).setColor( 80,  4, 2, 0);
			vc.addVertex(mat, cx + c1 * rOut, cy + s1 * rOut * 1.5f, cz).setColor( 80,  4, 2, 0);
			vc.addVertex(mat, cx + c1 * rIn,  cy + s1 * rIn  * 1.5f, cz).setColor(165, 14, 8, ga);
		}

		// ── Iris (filled ellipse via fan quads) ──
		// Previously (130,6,6) — indistinguishable from near-black throne; now vivid.
		float irX = sc * 0.80f, irY = sc * 1.25f;
		for (int s = 0; s < SEGS; s++) {
			double a0 = Math.PI * 2 * s / SEGS;
			double a1 = Math.PI * 2 * (s + 1) / SEGS;
			int ia = (int)(220 * pulse);
			vc.addVertex(mat, cx, cy, cz + 0.001f)
					.setColor(230, 18, 12, ia);
			vc.addVertex(mat, cx + (float)Math.cos(a0) * irX, cy + (float)Math.sin(a0) * irY, cz + 0.001f)
					.setColor(200, 12, 8, ia);
			vc.addVertex(mat, cx + (float)Math.cos(a1) * irX, cy + (float)Math.sin(a1) * irY, cz + 0.001f)
					.setColor(200, 12, 8, ia);
			vc.addVertex(mat, cx, cy, cz + 0.001f)
					.setColor(230, 18, 12, ia); // degenerate 4th vertex — completes the fan triangle
		}

		// ── Iris spokes ──
		float spkHW = sc * 0.045f;
		for (int s = 0; s < 10; s++) {
			double angle = Math.PI * 2.0 * s / 10.0 + time * 0.0025;
			float ca = (float)Math.cos(angle), sa = (float)Math.sin(angle);
			float ix = cx + ca * sc * 0.30f, iy = cy + sa * sc * 0.46f;
			float ox = cx + ca * irX * 0.9f,  oy = cy + sa * irY * 0.9f;
			float dx = ox - ix, dy = oy - iy;
			float len = Mth.sqrt(dx * dx + dy * dy);
			if (len < 1e-6f) continue;
			float nx = -dy / len * spkHW, ny = dx / len * spkHW;
			int sa2 = (int)(190 * pulse); // was 140
			vc.addVertex(mat, ix + nx, iy + ny, cz + 0.002f).setColor(245, 22, 14, sa2);
			vc.addVertex(mat, ix - nx, iy - ny, cz + 0.002f).setColor(245, 22, 14, sa2);
			vc.addVertex(mat, ox - nx, oy - ny, cz + 0.002f).setColor(245, 22, 14, sa2);
			vc.addVertex(mat, ox + nx, oy + ny, cz + 0.002f).setColor(245, 22, 14, sa2);
		}

		// ── Dark pupil ──
		float puX = sc * 0.32f, puY = sc * 0.50f;
		for (int s = 0; s < SEGS; s++) {
			double a0 = Math.PI * 2 * s / SEGS;
			double a1 = Math.PI * 2 * (s + 1) / SEGS;
			vc.addVertex(mat, cx, cy, cz + 0.003f).setColor(6, 0, 0, 245);
			vc.addVertex(mat, cx + (float)Math.cos(a0) * puX, cy + (float)Math.sin(a0) * puY, cz + 0.003f).setColor(5, 0, 0, 245);
			vc.addVertex(mat, cx + (float)Math.cos(a1) * puX, cy + (float)Math.sin(a1) * puY, cz + 0.003f).setColor(5, 0, 0, 245);
			vc.addVertex(mat, cx, cy, cz + 0.003f).setColor(6, 0, 0, 245);
		}

		// ── Highlight ──
		float hlX = sc * 0.12f, hlY = sc * 0.13f;
		float hlCX = cx - sc * 0.12f, hlCY = cy + sc * 0.18f;
		for (int s = 0; s < 8; s++) {
			double a0 = Math.PI * 2 * s / 8;
			double a1 = Math.PI * 2 * (s + 1) / 8;
			int ha = (int)(200 * beat);
			vc.addVertex(mat, hlCX, hlCY, cz + 0.004f).setColor(255, 245, 230, ha);
			vc.addVertex(mat, hlCX + (float)Math.cos(a0) * hlX, hlCY + (float)Math.sin(a0) * hlY, cz + 0.004f).setColor(250, 240, 225, ha);
			vc.addVertex(mat, hlCX + (float)Math.cos(a1) * hlX, hlCY + (float)Math.sin(a1) * hlY, cz + 0.004f).setColor(250, 240, 225, ha);
			vc.addVertex(mat, hlCX, hlCY, cz + 0.004f).setColor(255, 245, 230, ha);
		}
	}

	// ── Blood drips ──────────────────────────────────────────────────────────

	/**
	 * Draws three animated blood drips hanging from the front bottom edge of the
	 * backrest (where it meets the seat) and one drip from the front of each
	 * armrest base.  Each drip oscillates vertically so it looks like a drop
	 * slowly forming and falling.
	 */
	private static void renderDrips(MultiBufferSource buf, Matrix4f mat, float time) {
		VertexConsumer vc = buf.getBuffer(RenderTypeInit.RADIANT_RENDER_TYPE);

		// Backrest base drips at three X positions
		float[] dripX = { -0.18f, 0.0f, 0.18f };
		float[] dripPhase = { 0f, 1.9f, 3.5f };
		for (int d = 0; d < 3; d++) {
			renderOneDrip(vc, mat, time, dripX[d], BK_Y_BOTTOM, BK_FRONT_Z - 0.003f, dripPhase[d], true);
		}
		// Armrest drips
		float[] armDripX = { -ARM_X_INNER + 0.04f, ARM_X_INNER - 0.04f };
		float[] armDripPhase = { 0.8f, 2.6f };
		for (int d = 0; d < 2; d++) {
			renderOneDrip(vc, mat, time, armDripX[d], ARM_Y_BOTTOM, -0.10f, armDripPhase[d], false);
		}
	}

	/**
	 * Renders one drip: a thin elongated teardrop hanging from {@code (startX, startY, z)}.
	 * The drip length oscillates to simulate the drop forming and falling away.
	 *
	 * @param vertical if true the drip hangs down; if false it hangs at an angle
	 */
	private static void renderOneDrip(VertexConsumer vc, Matrix4f mat, float time,
			float startX, float startY, float z, float phase, boolean vertical) {
		float cycleLen = 0.10f + 0.07f * Mth.sin(phase * 7.3f); // drip length cycle
		float dropT    = ((time * 0.012f + phase) % 1.0f);       // position in the drop cycle
		float dropLen  = Mth.clamp(dropT * cycleLen, 0.005f, cycleLen);
		float fade     = dropT < 0.7f ? 1f : 1f - (dropT - 0.7f) / 0.3f;

		float endY = startY - dropLen;
		float hw   = 0.009f;
		int baseA  = (int)(Mth.clamp(fade, 0, 1) * 200f);
		if (baseA < 8) return;

		// Stem
		vc.addVertex(mat, startX - hw, startY,  z).setColor(140, 5, 5, baseA);
		vc.addVertex(mat, startX - hw, endY,    z).setColor(110, 4, 4, baseA / 2);
		vc.addVertex(mat, startX + hw, endY,    z).setColor(110, 4, 4, baseA / 2);
		vc.addVertex(mat, startX + hw, startY,  z).setColor(140, 5, 5, baseA);

		// Bulb at the tip
		float br = 0.014f * (0.6f + 0.4f * Mth.sin(time * 0.15f + phase));
		vc.addVertex(mat, startX - br, endY,       z).setColor(150, 7, 7, baseA / 2);
		vc.addVertex(mat, startX - br, endY - br,  z).setColor(130, 5, 5, 0);
		vc.addVertex(mat, startX + br, endY - br,  z).setColor(130, 5, 5, 0);
		vc.addVertex(mat, startX + br, endY,       z).setColor(150, 7, 7, baseA / 2);
	}

	// ── Misc ─────────────────────────────────────────────────────────────────

	@Override
	public boolean shouldRenderOffScreen(CovenantThroneBlockEntity te) {
		return true;
	}
}

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
import net.minecraft.world.phys.Vec3;
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
	private static final float BK_FRONT_Z   = -0.350f;
	private static final float BK_REAR_Z    = -0.505f;
	private static final float BK_X_HALF    = 0.82f;
	private static final float BK_Y_BOTTOM  = 0.45f;
	private static final float BK_Y_TOP     = 2.95f;
	private static final float BK_HEIGHT    =  BK_Y_TOP - BK_Y_BOTTOM; // 1.375

	private static final float SEAT_X_HALF  =  0.52f;
	private static final float SEAT_Y       =  0.50f;
	private static final float SEAT_Z_HALF  =  0.55f;

	private static final float ARM_X_INNER  =  0.626f;
	private static final float ARM_X_MIN    =  0.65f;
	private static final float ARM_X_MAX    =  1.10f;
	private static final float ARM_Z_MIN    = -0.42f;
	private static final float ARM_Z_MAX    =  0.29f;
	private static final float ARM_Y_BOTTOM =  0.45f;
	private static final float ARM_Y_TOP    =  1.125f;
	private static final float PYLON_X_INNER = 1.18f;
	private static final float PYLON_X_OUTER = 1.44f;
	private static final float PYLON_Y_BOTTOM = 0.08f;
	private static final float PYLON_Y_TOP = 1.98f;
	private static final float PYLON_Z_MIN = -0.34f;
	private static final float PYLON_Z_MAX = 0.44f;

	// ── Vein tendril parameters ───────────────────────────────────────────────
	private static final int TENDRIL_COUNT = 16;

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

		float wakeIntensity = getWakeIntensity(te, partialTicks, time);
		float trance = te.getTranceVisualProgress(partialTicks);
		boolean renderRearMotif = shouldRenderRearMotif(mc, te, facing);
		renderEffects(ms, bufferIn, time, wakeIntensity, trance, renderRearMotif);

		ms.popPose();
	}

	// ── Effect rendering ─────────────────────────────────────────────────────

	/**
	 * Renders all animated spooky overlay effects in the effect-pass coordinate
	 * space.  Order: glow washes first (semi-transparent backgrounds), then
	 * tendrils (additive lines), then the blood eye (on top).
	 */
	private void renderEffects(PoseStack ms, MultiBufferSource buf, float time, float wakeIntensity, float trance, boolean renderRearMotif) {
		renderGlowWashes(buf, ms.last().pose(), time);
		renderTendrils(buf, ms, time);
		renderSeatTendrils(buf, ms.last().pose(), time, wakeIntensity);
		renderPylonTendrils(buf, ms.last().pose(), time, wakeIntensity);
		renderSidePanelFaceTendrils(buf, ms.last().pose(), time, wakeIntensity);
		renderArmTopTendrils(buf, ms.last().pose(), time, wakeIntensity);
		if (renderRearMotif) {
			renderCircleOfWillisMotif(buf, ms.last().pose(), time, wakeIntensity);
		}
		renderBloodEye(buf, ms.last().pose(), time);
		renderDrips(buf, ms.last().pose(), time);
		renderTranceAura(buf, ms.last().pose(), time, trance * wakeIntensity);
	}

	private static boolean shouldRenderRearMotif(Minecraft mc, CovenantThroneBlockEntity te, Direction facing) {
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		Vec3 center = Vec3.atCenterOf(te.getBlockPos());
		Vec3 toCamera = camera.subtract(center);
		Vec3 rearNormal = Vec3.atLowerCornerOf(facing.getOpposite().getNormal());
		return toCamera.dot(rearNormal) > 0.05D;
	}

	private static float getWakeIntensity(CovenantThroneBlockEntity te, float partialTicks, float time) {
		float wake = te.getWakeIntensity(partialTicks);
		float idlePulse = 0.18f + 0.07f * (0.5f + 0.5f * Mth.sin(time * 0.035f));
		return Mth.clamp(idlePulse + wake * 0.82f, 0.18f, 1.0f);
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
		float bZ = BK_FRONT_Z;
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
				float pz = BK_FRONT_Z;
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
	private static void renderSeatTendrils(MultiBufferSource buf, Matrix4f mat, float time, float wakeIntensity) {
		VertexConsumer vc = buf.getBuffer(RenderTypeInit.RADIANT_RENDER_TYPE);
		float y = SEAT_Y + 0.006f;
		for (int i = 0; i < 7; i++) {
			float phase = i * 1.37f;
			float x0 = -SEAT_X_HALF + 0.08f + i * (SEAT_X_HALF * 2.0f - 0.16f) / 6.0f;
			float z0 = -SEAT_Z_HALF + 0.06f;
			for (int step = 0; step < 24; step++) {
				float t = step / 23.0f;
				float x = x0 + 0.055f * Mth.sin(t * 8.0f + time * 0.045f + phase);
				float z = z0 + t * (SEAT_Z_HALF * 1.78f);
				float pulse = 0.45f + 0.55f * Mth.sin(time * 0.07f + phase + t * 4.0f);
				int alpha = (int) Mth.clamp((35f + 125f * wakeIntensity) * pulse, 15f, 190f);
				float sz = 0.011f + 0.006f * wakeIntensity;
				vc.addVertex(mat, x - sz, y, z - sz).setColor(160, 5, 5, alpha);
				vc.addVertex(mat, x - sz, y, z + sz).setColor(95, 2, 2, alpha / 2);
				vc.addVertex(mat, x + sz, y, z + sz).setColor(95, 2, 2, alpha / 2);
				vc.addVertex(mat, x + sz, y, z - sz).setColor(160, 5, 5, alpha);
			}
		}
	}

	private static void renderPylonTendrils(MultiBufferSource buf, Matrix4f mat, float time, float wakeIntensity) {
		VertexConsumer vc = buf.getBuffer(RenderTypeInit.RADIANT_RENDER_TYPE);
		for (int side = -1; side <= 1; side += 2) {
			float x = side * (PYLON_X_INNER + 0.055f);
			for (int i = 0; i < 5; i++) {
				float phase = side * 0.8f + i * 1.11f;
				float zBase = -0.32f + i * 0.16f;
				for (int step = 0; step < 30; step++) {
					float t = step / 29.0f;
					float y = PYLON_Y_BOTTOM + t * (PYLON_Y_TOP - PYLON_Y_BOTTOM);
					float z = zBase + 0.045f * Mth.sin(t * 10.0f + time * 0.038f + phase);
					if (!isOnPylonSide(x, y, z, side)) {
						continue;
					}
					float pulse = 0.45f + 0.55f * Mth.sin(time * 0.06f + phase + step * 0.12f);
					int alpha = (int) Mth.clamp((25f + 110f * wakeIntensity) * pulse, 10f, 165f);
					float sz = 0.010f + 0.004f * wakeIntensity;
					vc.addVertex(mat, x, y - sz, z - sz).setColor(145, 4, 4, alpha);
					vc.addVertex(mat, x, y + sz, z - sz).setColor(185, 8, 6, alpha);
					vc.addVertex(mat, x, y + sz, z + sz).setColor(105, 2, 2, alpha / 2);
					vc.addVertex(mat, x, y - sz, z + sz).setColor(105, 2, 2, alpha / 2);
				}
			}
		}
	}

	private static boolean isOnPylonSide(float x, float y, float z, int side) {
		float signedX = Math.abs(x);
		return x * side > 0.0f
				&& signedX >= PYLON_X_INNER
				&& signedX <= PYLON_X_OUTER
				&& y >= PYLON_Y_BOTTOM
				&& y <= PYLON_Y_TOP
				&& z >= PYLON_Z_MIN
				&& z <= PYLON_Z_MAX;
	}

	private static void renderSidePanelFaceTendrils(MultiBufferSource buf, Matrix4f mat, float time, float wakeIntensity) {
		VertexConsumer vc = buf.getBuffer(RenderTypeInit.RADIANT_RENDER_TYPE);
		float z = BK_FRONT_Z + 0.001f;
		for (int side = -1; side <= 1; side += 2) {
			for (int i = 0; i < 5; i++) {
				float phase = i * 1.29f + side * 0.6f;
				float xBase = side * (PYLON_X_INNER + 0.035f + i * 0.045f);
				for (int step = 0; step < 28; step++) {
					float t = step / 27.0f;
					float y = 0.35f + t * 1.58f;
					float x = clampToPylonX(xBase + 0.030f * Mth.sin(t * 9.0f + time * 0.043f + phase), side);
					float pulse = 0.40f + 0.60f * Mth.sin(time * 0.066f + phase + step * 0.1f);
					int alpha = (int) Mth.clamp((28f + 115f * wakeIntensity) * pulse, 10f, 170f);
					float sz = 0.010f + 0.004f * wakeIntensity;
					vc.addVertex(mat, x - sz, y - sz, z).setColor(155, 4, 4, alpha);
					vc.addVertex(mat, x - sz, y + sz, z).setColor(190, 8, 6, alpha);
					vc.addVertex(mat, x + sz, y + sz, z).setColor(105, 2, 2, alpha / 2);
					vc.addVertex(mat, x + sz, y - sz, z).setColor(105, 2, 2, alpha / 2);
				}
			}
		}
	}

	private static float clampToPylonX(float x, int side) {
		float signedX = Mth.clamp(Math.abs(x), PYLON_X_INNER, PYLON_X_OUTER);
		return side < 0 ? -signedX : signedX;
	}

	private static void renderArmTopTendrils(MultiBufferSource buf, Matrix4f mat, float time, float wakeIntensity) {
		VertexConsumer vc = buf.getBuffer(RenderTypeInit.RADIANT_RENDER_TYPE);
		float y = ARM_Y_TOP + 0.006f;
		for (int side = -1; side <= 1; side += 2) {
			float xCenter = side * 0.88f;
			for (int i = 0; i < 4; i++) {
				float phase = i * 1.8f + side * 0.4f;
				float zStart = -0.42f + i * 0.24f;
				for (int step = 0; step < 22; step++) {
					float t = step / 21.0f;
					float x = xCenter + side * 0.055f * Mth.sin(t * 7.5f + time * 0.052f + phase);
					float z = zStart + t * 0.34f;
					if (!isOnArmTop(x, z, side)) {
						continue;
					}
					float pulse = 0.35f + 0.65f * Mth.sin(time * 0.074f + phase + t * 5.0f);
					int alpha = (int) Mth.clamp((30f + 120f * wakeIntensity) * pulse, 12f, 175f);
					float sz = 0.010f + 0.005f * wakeIntensity;
					vc.addVertex(mat, x - sz, y, z - sz).setColor(170, 5, 5, alpha);
					vc.addVertex(mat, x - sz, y, z + sz).setColor(110, 2, 2, alpha / 2);
					vc.addVertex(mat, x + sz, y, z + sz).setColor(110, 2, 2, alpha / 2);
					vc.addVertex(mat, x + sz, y, z - sz).setColor(170, 5, 5, alpha);
				}
			}
		}
	}

	private static boolean isOnArmTop(float x, float z, int side) {
		float signedX = Math.abs(x);
		return x * side > 0.0f
				&& signedX >= ARM_X_MIN
				&& signedX <= ARM_X_MAX
				&& z >= ARM_Z_MIN
				&& z <= ARM_Z_MAX;
	}

	private static void renderCircleOfWillisMotif(MultiBufferSource buf, Matrix4f mat, float time, float wakeIntensity) {
		VertexConsumer vc = buf.getBuffer(RenderTypeInit.RADIANT_RENDER_TYPE);

		float pulse = 0.62f + 0.38f * Mth.sin(time * 0.062f);
		int glowAlpha = (int) Mth.clamp(26f + 45f * wakeIntensity + 18f * pulse, 24f, 95f);
		int coreAlpha = (int) Mth.clamp(56f + 78f * wakeIntensity + 24f * pulse, 58f, 170f);
		float z = BK_REAR_Z;
		float cx = 0.0f;
		float cy = 1.76f;
		float rx = 0.50f;
		float ry = 0.61f;

		renderWillisOrganicLoop(vc, mat, cx, cy, rx, ry, z, glowAlpha, coreAlpha);
		renderWillisCurve(vc, mat, -0.13f, 2.36f, -0.15f, 2.48f, -0.22f, 2.55f, -0.24f, 2.76f,
				z, 0.024f, 0.017f, 0.009f, 0.006f, 18, glowAlpha, coreAlpha);
		renderWillisCurve(vc, mat, 0.13f, 2.36f, 0.15f, 2.48f, 0.22f, 2.55f, 0.24f, 2.76f,
				z, 0.024f, 0.017f, 0.009f, 0.006f, 18, glowAlpha, coreAlpha);
		renderWillisCurve(vc, mat, -0.22f, 2.33f, -0.12f, 2.43f, 0.12f, 2.43f, 0.22f, 2.33f,
				z, 0.016f, 0.012f, 0.005f, 0.004f, 18, glowAlpha / 2, coreAlpha);

		renderWillisCurve(vc, mat, 0.0f, 1.18f, -0.02f, 1.00f, 0.02f, 0.78f, 0.0f, 0.58f,
				z, 0.026f, 0.024f, 0.010f, 0.009f, 26, glowAlpha, coreAlpha);
		renderWillisCurve(vc, mat, 0.0f, 0.62f, -0.02f, 0.50f, 0.02f, 0.38f, 0.0f, 0.22f,
				z, 0.020f, 0.015f, 0.007f, 0.005f, 18, glowAlpha, coreAlpha);
		for (int side = -1; side <= 1; side += 2) {
			renderWillisCurve(vc, mat, side * 0.03f, 0.72f, side * 0.15f, 0.88f,
					side * 0.40f, 0.78f, side * 0.48f, 0.44f,
					z, 0.021f, 0.016f, 0.007f, 0.005f, 26, glowAlpha, coreAlpha);
			renderWillisCurve(vc, mat, side * 0.48f, 0.44f, side * 0.52f, 0.32f,
					side * 0.50f, 0.18f, side * 0.46f, 0.08f,
					z, 0.016f, 0.012f, 0.005f, 0.003f, 14, glowAlpha, coreAlpha);
			renderWillisCurve(vc, mat, side * 0.26f, 0.68f, side * 0.18f, 0.54f,
					side * 0.14f, 0.42f, side * 0.07f, 0.34f,
					z, 0.015f, 0.005f, 0.004f, 0.0016f, 12, glowAlpha / 2, coreAlpha);
			renderWillisCurve(vc, mat, side * 0.26f, 0.48f, side * 0.17f, 0.40f,
					side * 0.13f, 0.32f, side * 0.06f, 0.26f,
					z, 0.012f, 0.004f, 0.003f, 0.0014f, 10, glowAlpha / 2, coreAlpha);
		}

		for (int i = 0; i < 5; i++) {
			float yRib = 1.08f - i * 0.12f;
			float width = 0.18f + i * 0.055f;
			renderWillisCurve(vc, mat, 0.0f, yRib, -width * 0.35f, yRib + 0.09f,
					-width * 0.80f, yRib + 0.11f, -width, yRib + 0.03f,
					z, 0.014f, 0.007f, 0.004f, 0.002f, 14, glowAlpha / 2, coreAlpha);
			renderWillisCurve(vc, mat, 0.0f, yRib, width * 0.35f, yRib + 0.09f,
					width * 0.80f, yRib + 0.11f, width, yRib + 0.03f,
					z, 0.014f, 0.007f, 0.004f, 0.002f, 14, glowAlpha / 2, coreAlpha);
		}

		renderWillisCurve(vc, mat, 0.0f, 1.38f, -0.01f, 1.58f, 0.01f, 1.84f, 0.0f, 2.08f,
				z + 0.001f, 0.010f, 0.003f, 0.0025f, 0.001f, 18, glowAlpha / 3, coreAlpha / 2);
		for (int side = -1; side <= 1; side += 2) {
			renderWillisCurve(vc, mat, side * 0.42f, 1.93f, side * 0.50f, 1.98f,
					side * 0.60f, 1.98f, side * 0.70f, 1.92f,
					z, 0.020f, 0.012f, 0.006f, 0.003f, 18, glowAlpha, coreAlpha);
			renderWillisCurve(vc, mat, side * 0.44f, 1.55f, side * 0.54f, 1.50f,
					side * 0.62f, 1.51f, side * 0.71f, 1.58f,
					z, 0.019f, 0.011f, 0.006f, 0.003f, 18, glowAlpha, coreAlpha);
			renderWillisTwigCluster(vc, mat, side, z, glowAlpha, coreAlpha);
		}
	}

	private static void renderWillisOrganicLoop(VertexConsumer vc, Matrix4f mat,
			float cx, float cy, float rx, float ry, float z, int glowAlpha, int coreAlpha) {
		renderWillisCurve(vc, mat, cx - 0.11f, cy + ry * 0.92f, cx - rx * 0.56f, cy + ry * 0.88f,
				cx - rx * 1.10f, cy + ry * 0.40f, cx - rx * 0.86f, cy - ry * 0.74f,
				z, 0.025f, 0.023f, 0.008f, 0.007f, 44, glowAlpha, coreAlpha);
		renderWillisCurve(vc, mat, cx + 0.11f, cy + ry * 0.92f, cx + rx * 0.56f, cy + ry * 0.88f,
				cx + rx * 1.10f, cy + ry * 0.40f, cx + rx * 0.86f, cy - ry * 0.74f,
				z, 0.025f, 0.023f, 0.008f, 0.007f, 44, glowAlpha, coreAlpha);
		renderWillisCurve(vc, mat, cx - 0.15f, cy + ry * 0.92f, cx - 0.06f, cy + ry * 1.02f,
				cx + 0.06f, cy + ry * 1.02f, cx + 0.15f, cy + ry * 0.92f,
				z, 0.018f, 0.016f, 0.005f, 0.004f, 20, glowAlpha / 2, coreAlpha);
	}

	private static void renderWillisTwigCluster(VertexConsumer vc, Matrix4f mat,
			int side, float z, int glowAlpha, int coreAlpha) {
		renderWillisCurve(vc, mat, side * 0.58f, 2.06f, side * 0.68f, 2.17f, side * 0.76f, 2.20f, side * 0.88f, 2.31f,
				z, 0.015f, 0.004f, 0.004f, 0.0015f, 12, glowAlpha / 2, coreAlpha);
		renderWillisCurve(vc, mat, side * 0.62f, 1.97f, side * 0.72f, 2.04f, side * 0.78f, 2.02f, side * 0.89f, 2.06f,
				z, 0.012f, 0.003f, 0.003f, 0.0012f, 10, glowAlpha / 2, coreAlpha);
		renderWillisCurve(vc, mat, side * 0.58f, 1.46f, side * 0.68f, 1.34f, side * 0.77f, 1.30f, side * 0.88f, 1.19f,
				z, 0.015f, 0.004f, 0.004f, 0.0015f, 12, glowAlpha / 2, coreAlpha);
		renderWillisCurve(vc, mat, side * 0.63f, 1.56f, side * 0.73f, 1.49f, side * 0.79f, 1.51f, side * 0.90f, 1.47f,
				z, 0.012f, 0.003f, 0.003f, 0.0012f, 10, glowAlpha / 2, coreAlpha);
		renderWillisCurve(vc, mat, side * 0.30f, 2.27f, side * 0.41f, 2.39f, side * 0.48f, 2.40f, side * 0.57f, 2.50f,
				z, 0.012f, 0.003f, 0.003f, 0.0012f, 10, glowAlpha / 2, coreAlpha);
		renderWillisCurve(vc, mat, side * 0.34f, 1.20f, side * 0.45f, 1.08f, side * 0.51f, 1.06f, side * 0.61f, 0.96f,
				z, 0.012f, 0.003f, 0.003f, 0.0012f, 10, glowAlpha / 2, coreAlpha);
	}

	private static void renderWillisCurve(VertexConsumer vc, Matrix4f mat,
			float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3,
			float z, float glowWidth0, float glowWidth1, float coreWidth0, float coreWidth1,
			int steps, int glowAlpha, int coreAlpha) {
		for (int i = 0; i < steps; i++) {
			float t0 = i / (float) steps;
			float t1 = (i + 1) / (float) steps;
			float px0 = cubic(x0, x1, x2, x3, t0);
			float py0 = cubic(y0, y1, y2, y3, t0);
			float px1 = cubic(x0, x1, x2, x3, t1);
			float py1 = cubic(y0, y1, y2, y3, t1);
			float mid = (t0 + t1) * 0.5f;
			float endFade = Mth.clamp(Math.min(mid, 1.0f - mid) * 8.0f, 0.20f, 1.0f);
			float glowWidth = Mth.lerp(mid, glowWidth0, glowWidth1);
			float coreWidth = Mth.lerp(mid, coreWidth0, coreWidth1);
			renderWillisSegment(vc, mat, px0, py0, px1, py1, z, glowWidth, coreWidth,
					(int) (glowAlpha * endFade), (int) (coreAlpha * endFade));
		}
	}

	private static float cubic(float p0, float p1, float p2, float p3, float t) {
		float inv = 1.0f - t;
		return inv * inv * inv * p0 + 3.0f * inv * inv * t * p1 + 3.0f * inv * t * t * p2 + t * t * t * p3;
	}

	private static void renderWillisSegment(VertexConsumer vc, Matrix4f mat,
			float x0, float y0, float x1, float y1, float z, float glowWidth, float coreWidth,
			int glowAlpha, int coreAlpha) {
		renderVesselSegment(vc, mat, x0, y0, x1, y1, z, glowWidth, 160, 8, 6, glowAlpha);
		renderVesselSegment(vc, mat, x0, y0, x1, y1, z + 0.001f, coreWidth, 235, 24, 14, coreAlpha);
	}

	private static void renderVesselSegment(VertexConsumer vc, Matrix4f mat,
			float x0, float y0, float x1, float y1, float z, float width,
			int r, int g, int b, int alpha) {
		float dx = x1 - x0;
		float dy = y1 - y0;
		float len = Mth.sqrt(dx * dx + dy * dy);
		if (len < 1.0e-5f || alpha <= 0) {
			return;
		}
		float nx = -dy / len * width;
		float ny = dx / len * width;
		vc.addVertex(mat, x0 + nx, y0 + ny, z).setColor(r, g, b, alpha);
		vc.addVertex(mat, x0 - nx, y0 - ny, z).setColor(r, g, b, alpha);
		vc.addVertex(mat, x1 - nx, y1 - ny, z).setColor(r, g, b, alpha);
		vc.addVertex(mat, x1 + nx, y1 + ny, z).setColor(r, g, b, alpha);
	}

	private static void renderBloodEye(MultiBufferSource buf, Matrix4f mat, float time) {
		VertexConsumer vc = buf.getBuffer(RenderTypeInit.RADIANT_RENDER_TYPE);

		// Eye centre: upper third of backrest, dead centre horizontally
		float cx = 0f;
		float cy = 1.78f;
		float cz = BK_FRONT_Z + 0.008f;

		float pulse = 0.70f + 0.30f * Mth.sin(time * 0.052f);
		float beat  = 0.55f + 0.45f * Mth.sin(time * 0.11f + 0.8f);

		// sc=0.30 → iris ~0.24 wide; was 0.065 (microscopic).
		float sc = 0.21f;

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
			renderOneDrip(vc, mat, time, dripX[d], BK_Y_BOTTOM, BK_FRONT_Z, dripPhase[d], true);
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

	private static void renderTranceAura(MultiBufferSource buf, Matrix4f mat, float time, float trance) {
		if (trance <= 0.01f) {
			return;
		}
		VertexConsumer vc = buf.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);
		int segments = 40;
		float pulse = 0.5f + 0.5f * Mth.sin(time * 0.12f);
		float inner = 0.62f + pulse * 0.04f;
		float outer = 1.42f + pulse * 0.10f;
		float y = 0.035f;
		int alphaInner = (int) Mth.clamp(120f * trance, 0f, 150f);
		int alphaOuter = (int) Mth.clamp(12f * trance, 0f, 35f);
		for (int i = 0; i < segments; i++) {
			double a0 = Math.PI * 2.0D * i / segments;
			double a1 = Math.PI * 2.0D * (i + 1) / segments;
			float x0i = (float) Math.cos(a0) * inner;
			float z0i = (float) Math.sin(a0) * inner;
			float x0o = (float) Math.cos(a0) * outer;
			float z0o = (float) Math.sin(a0) * outer;
			float x1i = (float) Math.cos(a1) * inner;
			float z1i = (float) Math.sin(a1) * inner;
			float x1o = (float) Math.cos(a1) * outer;
			float z1o = (float) Math.sin(a1) * outer;
			vc.addVertex(mat, x0i, y, z0i).setColor(210, 15, 10, alphaInner);
			vc.addVertex(mat, x0o, y, z0o).setColor(100, 4, 4, alphaOuter);
			vc.addVertex(mat, x1o, y, z1o).setColor(100, 4, 4, alphaOuter);
			vc.addVertex(mat, x1i, y, z1i).setColor(210, 15, 10, alphaInner);
		}
	}

	@Override
	public boolean shouldRenderOffScreen(CovenantThroneBlockEntity te) {
		return true;
	}
}

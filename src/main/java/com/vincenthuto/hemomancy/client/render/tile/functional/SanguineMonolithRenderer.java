package com.vincenthuto.hemomancy.client.render.tile.functional;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.SanguineMonolithModel;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.functional.SanguineMonolithBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Renders the Sanguine Monolith as a black slab (1 wide × 2 tall × 0.5 deep)
 * with an animated red vein pattern crawling over the bottom third, a glowing
 * blood-eye on the top half of the front face, and a rising-from-ground
 * animation on first placement.
 */
public class SanguineMonolithRenderer implements BlockEntityRenderer<SanguineMonolithBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_sanguine_monolith.png");

	private final SanguineMonolithModel model;

	// ── Vein animation parameters ──
	private static final int VEIN_COUNT = 12;
	private static final float VEIN_ZONE_HEIGHT = 0.667f; // bottom third of 2-block height

	/** How far below the base the monolith starts when first placed. */
	private static final float RISE_Y_OFFSET = 2.5f;

	// Pre-computed vein tendril parameters (seed-based for consistency)
	private final float[][] veinParams;

	public SanguineMonolithRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new SanguineMonolithModel(context.bakeLayer(SanguineMonolithModel.LAYER_LOCATION));

		java.util.Random rand = new java.util.Random(42L);
		veinParams = new float[VEIN_COUNT][8];
		for (int i = 0; i < VEIN_COUNT; i++) {
			veinParams[i][0] = rand.nextFloat();          // start X position (0–1 across face)
			veinParams[i][1] = rand.nextFloat() * 0.33f;  // start Y position (bottom third only)
			veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2); // base angle
			veinParams[i][3] = 0.3f + rand.nextFloat() * 0.5f; // speed
			veinParams[i][4] = 0.02f + rand.nextFloat() * 0.04f; // amplitude (world-space)
			veinParams[i][5] = 3f + rand.nextFloat() * 5f;  // frequency
			veinParams[i][6] = 15 + rand.nextInt(25);        // length (steps)
			veinParams[i][7] = 0.3f + rand.nextFloat() * 0.7f; // brightness
		}
	}

	@Override
	public boolean shouldRenderOffScreen(SanguineMonolithBlockEntity te) {
		return true;
	}

	@Override
	public void render(SanguineMonolithBlockEntity te, float partialTicks, PoseStack ms, MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn) {

		Direction facing = te.getBlockState().getValue(FACING);
		float yRot = switch (facing) {
			case NORTH -> 180f;
			case EAST  -> 270f;
			case SOUTH -> 0f;
			case WEST  -> 90f;
			default    -> 0f;
		};

		float riseProgress = te.getRiseProgress(partialTicks);
		// Smooth the rise with a slight ease-out curve
		float easedProgress = 1f - (1f - riseProgress) * (1f - riseProgress);
		float yOffset = -(1f - easedProgress) * RISE_Y_OFFSET;

		// ── Pass 1: Render the black monolith body ──
		ms.pushPose();
		ms.translate(0.5D, 1.5D + yOffset, 0.5D);
		ms.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());
		ms.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());

		VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
		// Render entirely black (RGB = 0.05, 0.02, 0.02 for a very dark slab)
		model.renderToBuffer(ms, vertexConsumer, combinedLightIn, OverlayTexture.NO_OVERLAY,
				0.05F, 0.02F, 0.02F, 1.0F);
		ms.popPose();

		// ── Pass 2: Animated vein overlay + eye (bottom third + top half front face) ──
		float time = (te.getLevel() != null)
				? te.getLevel().getGameTime() + partialTicks
				: te.getTickCount() + partialTicks;

		ms.pushPose();
		ms.translate(0.5D, yOffset, 0.5D);
		ms.mulPose(Vector3.YP.rotationDegrees(-yRot).toMoj());

		renderVeinOverlay(ms, bufferIn, time, facing);
		renderEyeOverlay(ms, bufferIn, time);
		renderBackSymbol(ms, bufferIn, time);
		ms.popPose();
	}

	// ── Vein overlay ─────────────────────────────────────────────────────────────

	private void renderVeinOverlay(PoseStack ms, MultiBufferSource bufferIn, float time, Direction facing) {
		VertexConsumer vc = bufferIn.getBuffer(RenderTypeInit.RADIANT_RENDER_TYPE);

		// Render veins on front face (Z = -0.251 so it sits just above the model surface)
		renderFaceVeins(ms, vc, time, 0.0f, 0.0f, -0.251f, 1.0f, false);

		// Render veins on back face
		renderFaceVeins(ms, vc, time, 0.0f, 0.0f, 0.251f, 1.0f, true);

		// Render veins on left side (X = -0.501)
		renderSideVeins(ms, vc, time, -0.501f, 0.0f, 0.0f, false);

		// Render veins on right side (X = 0.501)
		renderSideVeins(ms, vc, time, 0.501f, 0.0f, 0.0f, true);
	}

	private void renderFaceVeins(PoseStack ms, VertexConsumer vc, float time,
			float faceX, float faceY, float faceZ, float faceWidth, boolean flipNormal) {

		Matrix4f mat = ms.last().pose();

		for (int i = 0; i < VEIN_COUNT; i++) {
			float[] p = veinParams[i];
			float startX = (p[0] - 0.5f) * faceWidth;
			float startY = p[1] * VEIN_ZONE_HEIGHT;
			float baseAngle = p[2];
			float speed = p[3];
			float amplitude = p[4];
			float frequency = p[5];
			int length = (int) p[6];
			float brightness = p[7];

			float angleDrift = baseAngle + 0.15f * Mth.sin(time * speed * 0.02f + i);
			float cosA = Mth.cos(angleDrift);
			float sinA = Mth.sin(angleDrift);
			float timeOffset = time * speed * 0.1f;

			int baseRed = (int) (100 + 80 * brightness);
			int baseGreen = (int) (5 + 15 * brightness);
			int baseBlue = (int) (5 + 10 * brightness);

			for (int step = 0; step < length; step++) {
				float t = step * 0.015f;
				float squiggle = amplitude * Mth.sin(frequency * step + timeOffset);
				float microSquiggle = (amplitude * 0.3f) * Mth.sin(frequency * 2.7f * step + timeOffset * 1.4f + i);
				float displacement = squiggle + microSquiggle;

				float px = faceX + startX + t * cosA - displacement * sinA;
				float py = faceY + startY + t * sinA + displacement * cosA;

				// Stay within bottom third
				if (py > VEIN_ZONE_HEIGHT || py < 0) continue;
				// Stay within face width
				if (Math.abs(px) > faceWidth * 0.5f) continue;

				float tipFade = 1f;
				if (step < 5) tipFade = step / 5f;
				else if (step > length - 5) tipFade = (length - step) / 5f;

				float pulse = 0.6f + 0.4f * Mth.sin(time * 0.08f + i * 0.5f + step * 0.05f);
				int alpha = (int) (Mth.clamp(tipFade * pulse * 200, 20, 220));
				int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
				int g = (int) Mth.clamp(baseGreen * pulse * 0.4f, 0, 255);
				int b = (int) Mth.clamp(baseBlue * pulse * 0.3f, 0, 255);

				float size = 0.015f;
				float z = faceZ;

				// Render a small quad for each vein step
				vc.vertex(mat, px - size, py - size, z).color(r, g, b, alpha).endVertex();
				vc.vertex(mat, px - size, py + size, z).color(r, g, b, alpha).endVertex();
				vc.vertex(mat, px + size, py + size, z).color(r, g, b, alpha).endVertex();
				vc.vertex(mat, px + size, py - size, z).color(r, g, b, alpha).endVertex();
			}
		}

		// Add a subtle pulsing glow wash across the bottom third
		float glowPulse = 0.5f + 0.5f * Mth.sin(time * 0.04f);
		int glowAlpha = (int) (20 * glowPulse);
		float hw = faceWidth * 0.5f;
		float z = faceZ + (flipNormal ? -0.001f : 0.001f);

		vc.vertex(mat, -hw, 0f, z).color(80, 5, 5, glowAlpha).endVertex();
		vc.vertex(mat, -hw, VEIN_ZONE_HEIGHT * 0.8f, z).color(40, 2, 2, 0).endVertex();
		vc.vertex(mat,  hw, VEIN_ZONE_HEIGHT * 0.8f, z).color(40, 2, 2, 0).endVertex();
		vc.vertex(mat,  hw, 0f, z).color(80, 5, 5, glowAlpha).endVertex();
	}

	private void renderSideVeins(PoseStack ms, VertexConsumer vc, float time,
			float sideX, float sideY, float sideZ, boolean rightSide) {

		Matrix4f mat = ms.last().pose();
		float halfDepth = 0.25f; // 0.5 blocks / 2

		for (int i = 0; i < VEIN_COUNT / 2; i++) {
			float[] p = veinParams[i + VEIN_COUNT / 2];
			float startZ = (p[0] - 0.5f) * halfDepth * 2;
			float startY = p[1] * VEIN_ZONE_HEIGHT;
			float baseAngle = p[2];
			float speed = p[3];
			float amplitude = p[4] * 0.5f; // narrower on sides
			float frequency = p[5];
			int length = (int) (p[6] * 0.7f);
			float brightness = p[7];

			float angleDrift = baseAngle + 0.15f * Mth.sin(time * speed * 0.02f + i + 7);
			float cosA = Mth.cos(angleDrift);
			float sinA = Mth.sin(angleDrift);
			float timeOffset = time * speed * 0.1f;

			int baseRed = (int) (90 + 70 * brightness);
			int baseGreen = (int) (5 + 12 * brightness);
			int baseBlue = (int) (5 + 8 * brightness);

			for (int step = 0; step < length; step++) {
				float t = step * 0.015f;
				float squiggle = amplitude * Mth.sin(frequency * step + timeOffset);
				float displacement = squiggle;

				float pz = sideZ + startZ + t * cosA - displacement * sinA;
				float py = sideY + startY + t * sinA + displacement * cosA;

				if (py > VEIN_ZONE_HEIGHT || py < 0) continue;
				if (Math.abs(pz) > halfDepth) continue;

				float tipFade = 1f;
				if (step < 4) tipFade = step / 4f;
				else if (step > length - 4) tipFade = (length - step) / 4f;

				float pulse = 0.6f + 0.4f * Mth.sin(time * 0.08f + i * 0.7f + step * 0.06f);
				int alpha = (int) (Mth.clamp(tipFade * pulse * 180, 15, 200));
				int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
				int g = (int) Mth.clamp(baseGreen * pulse * 0.4f, 0, 255);
				int b = (int) Mth.clamp(baseBlue * pulse * 0.3f, 0, 255);

				float size = 0.012f;
				float x = sideX;

				vc.vertex(mat, x, py - size, pz - size).color(r, g, b, alpha).endVertex();
				vc.vertex(mat, x, py + size, pz - size).color(r, g, b, alpha).endVertex();
				vc.vertex(mat, x, py + size, pz + size).color(r, g, b, alpha).endVertex();
				vc.vertex(mat, x, py - size, pz + size).color(r, g, b, alpha).endVertex();
			}
		}
	}

	// ── Eye overlay ──────────────────────────────────────────────────────────────

	/**
	 * Renders three vertically-almond-shaped blood-eyes on the top half of the front
	 * face: a larger centre eye flanked by two smaller side eyes, matching the
	 * three-pupil motif from the reference art.
	 *
	 * cz is POSITIVE — the front face is +Z in Pass-2's rotated local space.
	 */
	private void renderEyeOverlay(PoseStack ms, MultiBufferSource bufferIn, float time) {
		VertexConsumer vc = bufferIn.getBuffer(RenderTypeInit.RADIANT_RENDER_TYPE);

		float cz = +0.252f;
		float tiltDeg = 22f; // outward tilt for flanking eyes

		// Each eye pulses at a slightly different phase so they feel independent
		float pulseC = 0.75f + 0.25f * Mth.sin(time * 0.05f);
		float beatC  = 0.60f + 0.40f * Mth.sin(time * 0.12f + 1.2f);
		float pulseL = 0.75f + 0.25f * Mth.sin(time * 0.05f + 1.1f);
		float beatL  = 0.60f + 0.40f * Mth.sin(time * 0.12f + 2.3f);
		float pulseR = 0.75f + 0.25f * Mth.sin(time * 0.05f + 2.2f);
		float beatR  = 0.60f + 0.40f * Mth.sin(time * 0.12f + 0.5f);

		// Left eye — rotated counterclockwise so top tip points outward
		ms.pushPose();
		ms.translate(-0.245f, 1.35f, 0f);
		ms.mulPose(Vector3.ZP.rotationDegrees(-tiltDeg).toMoj());
		renderSingleEye(ms.last().pose(), vc, 0f, 0f, cz, 0.72f, time, pulseL, beatL);
		ms.popPose();

		// Centre eye — upright
		renderSingleEye(ms.last().pose(), vc, 0.000f, 1.42f, cz, 0.90f, time, pulseC, beatC);

		// Right eye — rotated clockwise so top tip points outward
		ms.pushPose();
		ms.translate(+0.245f, 1.35f, 0f);
		ms.mulPose(Vector3.ZP.rotationDegrees(+tiltDeg).toMoj());
		renderSingleEye(ms.last().pose(), vc, 0f, 0f, cz, 0.72f, time, pulseR, beatR);
		ms.popPose();
	}

	/**
	 * Draws one almond-shaped eye centred at (cx, cy) scaled by {@code sc}.
	 * No sclera ring — layers: outer glow → eyelid arcs → iris → spokes → pupil → highlight.
	 */
	private void renderSingleEye(Matrix4f mat, VertexConsumer vc,
			float cx, float cy, float cz, float sc,
			float time, float pulse, float beat) {

		float glowInSX  = sc * 0.120f, glowInSY  = sc * 0.185f;
		float glowOutSX = sc * 0.165f, glowOutSY = sc * 0.245f;
		float irisSX    = sc * 0.088f, irisSY    = sc * 0.145f;
		float pupilSX   = sc * 0.040f, pupilSY   = sc * 0.065f;

		// ── 1. Outer diffuse glow halo ──
		drawEllipseRing(mat, vc, cx, cy, cz,
				glowInSX, glowInSY, glowOutSX, glowOutSY,
				180, 8, 8, (int)(40 * pulse), 28);

		// ── 2. Eyelid arcs — almond corners meet at ±glowInSX ──
		float lidEA = sc * 0.235f, lidEB = glowInSX;
		int lidR = (int)(155 * pulse), lidG = (int)(10 * pulse), lidB = (int)(10 * pulse);
		int lidAlpha = (int)(200 * pulse);
		drawEyelidArc(mat, vc, cx, cy, cz + 0.001f,  lidEA, lidEB, 0, (float) Math.PI,
				lidR, lidG, lidB, lidAlpha, 34);
		drawEyelidArc(mat, vc, cx, cy, cz + 0.001f, -lidEA, lidEB, 0, (float) Math.PI,
				lidR, lidG, lidB, lidAlpha, 34);

		// ── 3. Red iris (filled vertical ellipse) ──
		drawFilledEllipse(mat, vc, cx, cy, cz + 0.002f, irisSX, irisSY,
				145, 8, 8, (int)(215 * pulse), 24);

		// ── 4. Radiant spokes along the elliptical iris edge ──
		float spkInSX = sc * 0.048f, spkInSY = sc * 0.078f;
		float spkOutSX = irisSX - sc * 0.008f, spkOutSY = irisSY - sc * 0.010f;
		float spkHW = sc * 0.0050f;
		int spkR = (int)(200 * pulse), spkG = (int)(18 * pulse), spkB = (int)(12 * pulse);
		int spkA = (int)(155 * pulse);
		for (int s = 0; s < 12; s++) {
			double angle = Math.PI * 2.0 * s / 12.0 + time * 0.003;
			float cosA = (float) Math.cos(angle);
			float sinA = (float) Math.sin(angle);
			float ix = cx + cosA * spkInSX,  iy = cy + sinA * spkInSY;
			float ox = cx + cosA * spkOutSX, oy = cy + sinA * spkOutSY;
			float dx = ox - ix, dy = oy - iy;
			float len = Mth.sqrt(dx * dx + dy * dy);
			if (len < 1e-6f) continue;
			float nx = -dy / len * spkHW, ny = dx / len * spkHW;
			vc.vertex(mat, ix + nx, iy + ny, cz + 0.003f).color(spkR, spkG, spkB, spkA).endVertex();
			vc.vertex(mat, ix - nx, iy - ny, cz + 0.003f).color(spkR, spkG, spkB, spkA).endVertex();
			vc.vertex(mat, ox - nx, oy - ny, cz + 0.003f).color(spkR, spkG, spkB, spkA).endVertex();
			vc.vertex(mat, ox + nx, oy + ny, cz + 0.003f).color(spkR, spkG, spkB, spkA).endVertex();
		}

		// ── 5. Dark pupil ──
		drawFilledEllipse(mat, vc, cx, cy, cz + 0.004f, pupilSX, pupilSY,
				6, 0, 0, 245, 20);

		// ── 6. Bright highlight dot ──
		drawFilledEllipse(mat, vc,
				cx - sc * 0.014f, cy + sc * 0.020f, cz + 0.005f,
				sc * 0.013f, sc * 0.013f,
				255, 245, 235, (int)(205 * beat), 12);
	}

	// ── Back-face symbol ─────────────────────────────────────────────────────────

	/**
	 * Renders an 8-pointed star (two overlapping squares with their diagonals) on
	 * the back face of the monolith. Dark red traces the N/E/S/W diamond + cross;
	 * orange traces the NE/SE/SW/NW axis-aligned square + X diagonals.
	 * Both layers pulse and fade independently.
	 */
	/**
	 * Renders a regular {8/3} octagram on the back face: 8 vertices equally spaced
	 * on a circle, each connected to the vertex 3 steps away (skip-2). Even-indexed
	 * chords are dark red, odd-indexed chords are orange — matching the two
	 * interlocking tetragram halves of the reference image. Both colour groups pulse
	 * independently.
	 */
	private void renderBackSymbol(PoseStack ms, MultiBufferSource bufferIn, float time) {
		VertexConsumer vc = bufferIn.getBuffer(RenderTypeInit.RADIANT_RENDER_TYPE);
		Matrix4f mat = ms.last().pose();

		float cz = -0.252f; // back face (+z = front, -z = back in Pass-2 space)
		float cy = 1.0f;    // vertical centre of the 2-block face
		float R  = 0.400f;

		// 8 octagon vertices, starting from N and going clockwise
		float[] vx = new float[8];
		float[] vy = new float[8];
		for (int i = 0; i < 8; i++) {
			double a = Math.PI / 2.0 + Math.PI / 8.0 - i * Math.PI / 4.0; // +22.5° offset → two vertices at bottom
			vx[i] = (float)(R * Math.cos(a));
			vy[i] = cy + (float)(R * Math.sin(a));
		}

		int drR = 175, drG = 22, drB = 22;   // dark red  (even chords)
		int orR = 228, orG = 115, orB = 20;  // orange    (odd chords)

		float pulDR = 0.50f + 0.50f * Mth.sin(time * 0.022f);
		float pulOR = 0.50f + 0.50f * Mth.sin(time * 0.022f + 1.5f);

		float lw = 0.009f;  // core half-width
		float gw = 0.020f;  // glow half-width
		float czo = cz - 0.001f; // core lines slightly in front of glow layer

		// Draw all 8 {8/3} chords: vertex k → vertex (k+3) mod 8
		for (int k = 0; k < 8; k++) {
			int j = (k + 3) % 8;
			boolean even = (k % 2 == 0);
			int r  = even ? drR : orR;
			int g  = even ? drG : orG;
			int b  = even ? drB : orB;
			int a  = (int)(210 * (even ? pulDR : pulOR));
			int ag = (int)( 60 * (even ? pulDR : pulOR));
			drawLine(mat, vc, vx[k], vy[k], vx[j], vy[j], cz,  gw, r, g, b, ag);
			drawLine(mat, vc, vx[k], vy[k], vx[j], vy[j], czo, lw, r, g, b, a);
		}
	}

	/** Draws a thin rectangle (ribbon) between two points, for crisp line rendering. */
	private void drawLine(Matrix4f mat, VertexConsumer vc,
			float x1, float y1, float x2, float y2, float z,
			float halfW, int r, int g, int b, int alpha) {
		float dx = x2 - x1, dy = y2 - y1;
		float len = Mth.sqrt(dx * dx + dy * dy);
		if (len < 1e-6f) return;
		float px = -dy / len * halfW;
		float py =  dx / len * halfW;
		vc.vertex(mat, x1 + px, y1 + py, z).color(r, g, b, alpha).endVertex();
		vc.vertex(mat, x1 - px, y1 - py, z).color(r, g, b, alpha).endVertex();
		vc.vertex(mat, x2 - px, y2 - py, z).color(r, g, b, alpha).endVertex();
		vc.vertex(mat, x2 + px, y2 + py, z).color(r, g, b, alpha).endVertex();
	}

	// ── Geometry helpers ─────────────────────────────────────────────────────────

	/**
	 * Draws a filled ellipse using a degenerate-quad fan.
	 * Pass equal semiX/semiY for a circle.
	 */
	private void drawFilledEllipse(Matrix4f mat, VertexConsumer vc,
			float cx, float cy, float cz, float semiX, float semiY,
			int r, int g, int b, int alpha, int segments) {
		for (int i = 0; i < segments; i++) {
			double a1 = 2.0 * Math.PI * i / segments;
			double a2 = 2.0 * Math.PI * (i + 1) / segments;
			float x1 = cx + (float)(Math.cos(a1) * semiX);
			float y1 = cy + (float)(Math.sin(a1) * semiY);
			float x2 = cx + (float)(Math.cos(a2) * semiX);
			float y2 = cy + (float)(Math.sin(a2) * semiY);
			// Degenerate quad → triangle (center, center, edge1, edge2)
			vc.vertex(mat, cx, cy, cz).color(r, g, b, alpha).endVertex();
			vc.vertex(mat, cx, cy, cz).color(r, g, b, alpha).endVertex();
			vc.vertex(mat, x1, y1, cz).color(r, g, b, alpha).endVertex();
			vc.vertex(mat, x2, y2, cz).color(r, g, b, alpha).endVertex();
		}
	}

	/**
	 * Draws an annular elliptical ring between an inner ellipse (innerSX, innerSY)
	 * and an outer ellipse (outerSX, outerSY).
	 */
	private void drawEllipseRing(Matrix4f mat, VertexConsumer vc,
			float cx, float cy, float cz,
			float innerSX, float innerSY, float outerSX, float outerSY,
			int r, int g, int b, int alpha, int segments) {
		for (int i = 0; i < segments; i++) {
			double a1 = 2.0 * Math.PI * i / segments;
			double a2 = 2.0 * Math.PI * (i + 1) / segments;
			float ix1 = cx + (float)(Math.cos(a1) * innerSX);
			float iy1 = cy + (float)(Math.sin(a1) * innerSY);
			float ix2 = cx + (float)(Math.cos(a2) * innerSX);
			float iy2 = cy + (float)(Math.sin(a2) * innerSY);
			float ox1 = cx + (float)(Math.cos(a1) * outerSX);
			float oy1 = cy + (float)(Math.sin(a1) * outerSY);
			float ox2 = cx + (float)(Math.cos(a2) * outerSX);
			float oy2 = cy + (float)(Math.sin(a2) * outerSY);

			vc.vertex(mat, ix1, iy1, cz).color(r, g, b, alpha).endVertex();
			vc.vertex(mat, ox1, oy1, cz).color(r, g, b, alpha).endVertex();
			vc.vertex(mat, ox2, oy2, cz).color(r, g, b, alpha).endVertex();
			vc.vertex(mat, ix2, iy2, cz).color(r, g, b, alpha).endVertex();
		}
	}

	/**
	 * Draws one eyelid arc as a thin ribbon along an ellipse from angle 0 to
	 * {@code endAngle}. {@code semiA} is the vertical semi-axis (negative to
	 * flip for the lower lid), {@code semiB} is the horizontal semi-axis.
	 */
	private void drawEyelidArc(Matrix4f mat, VertexConsumer vc,
			float cx, float cy, float cz,
			float semiA, float semiB, float startAngle, float endAngle,
			int r, int g, int b, int alpha, int segments) {
		float halfW = 0.006f; // half-width of the arc ribbon
		for (int i = 0; i < segments; i++) {
			float t1 = startAngle + (endAngle - startAngle) * i / segments;
			float t2 = startAngle + (endAngle - startAngle) * (i + 1) / segments;

			float x1 = cx + semiB * Mth.cos(t1);
			float y1 = cy + semiA * Mth.sin(t1);
			float x2 = cx + semiB * Mth.cos(t2);
			float y2 = cy + semiA * Mth.sin(t2);

			// Perpendicular to the arc segment for ribbon width
			float dx = x2 - x1, dy = y2 - y1;
			float len = Mth.sqrt(dx * dx + dy * dy);
			if (len < 1e-6f) continue;
			float nx = -dy / len * halfW;
			float ny =  dx / len * halfW;

			vc.vertex(mat, x1 + nx, y1 + ny, cz).color(r, g, b, alpha).endVertex();
			vc.vertex(mat, x1 - nx, y1 - ny, cz).color(r, g, b, alpha).endVertex();
			vc.vertex(mat, x2 - nx, y2 - ny, cz).color(r, g, b, alpha).endVertex();
			vc.vertex(mat, x2 + nx, y2 + ny, cz).color(r, g, b, alpha).endVertex();
		}
	}
}

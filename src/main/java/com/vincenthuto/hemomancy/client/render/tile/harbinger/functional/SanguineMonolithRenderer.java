package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.SanguineMonolithModel;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.SanguineMonolithBlock;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.client.render.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineMonolithBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.joml.Matrix4f;

/**
 * Renders the Sanguine Monolith as a black slab (1 wide × 2 tall × 0.5 deep)
 * with an animated red vein pattern crawling over the bottom third, a glowing
 * blood-eye on the top half of the front face, and a rising-from-ground
 * animation on first placement.
 */
public class SanguineMonolithRenderer implements BlockEntityRenderer<SanguineMonolithBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/model_sanguine_monolith.png");

	private final SanguineMonolithModel model;
	private final com.vincenthuto.hemomancy.client.model.tile.functional.SanguineMonolithHalvesModel halves;
	/** The break between the halves: rows up the stone, columns through its depth, and where the boxes stop short of centre. */
	private static final int BREAK_ROWS = 16;
	private static final int BREAK_COLUMNS = 2;
	private static final float BREAK_BASE = 2.5f / 16f;
	private static final float BREAK_REACH = 0.135f;
	private static final float SLAB_DEPTH = 0.25f;
	/** Baked rubble per stone position: x, y, z and grey for each vertex. Seeded by the position, so it never shuffles. */
	private static final java.util.Map<Long, float[]> RUBBLE_MESHES = new java.util.HashMap<>();
	/** The heap is a block across: its mound, baked at {@link #MOUND_REACH}, is scaled down to this radius. */
	private static final float RUBBLE_MAX_RADIUS = 1;
	/** How far the baked mound reaches from the fulcrum before scaling; pebbles are kept within it. */
	private static final float MOUND_REACH = 1.9f;
	private static final float FOG_RADIUS =0.75f;

	// ── Vein animation parameters ──
	private static final float VEIN_ZONE_HEIGHT = 0.667f; // bottom third of 2-block height
	private static final int TIER_FIVE = 5;
	private static final int TIER_SEVEN = 7;
	private static final int TIER_RANGE = TIER_SEVEN - TIER_FIVE;
	/** The vein quads sit a hair off the slab (16 by 8 wide) and move with its surface, so they never hover or sink. */
	private static final float VEIN_OVERLAY_X = 0.506f;
	private static final float VEIN_OVERLAY_Z = 0.256f;
	/** The eyes and the back symbol sit the same hair off the slab's front and back and breathe with it. */
	private static final float EMBLEM_FRONT_Z = 0.258f;
	private static final float EMBLEM_BACK_Z = -0.258f;
	/**
	 * Which diagonal each slab face's quad splits on, from the cube's vertex order ({@code ModelPart.Cube}: the north
	 * face runs (x2,y1) (x1,y1) (x1,y2) (x2,y2), the south face (x1,y1) (x2,y1) (x2,y2) (x1,y2); a quad's two triangles
	 * share its first and third corners; the stone is drawn turned over, so the north face is its front). 0 is the
	 * bottom-left to top-right split, 1 the other.
	 */
	private static final float FRONT_FACE_DIAGONAL = 0f;
	private static final float BACK_FACE_DIAGONAL = 1f;
	/** The surface shader's breathing, as the standing stone is drawn with it (item/monolith_fragment.vsh). */
	private static final float SURFACE_BURDEN = 0.45f;
	private static final float SURFACE_ATTUNED = 0.88f;
	private static final float SURFACE_MORPH_STRENGTH = 0.014f;

	/** How far below the base the monolith starts when first placed. */
	private static final float RISE_Y_OFFSET = 2.5f;

	public SanguineMonolithRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new SanguineMonolithModel(context.bakeLayer(SanguineMonolithModel.LAYER_LOCATION));
		this.halves = new com.vincenthuto.hemomancy.client.model.tile.functional.SanguineMonolithHalvesModel(
				context.bakeLayer(com.vincenthuto.hemomancy.client.model.tile.functional.SanguineMonolithHalvesModel.LAYER_LOCATION));
	}

	@Override
	public boolean shouldRenderOffScreen(SanguineMonolithBlockEntity te) {
		return true;
	}

	/**
	 * The whole stone and what the break throws out: the halves swing past the block's sides and the rubble, fog and
	 * portal spread around the base. Without this the cull box is the base block alone, and looking up past it drops
	 * the stone.
	 */
	@Override
	public net.minecraft.world.phys.AABB getRenderBoundingBox(SanguineMonolithBlockEntity te) {
		return com.vincenthuto.hemomancy.common.tile.shared.IMultiBlockEntity.computeMultiBlockAABB(te).inflate(3.5, 1.0, 3.5);
	}

	@Override
	public void render(SanguineMonolithBlockEntity te, float partialTicks, PoseStack ms, MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn) {
		if (te.isBreaking() || te.isBroken()) {
			renderBroken(te, partialTicks, ms, bufferIn, combinedLightIn);
			return;
		}

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

		float shaderSeed = Math.floorMod(te.getBlockPos().asLong(), 10007L) / 10007.0f;
		VertexConsumer vertexConsumer = bufferIn.getBuffer(
				HemoRenderTypes.monolithEntitySurface(timeSeconds(te, partialTicks), shaderSeed, SURFACE_BURDEN, SURFACE_ATTUNED, 7.5f));
		model.renderToBuffer(ms, vertexConsumer, combinedLightIn, OverlayTexture.NO_OVERLAY, 0xFF0D0505);
		ms.popPose();

		// ── Pass 2: Animated vein overlay + eye (bottom third + top half front face) ──
		float time = (te.getLevel() != null)
				? AnimationTime.gameTime(te.getLevel(), partialTicks)
				: te.getTickCount() + partialTicks;
		TendrilVisualState tendrilVisual = computeTendrilVisualState(te);

		ms.pushPose();
		ms.translate(0.5D, yOffset, 0.5D);
		ms.mulPose(Vector3.YP.rotationDegrees(-yRot).toMoj());

		renderVeinOverlay(ms, bufferIn, time, shaderSeed, tendrilVisual, timeSeconds(te, partialTicks));
		// Each emblem is told the slab face it lies on (camera-relative), so it moves as that face moves.
		float seconds = timeSeconds(te, partialTicks);
		renderEyeOverlay(ms, emblemConsumer(ms, bufferIn, shaderSeed, seconds, 0.25f, FRONT_FACE_DIAGONAL), time);
		renderBackSymbol(ms, emblemConsumer(ms, bufferIn, shaderSeed, seconds, -0.25f, BACK_FACE_DIAGONAL), time);
		ms.popPose();
	}

	private static float timeSeconds(SanguineMonolithBlockEntity te, float partialTicks) {
		if (te.getLevel() != null) {
			return AnimationTime.gameTime(te.getLevel(), partialTicks) / 20.0f;
		}
		return (te.getTickCount() + partialTicks) / 20.0f;
	}

	// ── Vein overlay ─────────────────────────────────────────────────────────────

	/** The emblem type for the slab face at depth {@code z} of the pass-2 pose: its corners carried to camera space. */
	private static VertexConsumer emblemConsumer(PoseStack ms, MultiBufferSource bufferIn, float seed, float seconds, float z,
			float diagonal) {
		Matrix4f mat = ms.last().pose();
		org.joml.Vector3f origin = mat.transformPosition(new org.joml.Vector3f(-0.5f, 0f, z));
		org.joml.Vector3f right = mat.transformPosition(new org.joml.Vector3f(0.5f, 0f, z)).sub(origin);
		org.joml.Vector3f up = mat.transformPosition(new org.joml.Vector3f(-0.5f, 2f, z)).sub(origin);
		// Keyed by face so each keeps its own uniforms; the morph's seed is the stone's for both.
		return bufferIn.getBuffer(HemoRenderTypes.monolithEmblem(seed + (z < 0f ? 0.5f : 0.0f), seed, seconds, surfaceMorphStrength(), origin, right, up, diagonal));
	}

	/** How far the surface shader pushes a vertex in and out, with the burden and attunement the stone is drawn with. */
	private static float surfaceMorphStrength() {
		return SURFACE_MORPH_STRENGTH * (1.0f + SURFACE_BURDEN * 0.45f + SURFACE_ATTUNED * 0.25f);
	}

	/**
	 * The tendrils, one shader quad on each face; u runs around the perimeter so they carry over the corners. The
	 * quads breathe with the stone's surface ({@code morphTime} is the surface shader's clock, in seconds).
	 */
	private void renderVeinOverlay(PoseStack ms, MultiBufferSource bufferIn, float time, float seed, TendrilVisualState state,
			float morphTime) {
		float red = Mth.lerp(state.colorMix, 0.72f, state.targetR / 255f);
		float green = Mth.lerp(state.colorMix, 0.028f, state.targetG / 255f);
		float blue = Mth.lerp(state.colorMix, 0.02f, state.targetB / 255f);
		VertexConsumer vc = bufferIn.getBuffer(HemoRenderTypes.monolithVeins(time, seed, state.zoneHeight, red, green, blue,
				morphTime, surfaceMorphStrength()));
		PoseStack.Pose pose = ms.last();
		float x = VEIN_OVERLAY_X;
		float z = VEIN_OVERLAY_Z;
		float top = 2f;
		veinFace(vc, pose, -x, z, x, z, 0f, 1f, top);
		veinFace(vc, pose, x, z, x, -z, 1f, 1.5f, top);
		veinFace(vc, pose, x, -z, -x, -z, 1.5f, 2.5f, top);
		veinFace(vc, pose, -x, -z, -x, z, 2.5f, 3f, top);
	}

	private static void veinFace(VertexConsumer vc, PoseStack.Pose pose, float x0, float z0, float x1, float z1, float u0, float u1, float top) {
		veinVertex(vc, pose, x0, 0f, z0, u0, 0f);
		veinVertex(vc, pose, x1, 0f, z1, u1, 0f);
		veinVertex(vc, pose, x1, top, z1, u1, top);
		veinVertex(vc, pose, x0, top, z0, u0, top);
	}

	private static void veinVertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, float u, float v) {
		vc.addVertex(pose.pose(), x, y, z).setColor(1f, 1f, 1f, 1f).setUv(u, v).setLight(0xF000F0).setNormal(pose, 0f, 0f, 1f);
	}

	private TendrilVisualState computeTendrilVisualState(SanguineMonolithBlockEntity te) {
		int degree = getClientPlayerDegree();
		float growthProgress = Mth.clamp((degree - TIER_FIVE) / (float) TIER_RANGE, 0.0f, 1.0f);
		float lengthScale = 1.0f + growthProgress * 0.65f;
		float zoneHeight = Mth.clamp(VEIN_ZONE_HEIGHT * (1.0f + growthProgress * 0.45f), VEIN_ZONE_HEIGHT, 1.0f);
		boolean whitePhase = te.getArchonInteractions() > 0;

		if (whitePhase) {
			return new TendrilVisualState(lengthScale, zoneHeight, 1.0f, 245, 245, 245, true);
		}

		return new TendrilVisualState(lengthScale, zoneHeight, growthProgress, 228, 120, 30, false);
	}

	private int getClientPlayerDegree() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return 0;
		}
		return HemoCapabilityAccess.getInitiatoryDegree(mc.player)
				.getDegreeNumber();
	}

	private record TendrilVisualState(float lengthScale, float zoneHeight, float colorMix, int targetR, int targetG,
			int targetB, boolean whitePhase) {}

	// ── Eye overlay ──────────────────────────────────────────────────────────────

	/**
	 * Renders three vertically-almond-shaped blood-eyes on the top half of the front
	 * face: a larger centre eye flanked by two smaller side eyes, matching the
	 * three-pupil motif from the reference art.
	 *
	 * cz is POSITIVE — the front face is +Z in Pass-2's rotated local space.
	 */
	private void renderEyeOverlay(PoseStack ms, VertexConsumer vc, float time) {
		float cz = EMBLEM_FRONT_Z;
		float tiltDeg = -22f; // outward tilt for flanking eyes

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
			vc.addVertex(mat, ix + nx, iy + ny, cz + 0.003f).setColor(spkR, spkG, spkB, spkA);
			vc.addVertex(mat, ix - nx, iy - ny, cz + 0.003f).setColor(spkR, spkG, spkB, spkA);
			vc.addVertex(mat, ox - nx, oy - ny, cz + 0.003f).setColor(spkR, spkG, spkB, spkA);
			vc.addVertex(mat, ox + nx, oy + ny, cz + 0.003f).setColor(spkR, spkG, spkB, spkA);
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
	 * The seal on the back of the stone: the {8/3} octagram, one line through eight points, in a thin outer ring, a
	 * node at each point lit one after another as a pulse goes round, a lacquer ring inside, and at the centre the
	 * stone's third eye shut to a slit. Dark red with a bright core; the lacquer orange kept to the inner ring and the
	 * slit.
	 */
	private void renderBackSymbol(PoseStack ms, VertexConsumer vc, float time) {
		Matrix4f mat = ms.last().pose();
		float cz = EMBLEM_BACK_Z; // back face (+z = front, -z = back in Pass-2 space)
		float cy = 1.25f;         // vertical centre of the 2-block face
		float outer = 0.44f;
		float star = 0.37f;
		float inner = 0.14f;
		float lw = 0.008f;        // core half-width
		float gw = 0.019f;        // glow half-width
		float czo = cz - 0.001f;  // cores slightly in front of the glow layer
		float czi = cz - 0.002f;  // the eye in front of both

		int dr = 150, dg = 14, db = 18;    // hematic dark red
		int br = 236, bg = 62, bb = 34;    // arterial bright
		int lr = 228, lg = 115, lb = 20;   // lacquer orange

		// The pulse goes round the eight points over about ten seconds; each point and each chord brightens as it passes.
		float round = time * 0.03f;
		final int points = 8;

		// Outer ring, faint.
		drawEllipseRing(mat, vc, 0f, cy, cz, outer - 0.004f, outer - 0.004f, outer + 0.004f, outer + 0.004f, dr, dg, db, 120, 64);

		// Eight points, a pair at the bottom; the star's chords run from each point to the third along, one line through
		// all eight.
		float[] px = new float[points];
		float[] py = new float[points];
		for (int i = 0; i < points; i++) {
			double a = Math.PI / 2.0 + Math.PI / 8.0 - i * Math.PI / 4.0;
			px[i] = (float) (star * Math.cos(a));
			py[i] = cy + (float) (star * Math.sin(a));
		}
		for (int e = 0; e < points; e++) {
			int from = e;
			int to = (e + 3) % points;
			float lit = 0.55f + 0.45f * Mth.sin(round - e * (Mth.TWO_PI / points));
			int core = (int) (120 + 110 * lit);
			int glow = (int) (30 + 45 * lit);
			drawLine(mat, vc, px[from], py[from], px[to], py[to], cz, gw, dr, dg, db, glow);
			drawLine(mat, vc, px[from], py[from], px[to], py[to], czo, lw, (int) Mth.lerp(lit, dr, br), (int) Mth.lerp(lit, dg, bg), (int) Mth.lerp(lit, db, bb), core);
		}

		// A node at each point, lit as the pulse reaches it.
		for (int i = 0; i < points; i++) {
			float lit = Mth.clamp(0.5f + 0.5f * Mth.sin(round - i * (Mth.TWO_PI / points)), 0f, 1f);
			float radius = 0.018f + 0.010f * lit;
			drawFilledEllipse(mat, vc, px[i], py[i], cz, radius * 1.9f, radius * 1.9f, dr, dg, db, (int) (40 + 60 * lit), 16);
			drawFilledEllipse(mat, vc, px[i], py[i], czo, radius, radius, (int) Mth.lerp(lit, dr, br), (int) Mth.lerp(lit, dg, bg), (int) Mth.lerp(lit, db, bb), (int) (150 + 100 * lit), 16);
		}

		// The lacquer ring inside, breathing slowly.
		float breath = 0.5f + 0.5f * Mth.sin(time * 0.045f + 0.8f);
		drawEllipseRing(mat, vc, 0f, cy, czo, inner - 0.005f, inner - 0.005f, inner + 0.005f, inner + 0.005f, lr, lg, lb, (int) (110 + 90 * breath), 48);

		// The third eye, shut: an almond of dark flesh with the slit a thread of lacquer light.
		drawFilledEllipse(mat, vc, 0f, cy, czo, 0.082f, 0.046f, 40, 4, 6, 230, 32);
		drawEllipseRing(mat, vc, 0f, cy, czi, 0.078f, 0.042f, 0.086f, 0.050f, dr, dg, db, 200, 32);
		drawFilledEllipse(mat, vc, 0f, cy, czi, 0.007f + 0.003f * breath, 0.040f, lr, lg, lb, (int) (190 + 60 * breath), 12);
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
		vc.addVertex(mat, x1 + px, y1 + py, z).setColor(r, g, b, alpha);
		vc.addVertex(mat, x1 - px, y1 - py, z).setColor(r, g, b, alpha);
		vc.addVertex(mat, x2 - px, y2 - py, z).setColor(r, g, b, alpha);
		vc.addVertex(mat, x2 + px, y2 + py, z).setColor(r, g, b, alpha);
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
			vc.addVertex(mat, cx, cy, cz).setColor(r, g, b, alpha);
			vc.addVertex(mat, cx, cy, cz).setColor(r, g, b, alpha);
			vc.addVertex(mat, x1, y1, cz).setColor(r, g, b, alpha);
			vc.addVertex(mat, x2, y2, cz).setColor(r, g, b, alpha);
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

			vc.addVertex(mat, ix1, iy1, cz).setColor(r, g, b, alpha);
			vc.addVertex(mat, ox1, oy1, cz).setColor(r, g, b, alpha);
			vc.addVertex(mat, ox2, oy2, cz).setColor(r, g, b, alpha);
			vc.addVertex(mat, ix2, iy2, cz).setColor(r, g, b, alpha);
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

			vc.addVertex(mat, x1 + nx, y1 + ny, cz).setColor(r, g, b, alpha);
			vc.addVertex(mat, x1 - nx, y1 - ny, cz).setColor(r, g, b, alpha);
			vc.addVertex(mat, x2 - nx, y2 - ny, cz).setColor(r, g, b, alpha);
			vc.addVertex(mat, x2 + nx, y2 + ny, cz).setColor(r, g, b, alpha);
		}
	}

	// ── The break: cracks, the fallen halves, rubble, fog, lightning and the portal ─────────────────────────────

	private void renderBroken(SanguineMonolithBlockEntity te, float partialTicks, PoseStack ms, MultiBufferSource bufferIn,
			int light) {
		Direction facing = te.getBlockState().getValue(FACING);
		float yRot = switch (facing) {
			case NORTH -> 180f;
			case EAST -> 270f;
			case SOUTH -> 0f;
			case WEST -> 90f;
			default -> 0f;
		};
		float time = te.getLevel() != null ? AnimationTime.gameTime(te.getLevel(), partialTicks) : te.getTickCount() + partialTicks;
		float shaderSeed = Math.floorMod(te.getBlockPos().asLong(), 10007L) / 10007.0f;
		float crack = te.crackProgress(partialTicks);
		float fallStart = (float) SanguineMonolithBlock.CRACK_TICKS / SanguineMonolithBlock.BREAK_TICKS;
		float fall = te.isBroken() ? 1f : Mth.clamp((te.breakProgress(partialTicks) - fallStart) / (1f - fallStart), 0f, 1f);

		VertexConsumer surface = bufferIn.getBuffer(
				HemoRenderTypes.monolithEntitySurface(timeSeconds(te, partialTicks), shaderSeed, 0.45f, 0.88f, 7.5f));
		ms.pushPose();
		ms.translate(0.5D, 1.5D, 0.5D);
		ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180f));
		ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yRot));
		if (fall <= 0f) {
			model.renderToBuffer(ms, surface, light, OverlayTexture.NO_OVERLAY, 0xFF0D0505);
		} else {
			// Thrown open by the blast.
			halves.setFall(blastAngle(fall));
			halves.renderToBuffer(ms, surface, light, OverlayTexture.NO_OVERLAY, 0xFF0D0505);
		}
		ms.popPose();

		ms.pushPose();
		ms.translate(0.5D, 0.0D, 0.5D);
		ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-yRot));
		float[][] breakGrid = breakProfile(te.getBlockPos());
		float lean = fall > 0f ? blastAngle(fall) : 0f;
		if (fall > 0f) {
			renderRubble(ms, bufferIn, te.getBlockPos(), fall);
			renderBreakSolid(ms, bufferIn, breakGrid, lean);
		}
		// The cracks' light: full through the break, then the husk's own, dark when shut and filling as it is fed.
		float fill = te.isBroken() ? te.glow(partialTicks) : 1f;
		if (crack > 0f) renderFissures(ms, bufferIn, breakGrid, time, fall > 0f ? 1f : crack, shaderSeed, lean, fall > 0f, te, fill);
		if (te.isBroken()) renderPortal(ms, bufferIn, te, time, partialTicks, facing);
		ms.popPose();
		if (te.isBroken()) {
			renderFog(ms, bufferIn, te, time, partialTicks);
			strikeLightning(te, partialTicks);
		}
	}

	/** The swing of a half as the blast throws it: all the speed at once, past its rest, then settling back. */
	private static float blastAngle(float fall) {
		float t = fall - 1f;
		float thrown = 1f + 2.7f * t * t * t + 1.7f * t * t;
		return (float) Math.toRadians(45f * thrown);
	}

	/**
	 * The jagged break, shared by both halves so they part along one line: the x of the break at each row and column.
	 * The last entry of the result holds the height of each row.
	 */
	private static float[][] breakProfile(net.minecraft.core.BlockPos pos) {
		java.util.Random random = new java.util.Random(pos.asLong() ^ 0x5EEDL);
		float[][] grid = new float[BREAK_ROWS + 2][BREAK_COLUMNS];
		float[] heights = new float[BREAK_ROWS + 1];
		float x = (random.nextFloat() - 0.5f) * 0.08f;
		float direction = random.nextBoolean() ? 1f : -1f;
		for (int row = 0; row <= BREAK_ROWS; row++) {
			float jitter = row == 0 || row == BREAK_ROWS ? 0f : (random.nextFloat() - 0.5f) * 0.05f;
			heights[row] = 2f * row / BREAK_ROWS + jitter;
			// A long lean one way, then a sharp snap back: stone, not a sine wave.
			if (random.nextFloat() < 0.45f) direction = -direction;
			x = Mth.clamp(x + direction * (0.02f + random.nextFloat() * 0.09f), -BREAK_REACH + 0.03f, BREAK_REACH - 0.03f);
			for (int column = 0; column < BREAK_COLUMNS; column++) {
				grid[row][column] = Mth.clamp(x + (random.nextFloat() - 0.5f) * 0.06f, -BREAK_REACH, BREAK_REACH);
			}
		}
		grid[BREAK_ROWS + 1] = heights;
		return grid;
	}

	private static float breakDepth(int column) {
		return -SLAB_DEPTH + 2f * SLAB_DEPTH * column / (BREAK_COLUMNS - 1);
	}

	private static void hinge(PoseStack ms, int side, float lean) {
		ms.translate(side * 0.5D, 0.0D, 0.0D);
		ms.mulPose(com.mojang.math.Axis.ZP.rotation(-side * lean));
	}

	/** The solid of the break on each half once it has parted: the jagged surface, its side walls and its top. */
	private static void renderBreakSolid(PoseStack ms, MultiBufferSource bufferIn, float[][] grid, float lean) {
		VertexConsumer vc = bufferIn.getBuffer(RenderTypeInit.MONOLITH_RUBBLE);
		float[] heights = grid[BREAK_ROWS + 1];
		for (int side = -1; side <= 1; side += 2) {
			ms.pushPose();
			hinge(ms, side, lean);
			Matrix4f mat = ms.last().pose();
			float origin = side * 0.5f;
			float base = side * BREAK_BASE - origin;
			for (int row = 0; row < BREAK_ROWS; row++) {
				float y0 = heights[row];
				float y1 = heights[row + 1];
				for (int column = 0; column < BREAK_COLUMNS - 1; column++) {
					float z0 = breakDepth(column);
					float z1 = breakDepth(column + 1);
					float[] a = { grid[row][column] - origin, y0, z0 };
					float[] b = { grid[row][column + 1] - origin, y0, z1 };
					float[] c = { grid[row + 1][column + 1] - origin, y1, z1 };
					float[] d = { grid[row + 1][column] - origin, y1, z0 };
					blackTriangle(vc, mat, a, b, c);
					blackTriangle(vc, mat, a, c, d);
				}
				for (int end = 0; end < 2; end++) {
					int column = end == 0 ? 0 : BREAK_COLUMNS - 1;
					float z = breakDepth(column);
					float[] a = { base, y0, z };
					float[] b = { grid[row][column] - origin, y0, z };
					float[] c = { grid[row + 1][column] - origin, y1, z };
					float[] d = { base, y1, z };
					blackTriangle(vc, mat, a, b, c);
					blackTriangle(vc, mat, a, c, d);
				}
			}
			for (int column = 0; column < BREAK_COLUMNS - 1; column++) {
				float z0 = breakDepth(column);
				float z1 = breakDepth(column + 1);
				float[] a = { base, 2f, z0 };
				float[] b = { grid[BREAK_ROWS][column] - origin, 2f, z0 };
				float[] c = { grid[BREAK_ROWS][column + 1] - origin, 2f, z1 };
				float[] d = { base, 2f, z1 };
				blackTriangle(vc, mat, a, b, c);
				blackTriangle(vc, mat, a, c, d);
			}
			ms.popPose();
		}
	}

	private static void blackTriangle(VertexConsumer vc, Matrix4f mat, float[] a, float[] b, float[] c) {
		for (float[] p : new float[][] { a, b, c }) {
			vc.addVertex(mat, p[0], p[1], p[2]).setColor(0.012f, 0.004f, 0.005f, 1f);
		}
	}

	/**
	 * The fissure shader over the stone: on the front and back of each half, from its far edge to the jagged break
	 * (vertex red runs 0 to 1 across, so the fault follows the break), and over the raw break once the halves have parted.
	 */
	private static void renderFissures(PoseStack ms, MultiBufferSource bufferIn, float[][] grid, float time, float progress,
			float seed, float lean, boolean parted, SanguineMonolithBlockEntity te, float fill) {
		boolean overgrown = te.overgrown();
		VertexConsumer vc = bufferIn.getBuffer(HemoRenderTypes.monolithFissure(time, progress, seed, overgrown, fill));
		float[] heights = grid[BREAK_ROWS + 1];
		// Clear of the surface shader, whose vertex morph moves the stone up to 0.02 in and out over time.
		float skin = 0.024f;
		for (int side = -1; side <= 1; side += 2) {
			ms.pushPose();
			hinge(ms, side, lean);
			PoseStack.Pose pose = ms.last();
			float origin = side * 0.5f;
			for (int end = 0; end < 2; end++) {
				int column = end == 0 ? 0 : BREAK_COLUMNS - 1;
				float z = breakDepth(column) + (end == 0 ? -skin : skin);
				for (int row = 0; row < BREAK_ROWS; row++) {
					float y0 = heights[row];
					float y1 = heights[row + 1];
					float x0 = grid[row][column];
					float x1 = grid[row + 1][column];
					fissureVertex(vc, pose, 0f, y0, z, 0f, 1f, 0f, origin, y0);
					fissureVertex(vc, pose, x0 - origin, y0, z, 1f, 1f, 0f, x0, y0);
					fissureVertex(vc, pose, x1 - origin, y1, z, 1f, 1f, 0f, x1, y1);
					fissureVertex(vc, pose, 0f, y1, z, 0f, 1f, 0f, origin, y1);
				}
			}
			if (overgrown) {
				// The infestation has the whole stone, not only its faces: its outer side and its top as well.
				float out = -side * -skin;
				float base = side * BREAK_BASE - origin;
				float u = side * 0.3f;
				fissureVertex(vc, pose, out, 0f, -SLAB_DEPTH, 0f, 1f, 0.5f, u - SLAB_DEPTH, 0f);
				fissureVertex(vc, pose, out, 0f, SLAB_DEPTH, 0f, 1f, 0.5f, u + SLAB_DEPTH, 0f);
				fissureVertex(vc, pose, out, 2f, SLAB_DEPTH, 0f, 1f, 0.5f, u + SLAB_DEPTH, 2f);
				fissureVertex(vc, pose, out, 2f, -SLAB_DEPTH, 0f, 1f, 0.5f, u - SLAB_DEPTH, 2f);
				fissureVertex(vc, pose, out, 2f + skin, -SLAB_DEPTH, 0f, 1f, 0.5f, u - SLAB_DEPTH, 1.6f);
				fissureVertex(vc, pose, base, 2f + skin, -SLAB_DEPTH, 0f, 1f, 0.5f, u - SLAB_DEPTH, 1.9f);
				fissureVertex(vc, pose, base, 2f + skin, SLAB_DEPTH, 0f, 1f, 0.5f, u + SLAB_DEPTH, 1.9f);
				fissureVertex(vc, pose, out, 2f + skin, SLAB_DEPTH, 0f, 1f, 0.5f, u + SLAB_DEPTH, 1.6f);
			}
			if (parted) {
				for (int row = 0; row < BREAK_ROWS; row++) {
					float y0 = heights[row];
					float y1 = heights[row + 1];
					for (int column = 0; column < BREAK_COLUMNS - 1; column++) {
						float z0 = breakDepth(column);
						float z1 = breakDepth(column + 1);
						// The facet catches the light by how hard the break leans at this cell.
						float slope = Math.abs(grid[row + 1][column] - grid[row][column]) / Math.max(0.01f, y1 - y0);
						float shade = 0.45f + 0.55f * Mth.clamp(slope, 0f, 1f);
						// Offset per side so the raw faces of the halves are not the same field.
						float u = side + 3f;
						fissureVertex(vc, pose, grid[row][column] - origin, y0, z0, 1f, shade, 1f, u + z0, y0);
						fissureVertex(vc, pose, grid[row][column + 1] - origin, y0, z1, 1f, shade, 1f, u + z1, y0);
						fissureVertex(vc, pose, grid[row + 1][column + 1] - origin, y1, z1, 1f, shade, 1f, u + z1, y1);
						fissureVertex(vc, pose, grid[row + 1][column] - origin, y1, z0, 1f, shade, 1f, u + z0, y1);
					}
				}
			}
			ms.popPose();
		}
		float[] mound = RUBBLE_MESHES.get(te.getBlockPos().asLong());
		if (overgrown && mound != null) {
			// Over the dead stone too: each facet of the mound as a quad with its last corner doubled.
			PoseStack.Pose pose = ms.last();
			for (int i = 0; i + 11 < mound.length; i += 12) {
				for (int corner = 0; corner < 4; corner++) {
					int at = i + Math.min(corner, 2) * 4;
					float x = mound[at];
					float y = mound[at + 1];
					float z = mound[at + 2];
					float facet = Mth.clamp(0.3f + mound[at + 3] * 7f, 0f, 1f);
					fissureVertex(vc, pose, x, y + 0.002f, z, 0f, facet, 0.5f, x * 0.35f, 0.7f + z * 0.35f + y * 0.35f);
				}
			}
		}
	}

	private static void fissureVertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, float near, float shade,
			float layer, float u, float v) {
		vc.addVertex(pose.pose(), x, y, z).setColor(near, shade, layer, 1f).setUv(u, v).setLight(0xF000F0).setNormal(pose, 0f, 0f, 1f);
	}

	/** The dead stone heaped around the fulcrum, a black mound thrown up by the blast. */
	private static void renderRubble(PoseStack ms, MultiBufferSource bufferIn, net.minecraft.core.BlockPos pos, float fall) {
		if (RUBBLE_MESHES.size() > 16) RUBBLE_MESHES.clear();
		float[] mesh = RUBBLE_MESHES.computeIfAbsent(pos.asLong(), SanguineMonolithRenderer::bakeRubble);
		VertexConsumer vc = bufferIn.getBuffer(RenderTypeInit.MONOLITH_RUBBLE);
		Matrix4f mat = ms.last().pose();
		float grow = Math.min(1f, fall * 2.2f);
		grow = 1f - (1f - grow) * (1f - grow);
		for (int i = 0; i < mesh.length; i += 4) {
			float grey = mesh[i + 3];
			vc.addVertex(mat, mesh[i], mesh[i + 1] * grow, mesh[i + 2]).setColor(grey, grey, grey * 1.08f, 1f);
		}
	}

	private static float moundHeight(float x, float z, float[] waves) {
		float angle = (float) Math.atan2(z, x);
		float edge = 1f + 0.18f * Mth.sin(angle * 3f + waves[0]) + 0.1f * Mth.sin(angle * 7f + waves[1]);
		float nx = x / (1.5f * edge);
		float nz = z / (1.2f * edge);
		float u = nx * nx + nz * nz;
		if (u >= 1f) return 0f;
		float dome = (float) Math.pow(1f - u, 1.25f);
		float lumps = 1f + 0.22f * Mth.sin(x * 5.3f + waves[2]) * Mth.sin(z * 6.1f + waves[3]) + 0.12f * Mth.sin(x * 11f + z * 9f + waves[4]);
		return 0.62f * dome * lumps;
	}

	private static float[] bakeRubble(long seed) {
		java.util.Random random = new java.util.Random(seed ^ 0x0B51D1A4L);
		float[] waves = new float[5];
		for (int i = 0; i < waves.length; i++) waves[i] = random.nextFloat() * Mth.TWO_PI;
		java.util.ArrayList<float[]> out = new java.util.ArrayList<>(8000);

		// The body of the mound: a lumpy dome, darkest where it meets the ground.
		int rings = 9;
		int segments = 34;
		float[][][] grid = new float[rings + 1][segments][];
		for (int ring = 0; ring <= rings; ring++) {
			for (int segment = 0; segment < segments; segment++) {
				float angle = segment * Mth.TWO_PI / segments;
				float reach = ring / (float) rings * 1.9f;
				float jitter = ring == 0 ? 0f : (random.nextFloat() - 0.5f) * 0.07f;
				float x = Mth.cos(angle) * reach + jitter;
				float z = Mth.sin(angle) * reach * 0.85f + jitter;
				float y = moundHeight(x, z, waves);
				if (y > 0f) y += (random.nextFloat() - 0.5f) * 0.05f;
				grid[ring][segment] = new float[] { x, y > 0f ? y : -0.01f, z };
			}
		}
		float[] middle = { 0f, 0f, 0f };
		for (int ring = 0; ring < rings; ring++) {
			for (int segment = 0; segment < segments; segment++) {
				int next = (segment + 1) % segments;
				float[] a = grid[ring][segment];
				float[] b = grid[ring][next];
				float[] c = grid[ring + 1][next];
				float[] d = grid[ring + 1][segment];
				if (a[1] <= 0f && b[1] <= 0f && c[1] <= 0f && d[1] <= 0f) continue;
				rubbleFacet(out, a, b, c, new float[] { a[0], -1f, a[2] }, 0.10f, 0f);
				rubbleFacet(out, a, c, d, new float[] { a[0], -1f, a[2] }, 0.10f, 0f);
			}
		}

		// Boulders bedded in it, bigger toward the fulcrum.
		for (int rock = 0; rock < 110; rock++) {
			float angle = random.nextFloat() * Mth.TWO_PI;
			float reach = (float) Math.sqrt(random.nextFloat()) * 1.5f;
			float x = Mth.cos(angle) * reach;
			float z = Mth.sin(angle) * reach * 0.85f;
			float size = (0.07f + random.nextFloat() * random.nextFloat() * 0.2f) * (1.25f - 0.5f * reach / 1.5f);
			float y = moundHeight(x, z, waves) * (0.8f + random.nextFloat() * 0.25f);
			rubbleRock(out, random, x, Math.max(y, size * 0.35f), z, size, ICOSA_FACES);
		}
		// Pebbles thrown clear of it across the ground.
		for (int pebble = 0; pebble < 44; pebble++) {
			float angle = random.nextFloat() * Mth.TWO_PI;
			float reach = Math.min(MOUND_REACH, 1.15f + random.nextFloat() * random.nextFloat() * 1.3f);
			float size = 0.025f + random.nextFloat() * 0.05f;
			rubbleRock(out, random, Mth.cos(angle) * reach, size * 0.4f, Mth.sin(angle) * reach * 0.85f, size, OCTA_FACES);
		}
		float[] mesh = new float[out.size() * 4];
		for (int i = 0; i < out.size(); i++) System.arraycopy(out.get(i), 0, mesh, i * 4, 4);
		// The whole heap scaled about the fulcrum to a block across (owner's call).
		float scale = RUBBLE_MAX_RADIUS / MOUND_REACH;
		for (int i = 0; i < mesh.length; i += 4) {
			mesh[i] *= scale;
			mesh[i + 1] *= scale;
			mesh[i + 2] *= scale;
		}
		return mesh;
	}

	private static void rubbleRock(java.util.List<float[]> out, java.util.Random random, float x, float y, float z, float size, int[][] faces) {
		float[][] shape = faces == ICOSA_FACES ? ICOSA_POINTS : OCTA_POINTS;
		float[][] points = new float[shape.length][];
		// Squashed, stretched and turned at random, each corner pulled in or out: no two alike.
		float sx = size * (0.7f + random.nextFloat() * 0.7f);
		float sy = size * (0.55f + random.nextFloat() * 0.6f);
		float sz = size * (0.7f + random.nextFloat() * 0.7f);
		float yaw = random.nextFloat() * Mth.TWO_PI;
		float roll = (random.nextFloat() - 0.5f) * 1.6f;
		float cy = Mth.cos(yaw), sinY = Mth.sin(yaw), cr = Mth.cos(roll), sr = Mth.sin(roll);
		for (int i = 0; i < shape.length; i++) {
			float pull = 0.72f + random.nextFloat() * 0.5f;
			float px = shape[i][0] * sx * pull;
			float py = shape[i][1] * sy * pull;
			float pz = shape[i][2] * sz * pull;
			float rx = px * cr - py * sr;
			float ry = px * sr + py * cr;
			points[i] = new float[] { x + rx * cy + pz * sinY, y + ry, z - rx * sinY + pz * cy };
		}
		float[] centre = { x, y, z };
		for (int[] face : faces) {
			float glint = random.nextFloat() < 0.12f ? 0.05f : 0f;
			rubbleFacet(out, points[face[0]], points[face[1]], points[face[2]], centre, 0.21f, glint);
		}
	}

	/** One flat-shaded facet of dead stone: black, catching only a little grey on the faces turned to the light. */
	private static void rubbleFacet(java.util.List<float[]> out, float[] a, float[] b, float[] c, float[] inside, float gloss, float glint) {
		float ux = b[0] - a[0], uy = b[1] - a[1], uz = b[2] - a[2];
		float vx = c[0] - a[0], vy = c[1] - a[1], vz = c[2] - a[2];
		float nx = uy * vz - uz * vy, ny = uz * vx - ux * vz, nz = ux * vy - uy * vx;
		float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
		if (length < 1e-7f) return;
		nx /= length; ny /= length; nz /= length;
		float outX = (a[0] + b[0] + c[0]) / 3f - inside[0];
		float outY = (a[1] + b[1] + c[1]) / 3f - inside[1];
		float outZ = (a[2] + b[2] + c[2]) / 3f - inside[2];
		if (nx * outX + ny * outY + nz * outZ < 0f) { nx = -nx; ny = -ny; nz = -nz; }
		float lit = Math.max(0f, nx * 0.36f + ny * 0.78f + nz * 0.51f);
		float grey = 0.004f + gloss * lit * lit * lit + glint * lit;
		// Pooled dark low down, where the heap shadows itself.
		for (float[] p : new float[][] { a, b, c }) {
			float shadow = 0.55f + 0.45f * Mth.clamp(p[1] / 0.45f, 0f, 1f);
			out.add(new float[] { p[0], p[1], p[2], grey * shadow });
		}
	}

	private static final float GOLDEN = 1.618034f;
	private static final float[][] ICOSA_POINTS = icosaPoints();
	private static final int[][] ICOSA_FACES = {
			{ 0, 11, 5 }, { 0, 5, 1 }, { 0, 1, 7 }, { 0, 7, 10 }, { 0, 10, 11 }, { 1, 5, 9 }, { 5, 11, 4 }, { 11, 10, 2 }, { 10, 7, 6 }, { 7, 1, 8 },
			{ 3, 9, 4 }, { 3, 4, 2 }, { 3, 2, 6 }, { 3, 6, 8 }, { 3, 8, 9 }, { 4, 9, 5 }, { 2, 4, 11 }, { 6, 2, 10 }, { 8, 6, 7 }, { 9, 8, 1 } };
	private static final float[][] OCTA_POINTS = { { 1, 0, 0 }, { -1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 }, { 0, 0, 1 }, { 0, 0, -1 } };
	private static final int[][] OCTA_FACES = {
			{ 0, 2, 4 }, { 4, 2, 1 }, { 1, 2, 5 }, { 5, 2, 0 }, { 0, 4, 3 }, { 4, 1, 3 }, { 1, 5, 3 }, { 5, 0, 3 } };

	private static float[][] icosaPoints() {
		float t = GOLDEN;
		float n = Mth.sqrt(1f + t * t);
		float a = 1f / n, b = t / n;
		return new float[][] { { -a, b, 0 }, { a, b, 0 }, { -a, -b, 0 }, { a, -b, 0 }, { 0, -a, b }, { 0, a, b }, { 0, -a, -b }, { 0, a, -b },
				{ b, 0, -a }, { b, 0, a }, { -b, 0, -a }, { -b, 0, a } };
	}

	/** The third eye of void hanging between the halves, in the Lidless panel material. */
	private static void renderPortal(PoseStack ms, MultiBufferSource bufferIn, SanguineMonolithBlockEntity te, float time,
			float partialTicks, Direction facing) {
		var local = com.vincenthuto.hemomancy.client.data.LidlessPassageClientState.local();
		if (local != null && local.door().equals(te.getBlockPos())) return;
		float seed = com.vincenthuto.hemomancy.client.render.tile.shared.LidlessDoorRenderer.seed(te.getBlockPos());
		float opening = te.opening(partialTicks);
		int live = 0;
		// A gateway's open eye shows the Chamber: ask for the view, and show the one drawn for this husk.
		if (te.proxy() != null && !te.seedInside() && opening > 0.02F) {
			com.vincenthuto.hemomancy.client.render.world.chamberofwill.ChamberProxyView.request(te, facing);
			live = com.vincenthuto.hemomancy.client.render.world.chamberofwill.ChamberProxyView.textureFor(te.getBlockPos());
		}
		com.vincenthuto.hemomancy.client.render.tile.shared.LidlessPortalPanel.draw(ms, bufferIn, time, seed, facing,
				opening, true, te.theme(), te.seedInside() ? 0.0F : 1.0F, live);
	}

	/** The rite fog, low and still, pouring out around the broken base; thinner once the Seed is gone. */
	private static void renderFog(PoseStack ms, MultiBufferSource bufferIn, SanguineMonolithBlockEntity te, float time, float partialTicks) {
		if (!(bufferIn instanceof MultiBufferSource.BufferSource buffer)) return;
		net.minecraft.world.phys.Vec3 camera = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		net.minecraft.world.phys.Vec3 cameraLocal = camera.subtract(net.minecraft.world.phys.Vec3.atLowerCornerOf(te.getBlockPos()));
		float opacity = 1.3f + 0.4f * te.glow(partialTicks);
		float seed = Math.floorMod(te.getBlockPos().asLong(), 977L) / 977.0f;
		com.vincenthuto.hemomancy.client.render.world.CardinalRiteFogRenderer.drawStill(ms, buffer,
				new net.minecraft.world.phys.Vec3(0.5D, 0.15D, 0.5D), FOG_RADIUS, time, opacity, cameraLocal, seed);
	}

	/** Purple lightning from the fulcrum every few seconds while the Seed waits, rarely after. */
	private static void strikeLightning(SanguineMonolithBlockEntity te, float partialTicks) {
		if (te.getLevel() == null) return;
		long now = te.getLevel().getGameTime();
		if (te.clientNextStrike >= 0 && now < te.clientNextStrike) return;
		java.util.Random random = new java.util.Random(now ^ te.getBlockPos().asLong());
		te.clientNextStrike = now + (te.seedInside() ? 60 + random.nextInt(60) : 200 + random.nextInt(100));
		net.minecraft.world.phys.Vec3 fulcrum = net.minecraft.world.phys.Vec3.atCenterOf(te.getBlockPos()).add(0.0D, -0.35D, 0.0D);
		net.minecraft.world.phys.Vec3 end = fulcrum.add((random.nextDouble() - 0.5D) * 3.0D, 1.2D + random.nextDouble() * 1.8D,
				(random.nextDouble() - 0.5D) * 3.0D);
		com.vincenthuto.hemomancy.client.render.world.CardinalRiteFogLightning.strike(fulcrum, end, now, partialTicks);
	}
}

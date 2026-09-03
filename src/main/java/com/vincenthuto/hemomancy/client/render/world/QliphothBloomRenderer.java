package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.QliphothBloomClientData;
import com.vincenthuto.hemomancy.client.render.CachedMeshModelView;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders the Qliphoth Bloom — a large, blooming black and red tree-like
 * structure made of rendered geometry (not particles), surrounded by pulsing
 * red rings similar to the cardinal rite boundary renderer.
 * <p>
 * The tree grows from the ground at the rite center. Its trunk is dark/black,
 * branches spread outward and upward as angular, bark-skinned tubes. Pulsing
 * concentric rings radiate outward from the base.
 */
public class QliphothBloomRenderer {
	private static final ResourceLocation BARK = Hemomancy.rloc("textures/block/dark_oak_log.png");
	private static final ResourceLocation QLIPHOTH_SKY = Hemomancy.rloc("textures/environment/qliphoth_communion_sky.png");
	private static final RenderType BARK_TYPE = RenderType.entityCutoutNoCull(BARK);
	// ponytail: faceted transient effects; restore higher tessellation only if close visual checks show silhouette loss.
	private static final int EFFECT_LATITUDES = 5;
	private static final int EFFECT_LONGITUDES = 8;
	// ponytail: half-resolution scene capture; restore full resolution only if lensing visibly pixelates.
	private static final int SCENE_COPY_SCALE = 2;
	private static TextureTarget frameCopyTarget;

	public static void clearCaches() {
		QliphothBarkCache.clear();
		if (frameCopyTarget != null) {
			frameCopyTarget.destroyBuffers();
			frameCopyTarget = null;
		}
	}

	// ── Tree geometry parameters ──
	/** Total height of the tree in blocks. */
	private static final float TREE_HEIGHT = 8.0f;
	/** Trunk base radius. */
	private static final float TRUNK_BASE_RADIUS = 0.45f;
	/** Number of vertical segments for the trunk. */
	private static final int TRUNK_SEGMENTS_V = 12;
	/** Number of circumference segments for the trunk. */
	private static final int TRUNK_SEGMENTS_H = 10;
	/** Number of major branches. */
	private static final int BRANCH_COUNT = 7;
	/** Number of segments per branch. */
	private static final int BRANCH_SEGS = 6;
	/** Branch maximum length. */
	private static final float BRANCH_LENGTH = 8.5f;
	/** Number of sub-branches per main branch. */
	private static final int BRANCH_SUB_COUNT = 2;
	/** Number of segments per sub-branch. */
	private static final int BRANCH_SUB_SEGS = 4;
	/** Canopy bloom sphere count. */
	private static final int CANOPY_BLOOMS = 14;
	/** Canopy bloom radius. */
	private static final float BLOOM_RADIUS = 1.2f;
	/** Root count spreading on the ground. */
	private static final int ROOT_COUNT = 8;
	/** Number of segments per root. */
	private static final int ROOT_SEGS = 8;
	/** Number of sub-branches per root. */
	private static final int ROOT_SUB_BRANCHES = 2;
	/** Number of segments per sub-root branch. */
	private static final int ROOT_SUB_SEGS = 5;
	/** Number of small dark orbs embedded in the trunk. */
	private static final int TRUNK_ORB_COUNT = 9;
	/** Radius of each small trunk orb. */
	private static final float TRUNK_ORB_RADIUS = 0.12f;
	/** Lat/lon resolution for small trunk orbs. */
	private static final int TRUNK_ORB_LAT = 6;
	private static final int TRUNK_ORB_LON = 8;

	// ── Pulsing ring parameters ──
	/** Number of concentric rings. */
	private static final int RING_COUNT = 5;
	/** Ring segment count. */
	private static final int RING_SEGMENTS = 72;
	/** Ring core width. */
	private static final float RING_CORE_WIDTH = 0.08f;
	/** Ring glow width. */
	private static final float RING_GLOW_WIDTH = 0.20f;
	/** Maximum ring radius in blocks. */
	private static final float RING_MAX_RADIUS = 6.0f;
	/** How fast rings pulse outward. */
	private static final double RING_PULSE_SPEED = 0.005;

	public static void render(PoseStack poseStack, float partialTick, Frustum frustum) {
		List<QliphothBloomClientData.BloomEntry> blooms = QliphothBloomClientData.getActiveBlooms();
		if (blooms.isEmpty()) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		float currentTime = mc.level.getGameTime() + partialTick;
		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

		boolean sceneCopied = false;
		for (QliphothBloomClientData.BloomEntry bloom : blooms) {
			BlockPos center = bloom.getCenter();
			double cx = center.getX() + 0.5;
			double cy = center.getY() + 4.0;
			double cz = center.getZ() + 0.5;
			double distanceSquared = cam.distanceToSqr(cx, cy, cz);
			boolean visible = frustum != null && frustum.isVisible(new AABB(
					cx - 10.0, center.getY() - 1.0, cz - 10.0,
					cx + 10.0, center.getY() + 12.0, cz + 10.0));
			if (!QliphothBloomRenderRules.shouldRender(distanceSquared, visible)) continue;
			boolean dynamicEffects = QliphothBloomRenderRules.shouldRenderDynamicEffects(distanceSquared);
			if (drawQliphothTree(poseStack, buffer, bloom, currentTime, cam, dynamicEffects, !sceneCopied)) {
				sceneCopied = true;
			}
			if (dynamicEffects && !bloom.isPortalOpen() && !bloom.isSealedTrophy()) {
				drawPulsingRings(poseStack, buffer, bloom, currentTime, cam);
			}
		}
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_CORE);
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_GLOW);
		buffer.endBatch(BARK_TYPE);
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Growth-stage helpers
	// ════════════════════════════════════════════════════════════════════════

	/**
	 * Returns the fraction of {@link #TREE_HEIGHT} the trunk should be drawn to
	 * for the given growth stage (0 = freshly planted, 9 = fully grown).
	 * Stages 0–5 produce a linearly-scaled trunk (25% → 100%).
	 * Stages 6–9 are always at full height.
	 */
	private static float trunkHeightFrac(int stage) {
		if (stage >= 6) return 1.0f;
		return 0.25f + (stage / 5.0f) * 0.75f;
	}

	/**
	 * Returns the root-length multiplier for the given growth stage.
	 * Stages 0–4 scale linearly from 15% → 100%; stage 5+ is full.
	 */
	private static float rootLengthFrac(int stage) {
		if (stage >= 5) return 1.0f;
		return 0.15f + (stage / 4.0f) * 0.85f;
	}

	/**
	 * Returns the branch-length multiplier for the given growth stage.
	 * Branches are absent below stage 6; stage 6 → 40%; stage 7 → 70%; 8+ → 100%.
	 */
	private static float branchLengthFrac(int stage) {
		if (stage < 6) return 0.0f;
		if (stage >= 8) return 1.0f;
		return 0.4f + (stage - 6) * 0.3f; // 0.40 at stage 6, 0.70 at stage 7
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Public API for BlockEntityRenderer usage
	// ════════════════════════════════════════════════════════════════════════

	/**
	 * Renders the Qliphoth tree geometry (trunk, branches, canopy, roots, orbs)
	 * at the given offset from the current PoseStack origin, respecting the
	 * current growth {@code stage} (0 = freshly planted stub, 9 = fully grown
	 * with apex black-hole orb).
	 * <p>
	 * For use by the block entity renderer where the PoseStack is already
	 * translated to the block entity position.
	 *
	 * @param stack   PoseStack already positioned at the block entity origin
	 * @param buffer  multi buffer source
	 * @param time    current game time + partial tick
	 * @param offsetX X offset from PoseStack origin to center the tree
	 * @param offsetY Y offset from PoseStack origin (e.g. 0.1 for slight raise)
	 * @param offsetZ Z offset from PoseStack origin to center the tree
	 * @param stage   number of Qliphoth Pomes already dropped (0–9)
	 */
	public static void renderTree(PoseStack stack, MultiBufferSource buffer, float time,
			double offsetX, double offsetY, double offsetZ, int stage) {
		stack.pushPose();
		stack.translate(offsetX, offsetY, offsetZ);
		Matrix4f mat = stack.last().pose();

		double pulse = (Math.sin(time * 0.06) + 1.0) * 0.5;

		drawFacetedBloom(stack, buffer, time, stage, true, true);

		stack.popPose();
	}

	/**
	 * Renders the pulsing concentric rings at the given offset from the
	 * current PoseStack origin.
	 *
	 * @param stack   PoseStack already positioned at the block entity origin
	 * @param buffer  multi buffer source
	 * @param time    current game time + partial tick
	 * @param offsetX X offset
	 * @param offsetY Y offset
	 * @param offsetZ Z offset
	 */
	public static void renderRings(PoseStack stack, MultiBufferSource buffer, float time,
			double offsetX, double offsetY, double offsetZ) {
		stack.pushPose();
		stack.translate(offsetX, offsetY, offsetZ);
		drawRingsInternal(stack, buffer, time);
		stack.popPose();
	}

	/**
	 * Internal ring drawing logic shared by both the world renderer and BER.
	 * Expects the PoseStack to already be translated to the ring center.
	 */
	private static void drawRingsInternal(PoseStack stack, MultiBufferSource buffer, float time) {
		Matrix4f mat = stack.last().pose();

		double pulse = (Math.sin(time * 0.08) + 1.0) * 0.5;

		for (int ring = 0; ring < RING_COUNT; ring++) {
			double phase = (time * RING_PULSE_SPEED + ring * (1.0 / RING_COUNT)) % 1.0;
			float ringRadius = (float) (1.5 + phase * RING_MAX_RADIUS);

			float fadeProgress = (float) phase;
			float fadeAlpha = 1.0f - fadeProgress * fadeProgress;

			if (fadeAlpha < 0.02f) continue;

			float coreAlpha = (float) (0.55 + 0.25 * pulse) * fadeAlpha;
			float glowAlpha = (float) (0.12 + 0.10 * pulse) * fadeAlpha;

			float coreR = (float) Math.min(1.0, 0.85 + 0.15 * pulse);
			float coreG = 0.04f;
			float coreB = 0.03f;
			float glowR = (float) Math.min(1.0, 0.55 + 0.2 * pulse);
			float glowG = 0.02f;
			float glowB = 0.02f;

			for (int i = 0; i < RING_SEGMENTS; i++) {
				double a1 = Math.toRadians((360.0 / RING_SEGMENTS) * i);
				double a2 = Math.toRadians((360.0 / RING_SEGMENTS) * (i + 1));

				float undulate1 = (float) (Math.sin(a1 * 6.0 + time * 0.05 + ring * 2.0) * 0.08 * (1.0 - fadeProgress * 0.5));
				float undulate2 = (float) (Math.sin(a2 * 6.0 + time * 0.05 + ring * 2.0) * 0.08 * (1.0 - fadeProgress * 0.5));

				float r1 = ringRadius + undulate1;
				float r2 = ringRadius + undulate2;

				float y1 = (float) (Math.sin(a1 * 4.0 + time * 0.06) * 0.01);
				float y2 = (float) (Math.sin(a2 * 4.0 + time * 0.06) * 0.01);

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
				emitGlow(buffer, mat,
						cos1 * iGlow1, y1, sin1 * iGlow1, glowR, glowG, glowB, 0f,
						cos1 * iCore1, y1, sin1 * iCore1, glowR, glowG, glowB, glowAlpha,
						cos2 * iCore2, y2, sin2 * iCore2, glowR, glowG, glowB, glowAlpha,
						cos2 * iGlow2, y2, sin2 * iGlow2, glowR, glowG, glowB, 0f);

				// Core
				emitCore(buffer, mat,
						cos1 * iCore1, y1, sin1 * iCore1, coreR, coreG, coreB, coreAlpha,
						cos1 * oCore1, y1, sin1 * oCore1, coreR, coreG, coreB, coreAlpha,
						cos2 * oCore2, y2, sin2 * oCore2, coreR, coreG, coreB, coreAlpha,
						cos2 * iCore2, y2, sin2 * iCore2, coreR, coreG, coreB, coreAlpha);

				// Outer glow
				emitGlow(buffer, mat,
						cos1 * oCore1, y1, sin1 * oCore1, glowR, glowG, glowB, glowAlpha,
						cos1 * oGlow1, y1, sin1 * oGlow1, glowR, glowG, glowB, 0f,
						cos2 * oGlow2, y2, sin2 * oGlow2, glowR, glowG, glowB, 0f,
						cos2 * oCore2, y2, sin2 * oCore2, glowR, glowG, glowB, glowAlpha);
			}
		}
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Tree Rendering — trunk, branches, canopy, roots
	// ════════════════════════════════════════════════════════════════════════

	private static boolean drawQliphothTree(PoseStack stack, MultiBufferSource buffer,
			QliphothBloomClientData.BloomEntry bloom, float time, Vec3 cam, boolean dynamicEffects,
			boolean copyScene) {

		BlockPos center = bloom.getCenter();
		double cx = center.getX() + 0.5;
		double cy = center.getY() + 0.1;
		double cz = center.getZ() + 0.5;

		stack.pushPose();
		stack.translate(cx - cam.x, cy - cam.y, cz - cam.z);
		Matrix4f mat = stack.last().pose();

		// Global breathing pulse
		double pulse = (Math.sin(time * 0.06) + 1.0) * 0.5;

		int stage      = bloom.getPomesDropped();
		if (bloom.isPortalOpen() || bloom.isSealedTrophy()) {
			QliphothBarkCache.renderRoots(stack, 9);
			drawRootVeins(buffer, mat, time, QliphothBloomGeometry.snapshot(9));
			float stumpSeed = Math.floorMod(center.asLong(), 10007L) / 10007.0F;
			drawSeveredStump(buffer, mat, time, pulse, stumpSeed, bloom.isPortalOpen());
			stack.popPose();
			return false;
		}
		drawFacetedBloom(stack, buffer, time, stage, dynamicEffects, copyScene);

		stack.popPose();
		return dynamicEffects && QliphothBloomGeometry.hasApex(stage) && copyScene;
	}

	private static void drawSeveredStump(MultiBufferSource buffer, Matrix4f mat, float time,
			double pulse, float seed, boolean open) {
		float baseRadius = 0.69F;
		float waistRadius = 0.55F;
		float baseY = 0.10F;
		float waistY = 0.92F;
		float trunkR = (float) (0.075F + 0.025F * pulse);
		float veinR = (float) (0.26F + 0.16F * pulse);

		for (int facet = 0; facet < QliphothSeveredGeometry.FACETS; facet++) {
			int next = (facet + 1) % QliphothSeveredGeometry.FACETS;
			float a0 = (float) (facet * Math.PI * 2.0 / QliphothSeveredGeometry.FACETS);
			float a1 = (float) (next * Math.PI * 2.0 / QliphothSeveredGeometry.FACETS);
			float baseX0 = (float) Math.cos(a0) * baseRadius;
			float baseZ0 = (float) Math.sin(a0) * baseRadius;
			float baseX1 = (float) Math.cos(a1) * baseRadius;
			float baseZ1 = (float) Math.sin(a1) * baseRadius;
			float waistX0 = (float) Math.cos(a0) * waistRadius;
			float waistZ0 = (float) Math.sin(a0) * waistRadius;
			float waistX1 = (float) Math.cos(a1) * waistRadius;
			float waistZ1 = (float) Math.sin(a1) * waistRadius;
			QliphothSeveredGeometry.RimPoint rim0 = QliphothSeveredGeometry.rimPoint(facet);
			QliphothSeveredGeometry.RimPoint rim1 = QliphothSeveredGeometry.rimPoint(next);

			emitCore(buffer, mat,
					baseX0, baseY, baseZ0, trunkR, 0.018F, 0.018F, 0.96F,
					baseX1, baseY, baseZ1, trunkR, 0.018F, 0.018F, 0.96F,
					waistX1, waistY, waistZ1, trunkR, 0.018F, 0.018F, 0.96F,
					waistX0, waistY, waistZ0, trunkR, 0.018F, 0.018F, 0.96F);
			emitCore(buffer, mat,
					waistX0, waistY, waistZ0, trunkR, 0.018F, 0.018F, 0.96F,
					waistX1, waistY, waistZ1, trunkR, 0.018F, 0.018F, 0.96F,
					rim1.x(), rim1.y(), rim1.z(), trunkR, 0.018F, 0.018F, 0.96F,
					rim0.x(), rim0.y(), rim0.z(), trunkR, 0.018F, 0.018F, 0.96F);

			float glowAlpha = open ? 0.12F : 0.055F;
			emitGlow(buffer, mat,
					waistX0 * 1.015F, waistY, waistZ0 * 1.015F, veinR, 0.01F, 0.01F, glowAlpha,
					waistX1 * 1.015F, waistY, waistZ1 * 1.015F, veinR, 0.01F, 0.01F, glowAlpha,
					rim1.x() * 1.015F, rim1.y(), rim1.z() * 1.015F, veinR, 0.01F, 0.01F, glowAlpha,
					rim0.x() * 1.015F, rim0.y(), rim0.z() * 1.015F, veinR, 0.01F, 0.01F, glowAlpha);
		}

		float shaderTime = time / 20.0F;
		float burden = open ? 0.34F : 0.12F;
		float attuned = open ? 0.96F : 0.84F;
		RenderType capType = HemoRenderTypes.monolithFragmentRigid(shaderTime, seed, burden, attuned, 0.0F);
		VertexConsumer cap = buffer.getBuffer(capType);
		QliphothSeveredGeometry.RimPoint center = new QliphothSeveredGeometry.RimPoint(
				0.0F, QliphothSeveredGeometry.cutHeight(0.0F, 0.0F), 0.0F, 0.5F, 0.5F);
		for (int facet = 0; facet < QliphothSeveredGeometry.FACETS; facet++) {
			QliphothSeveredGeometry.RimPoint current = QliphothSeveredGeometry.rimPoint(facet);
			QliphothSeveredGeometry.RimPoint next = QliphothSeveredGeometry.rimPoint(
					(facet + 1) % QliphothSeveredGeometry.FACETS);
			emitMonolithTriangle(cap, mat, center, next, current);
		}
		if (buffer instanceof MultiBufferSource.BufferSource source) {
			source.endBatch(capType);
		}
	}

	private static void drawFacetedBloom(PoseStack stack, MultiBufferSource buffer, float time, int stage,
			boolean dynamicEffects, boolean copyScene) {
		Matrix4f mat = stack.last().pose();
		QliphothBloomGeometry.StageSnapshot snapshot = QliphothBloomGeometry.snapshot(stage);
		QliphothBarkCache.renderTree(stack, snapshot.stage());
		if (!dynamicEffects) return;
		drawVeins(buffer, mat, snapshot.trunk(), time, 3);
		drawRootVeins(buffer, mat, time, snapshot);

		for (QliphothBloomGeometry.Limb branch : snapshot.mainBranches()) {
			drawVeins(buffer, mat, branch, time, 2);
		}
		for (QliphothBloomGeometry.Limb branch : snapshot.secondaryBranches()) {
			drawVeins(buffer, mat, branch, time, 1);
		}

		for (QliphothBloomGeometry.Pome pome : QliphothBloomGeometry.pomes(stage, time)) {
			drawPome(buffer, mat, pome, time);
		}
		for (QliphothBloomGeometry.Crystal crystal : QliphothBloomGeometry.crystals(stage, time)) {
			drawFruitStem(buffer, mat, crystal, time);
			drawCrystal(buffer, mat, crystal, time);
		}
		if (QliphothBloomGeometry.hasApex(stage)) drawFacetedApex(stack, buffer, mat, time, copyScene);
	}

	private static void drawRootVeins(MultiBufferSource buffer, Matrix4f mat, float time,
			QliphothBloomGeometry.StageSnapshot snapshot) {
		for (QliphothBloomGeometry.Limb root : snapshot.roots()) {
			drawVeins(buffer, mat, root, time, 2);
		}
	}

	private static void drawFacetedApex(PoseStack stack, MultiBufferSource buffer, Matrix4f mat, float time,
			boolean copyScene) {
		for (QliphothBloomGeometry.Limb prong : QliphothBloomGeometry.snapshot(9).crownProngs()) {
			drawVeins(buffer, mat, prong, time, 1);
		}
		for (QliphothBloomGeometry.Limb loop : QliphothBloomGeometry.apexLoops(time)) {
			drawLimb(buffer, mat, loop, 6, .72f, .008f, .018f, .88f);
			QliphothBloomGeometry.Limb glow = new QliphothBloomGeometry.Limb(loop.points(),
					loop.radii().stream().map(radius -> radius * 1.7).toList(), loop.seed());
			drawLimbGlow(buffer, mat, glow, 6, .95f, .012f, .022f, .12f);
		}
		renderAccretionBlackHole(stack, buffer, QliphothBloomGeometry.apexCenter(time), time, copyScene);
	}

	private static void renderAccretionBlackHole(PoseStack stack, MultiBufferSource buffer,
			QliphothBloomGeometry.Point center, float time, boolean copyScene) {
		if (buffer instanceof MultiBufferSource.BufferSource source) {
			source.endBatch(BARK_TYPE);
			source.endBatch(RenderTypeInit.RITE_BOUNDARY_CORE);
			source.endBatch(RenderTypeInit.RITE_BOUNDARY_GLOW);
		}
		if (copyScene) copyMainRenderTarget(Minecraft.getInstance());
		int sceneTexture = frameCopyTarget == null ? -1 : frameCopyTarget.getColorTextureId();
		RenderType type = HemoRenderTypes.qliphothBlackHole(QLIPHOTH_SKY, sceneTexture, time * .050f,
				.731f, 1.72f, 1.72f, true, true);
		stack.pushPose();
		stack.translate(center.x(), center.y(), center.z());
		stack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
		Matrix4f matrix = stack.last().pose();
		VertexConsumer out = buffer.getBuffer(type);
		float halfSize = 1.72f;
		out.addVertex(matrix, -halfSize, -halfSize, 0).setUv(0, 1).setColor(255, 255, 255, 255);
		out.addVertex(matrix, halfSize, -halfSize, 0).setUv(1, 1).setColor(255, 255, 255, 255);
		out.addVertex(matrix, halfSize, halfSize, 0).setUv(1, 0).setColor(255, 255, 255, 255);
		out.addVertex(matrix, -halfSize, halfSize, 0).setUv(0, 0).setColor(255, 255, 255, 255);
		stack.popPose();
		if (buffer instanceof MultiBufferSource.BufferSource source) source.endBatch(type);
	}

	private static void copyMainRenderTarget(Minecraft minecraft) {
		RenderTarget mainTarget = minecraft.getMainRenderTarget();
		if (mainTarget.width <= 0 || mainTarget.height <= 0) return;
		ensureFrameCopyTarget(mainTarget);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainTarget.frameBufferId);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, frameCopyTarget.frameBufferId);
		GL30.glBlitFramebuffer(0, 0, mainTarget.width, mainTarget.height, 0, 0,
				frameCopyTarget.width, frameCopyTarget.height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
		mainTarget.bindWrite(false);
	}

	private static void ensureFrameCopyTarget(RenderTarget mainTarget) {
		int width = Math.max(1, mainTarget.width / SCENE_COPY_SCALE);
		int height = Math.max(1, mainTarget.height / SCENE_COPY_SCALE);
		if (frameCopyTarget != null && frameCopyTarget.width == width && frameCopyTarget.height == height) return;
		if (frameCopyTarget != null) frameCopyTarget.destroyBuffers();
		frameCopyTarget = new TextureTarget(width, height, false, Minecraft.ON_OSX);
		frameCopyTarget.setFilterMode(GL11.GL_LINEAR);
	}

	private static void drawVeins(MultiBufferSource buffer, Matrix4f mat, QliphothBloomGeometry.Limb wood,
			float time, int count) {
		for (int index = 0; index < count; index++) {
			List<QliphothBloomGeometry.Point> path = QliphothBloomGeometry.surfaceVein(wood, index);
			float traveling = (float) ((Math.sin(time * .11 - index * 1.7 + wood.seed() * .17) + 1) * .5);
			QliphothBloomGeometry.Limb vein = new QliphothBloomGeometry.Limb(path,
					path.stream().map(point -> .018 + traveling * .009).toList(), wood.seed() + index);
			drawLimbGlow(buffer, mat, vein, 4, .60f + traveling * .35f, .006f, .012f, .58f);
		}
	}

	private static void drawFruitStem(MultiBufferSource buffer, Matrix4f mat,
			QliphothBloomGeometry.Crystal fruit, float time) {
		QliphothBloomGeometry.Limb stem = QliphothBloomGeometry.fruitStem(fruit, time);
		drawSphere(buffer, mat, fruit.anchor(), .095, .19f, .018f, .020f, .94f,
				EFFECT_LATITUDES, EFFECT_LONGITUDES);
		drawLimb(buffer, mat, stem, 5, .20f, .012f, .018f, .97f);
		drawVeins(buffer, mat, stem, time, 1);
		drawSphere(buffer, mat, fruit.center().add(new QliphothBloomGeometry.Point(0, fruit.size() * .68, 0)),
				fruit.size() * .24, .26f, .015f, .024f, .95f, EFFECT_LATITUDES, EFFECT_LONGITUDES);
	}

	private static void drawCrystal(MultiBufferSource buffer, Matrix4f mat,
			QliphothBloomGeometry.Crystal crystal, float time) {
		QliphothBloomGeometry.Point c = crystal.center();
		double size = crystal.size() * (1 + Math.sin(time * .04 + crystal.seed()) * .035);
		double rotation = time * .008 + crystal.seed() * 1.7;
		for (int lobe = 0; lobe < crystal.lobes(); lobe++) {
			double angle = rotation + lobe * Math.PI * 2 / crystal.lobes();
			double spread = size * (.30 + .22 * Math.sin(crystal.seed() * 3.1 + lobe));
			QliphothBloomGeometry.Point center = c.add(new QliphothBloomGeometry.Point(
					Math.cos(angle) * spread + crystal.skew() * size * .16,
					(lobe % 2 == 0 ? .16 : -.12) * size,
					Math.sin(angle) * spread));
			float shade = .34f + lobe / (float)Math.max(1, crystal.lobes() - 1) * .28f;
			drawDistortedEllipsoid(buffer, mat, center, size * (.72 + lobe % 3 * .10),
					size * (1.05 + (lobe + crystal.seed()) % 3 * .14), size * .68,
					angle, crystal.seed() * 19 + lobe, shade, .008f, .030f, .92f);
			QliphothBloomGeometry.Point highlight = center.add(new QliphothBloomGeometry.Point(
					-Math.cos(angle) * size * .38, size * .38, -Math.sin(angle) * size * .38));
			drawDistortedEllipsoid(buffer, mat, highlight, size * .15, size * .42, size * .10,
					angle, crystal.seed() * 31 + lobe, .88f, .075f, .095f, .48f);
		}
		drawFruitSeams(buffer, mat, c, size, crystal.lobes(), rotation);
		drawSphere(buffer, mat, c, size * 1.35, .78f, .008f, .032f, .055f, 6, 8);
		drawFruitWisps(buffer, mat, c, size * 1.7, time, crystal.seed() + 200);
	}

	private static void drawFruitSeams(MultiBufferSource buffer, Matrix4f mat,
			QliphothBloomGeometry.Point center, double size, int lobes, double rotation) {
		for (int seam = 0; seam < lobes; seam++) {
			double angle = rotation + seam * Math.PI * 2 / lobes;
			QliphothBloomGeometry.Point side = new QliphothBloomGeometry.Point(Math.cos(angle), 0, Math.sin(angle));
			List<QliphothBloomGeometry.Point> path = List.of(
					center.add(side.scale(size * .52)).add(new QliphothBloomGeometry.Point(0, size * .72, 0)),
					center.add(side.scale(size * .78)),
					center.add(side.scale(size * .48)).add(new QliphothBloomGeometry.Point(0, -size * .70, 0)));
			drawLimbGlow(buffer, mat, new QliphothBloomGeometry.Limb(path, List.of(.010, .014, .006), 1400 + seam),
					4, .82f, .016f, .034f, .38f);
		}
	}

	private static void drawPome(MultiBufferSource buffer, Matrix4f mat,
			QliphothBloomGeometry.Pome pome, float time) {
		double pulse = 1 + Math.sin(time * .032 + pome.wispPhase()) * .035;
		drawDistortedEllipsoid(buffer, mat, pome.center(), pome.radius() * pome.aspectX() * pulse,
				pome.radius() * pome.aspectY() * pulse, pome.radius() * (.80 + .16 * Math.sin(pome.angle())) * pulse,
				pome.angle(), (int)(pome.wispPhase() * 100), .004f, .002f, .006f, .99f);
		drawFruitWisps(buffer, mat, pome.center(), pome.radius() * 1.55, time,
				(int)(pome.wispPhase() * 100));
	}

	private static void drawFruitWisps(MultiBufferSource buffer, Matrix4f mat, QliphothBloomGeometry.Point center,
			double radius, float time, int seed) {
		for (int i = 0; i < 3; i++) {
			double angle = time * (.018 + i * .004) + seed * .17 + i * 2.1;
			double orbit = radius * (.82 + i * .17);
			QliphothBloomGeometry.Point wisp = center.add(new QliphothBloomGeometry.Point(
					Math.cos(angle) * orbit, Math.sin(angle * 1.7 + i) * radius * .38,
					Math.sin(angle) * orbit));
			drawSphere(buffer, mat, wisp, radius * (.13 + i * .025), .58f, .006f, .030f,
					.055f - i * .010f, 3, 5);
		}
	}

	private static void drawLimb(MultiBufferSource buffer, Matrix4f mat, QliphothBloomGeometry.Limb limb,
			int sides, float red, float green, float blue, float alpha) {
		drawTube(buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), mat, limb, sides, red, green, blue, alpha);
	}

	private static void drawLimbGlow(MultiBufferSource buffer, Matrix4f mat, QliphothBloomGeometry.Limb limb,
			int sides, float red, float green, float blue, float alpha) {
		drawTube(buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), mat, limb, sides, red, green, blue, alpha);
	}

	private static void drawTexturedLimb(VertexConsumer out, Matrix4f mat,
			QliphothBloomGeometry.Limb limb, int sides, float red, float green, float blue, float repeatsPerBlock) {
		List<QliphothBloomGeometry.Point> points = limb.points();
		if (points.size() < 2) return;
		QliphothBloomGeometry.Point[][] frames = tubeFrames(points);
		QliphothBloomGeometry.Point[] normals = frames[0], bins = frames[1];
		List<Double> textureV = QliphothBloomGeometry.textureV(limb, repeatsPerBlock);
		for (int segment = 0; segment < points.size() - 1; segment++) {
			for (int side = 0; side < sides; side++) {
				double a0 = side * Math.PI * 2 / sides, a1 = (side + 1) * Math.PI * 2 / sides;
				QliphothBloomGeometry.Point p00 = ringPoint(points.get(segment), normals[segment], bins[segment], limb.radii().get(segment), a0);
				QliphothBloomGeometry.Point p01 = ringPoint(points.get(segment), normals[segment], bins[segment], limb.radii().get(segment), a1);
				QliphothBloomGeometry.Point p10 = ringPoint(points.get(segment + 1), normals[segment + 1], bins[segment + 1], limb.radii().get(segment + 1), a0);
				QliphothBloomGeometry.Point p11 = ringPoint(points.get(segment + 1), normals[segment + 1], bins[segment + 1], limb.radii().get(segment + 1), a1);
				QliphothBloomGeometry.Point n00 = normals[segment].scale(Math.cos(a0)).add(bins[segment].scale(Math.sin(a0)));
				QliphothBloomGeometry.Point n01 = normals[segment].scale(Math.cos(a1)).add(bins[segment].scale(Math.sin(a1)));
				QliphothBloomGeometry.Point n10 = normals[segment + 1].scale(Math.cos(a0)).add(bins[segment + 1].scale(Math.sin(a0)));
				QliphothBloomGeometry.Point n11 = normals[segment + 1].scale(Math.cos(a1)).add(bins[segment + 1].scale(Math.sin(a1)));
				float u0 = side / (float)sides, u1 = (side + 1) / (float)sides;
				texturedVertex(out, mat, p00, n00, u0, textureV.get(segment).floatValue(), red, green, blue);
				texturedVertex(out, mat, p10, n10, u0, textureV.get(segment + 1).floatValue(), red, green, blue);
				texturedVertex(out, mat, p11, n11, u1, textureV.get(segment + 1).floatValue(), red, green, blue);
				texturedVertex(out, mat, p01, n01, u1, textureV.get(segment).floatValue(), red, green, blue);
			}
		}
	}

	private static void texturedVertex(VertexConsumer out, Matrix4f mat, QliphothBloomGeometry.Point point,
			QliphothBloomGeometry.Point normal, float u, float v, float red, float green, float blue) {
		out.addVertex(mat, (float)point.x(), (float)point.y(), (float)point.z())
				.setColor(red, green, blue, 1f).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(LightTexture.FULL_BRIGHT).setNormal((float)normal.x(), (float)normal.y(), (float)normal.z());
	}

	private static QliphothBloomGeometry.Point[][] tubeFrames(List<QliphothBloomGeometry.Point> points) {
		QliphothBloomGeometry.Point[] normals = new QliphothBloomGeometry.Point[points.size()];
		QliphothBloomGeometry.Point[] bins = new QliphothBloomGeometry.Point[points.size()];
		for (int i = 0; i < points.size(); i++) {
			QliphothBloomGeometry.Point tangent = points.get(Math.min(i + 1, points.size() - 1))
					.subtract(points.get(Math.max(0, i - 1))).normalized();
			if (i == 0) {
				QliphothBloomGeometry.Point ref = Math.abs(tangent.y()) > .88
						? new QliphothBloomGeometry.Point(1, 0, 0) : new QliphothBloomGeometry.Point(0, 1, 0);
				normals[i] = tangent.cross(ref).normalized();
			} else {
				QliphothBloomGeometry.Point projected = normals[i - 1].subtract(tangent.scale(dot(normals[i - 1], tangent)));
				if (projected.length() < 1.0e-5) projected = bins[i - 1].cross(tangent);
				normals[i] = projected.normalized();
			}
			bins[i] = tangent.cross(normals[i]).normalized();
		}
		return new QliphothBloomGeometry.Point[][] { normals, bins };
	}

	private static void drawTube(VertexConsumer out, Matrix4f mat, QliphothBloomGeometry.Limb limb,
			int sides, float red, float green, float blue, float alpha) {
		List<QliphothBloomGeometry.Point> points = limb.points();
		if (points.size() < 2) return;
		QliphothBloomGeometry.Point[][] frames = tubeFrames(points);
		QliphothBloomGeometry.Point[] normals = frames[0], bins = frames[1];
		for (int segment = 0; segment < points.size() - 1; segment++) {
			for (int side = 0; side < sides; side++) {
				double a0 = side * Math.PI * 2 / sides, a1 = (side + 1) * Math.PI * 2 / sides;
				QliphothBloomGeometry.Point p00 = ringPoint(points.get(segment), normals[segment], bins[segment], limb.radii().get(segment), a0);
				QliphothBloomGeometry.Point p01 = ringPoint(points.get(segment), normals[segment], bins[segment], limb.radii().get(segment), a1);
				QliphothBloomGeometry.Point p10 = ringPoint(points.get(segment + 1), normals[segment + 1], bins[segment + 1], limb.radii().get(segment + 1), a0);
				QliphothBloomGeometry.Point p11 = ringPoint(points.get(segment + 1), normals[segment + 1], bins[segment + 1], limb.radii().get(segment + 1), a1);
				emitPointQuad(out, mat, p00, p10, p11, p01, red, green, blue, alpha);
			}
		}
		if (points.get(0).distanceTo(points.get(points.size() - 1)) > 1.0e-5) {
			drawTubeCap(out, mat, points.get(0), normals[0], bins[0], limb.radii().get(0), sides,
					red, green, blue, alpha, true);
			drawTubeCap(out, mat, points.get(points.size() - 1), normals[points.size() - 1], bins[points.size() - 1],
					limb.radii().get(limb.radii().size() - 1), sides, red, green, blue, alpha, false);
		}
	}

	private static double dot(QliphothBloomGeometry.Point a, QliphothBloomGeometry.Point b) {
		return a.x() * b.x() + a.y() * b.y() + a.z() * b.z();
	}

	private static void drawTubeCap(VertexConsumer out, Matrix4f mat, QliphothBloomGeometry.Point center,
			QliphothBloomGeometry.Point normal, QliphothBloomGeometry.Point binormal, double radius, int sides,
			float red, float green, float blue, float alpha, boolean reverse) {
		if (radius <= 1.0e-5) return;
		for (int side = 0; side < sides; side++) {
			double a0 = side * Math.PI * 2 / sides, a1 = (side + 1) * Math.PI * 2 / sides;
			QliphothBloomGeometry.Point p0 = ringPoint(center, normal, binormal, radius, a0);
			QliphothBloomGeometry.Point p1 = ringPoint(center, normal, binormal, radius, a1);
			if (reverse) emitTriangleAsQuad(out, mat, center, p1, p0, red, green, blue, alpha);
			else emitTriangleAsQuad(out, mat, center, p0, p1, red, green, blue, alpha);
		}
	}

	private static QliphothBloomGeometry.Point ringPoint(QliphothBloomGeometry.Point center,
			QliphothBloomGeometry.Point n, QliphothBloomGeometry.Point b, double radius, double angle) {
		return center.add(n.scale(Math.cos(angle) * radius)).add(b.scale(Math.sin(angle) * radius));
	}

	private static void drawSphere(MultiBufferSource buffer, Matrix4f mat, QliphothBloomGeometry.Point c,
			double radius, float red, float green, float blue, float alpha, int latitudes, int longitudes) {
		VertexConsumer out = alpha < .5f ? buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW)
				: buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
		for (int lat = 0; lat < latitudes; lat++) for (int lon = 0; lon < longitudes; lon++) {
			double t0 = lat * Math.PI / latitudes, t1 = (lat + 1) * Math.PI / latitudes;
			double p0 = lon * Math.PI * 2 / longitudes, p1 = (lon + 1) * Math.PI * 2 / longitudes;
			QliphothBloomGeometry.Point a = spherePoint(c, radius, t0, p0);
			QliphothBloomGeometry.Point b = spherePoint(c, radius, t0, p1);
			QliphothBloomGeometry.Point d = spherePoint(c, radius, t1, p0);
			QliphothBloomGeometry.Point e = spherePoint(c, radius, t1, p1);
			emitPointQuad(out, mat, a, b, e, d, red, green, blue, alpha);
		}
	}

	private static QliphothBloomGeometry.Point spherePoint(QliphothBloomGeometry.Point c, double r, double theta, double phi) {
		return c.add(new QliphothBloomGeometry.Point(Math.sin(theta) * Math.cos(phi) * r,
				Math.cos(theta) * r, Math.sin(theta) * Math.sin(phi) * r));
	}

	private static void drawDistortedEllipsoid(MultiBufferSource buffer, Matrix4f mat,
			QliphothBloomGeometry.Point center, double rx, double ry, double rz, double yaw, int seed,
			float red, float green, float blue, float alpha) {
		VertexConsumer out = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
		int latitudes = EFFECT_LATITUDES, longitudes = EFFECT_LONGITUDES;
		for (int lat = 0; lat < latitudes; lat++) for (int lon = 0; lon < longitudes; lon++) {
			double t0 = lat * Math.PI / latitudes, t1 = (lat + 1) * Math.PI / latitudes;
			double p0 = lon * Math.PI * 2 / longitudes, p1 = (lon + 1) * Math.PI * 2 / longitudes;
			QliphothBloomGeometry.Point a = distortedPoint(center, rx, ry, rz, yaw, seed, t0, p0);
			QliphothBloomGeometry.Point b = distortedPoint(center, rx, ry, rz, yaw, seed, t0, p1);
			QliphothBloomGeometry.Point c = distortedPoint(center, rx, ry, rz, yaw, seed, t1, p1);
			QliphothBloomGeometry.Point d = distortedPoint(center, rx, ry, rz, yaw, seed, t1, p0);
			emitPointQuad(out, mat, a, b, c, d, red, green, blue, alpha);
		}
	}

	private static QliphothBloomGeometry.Point distortedPoint(QliphothBloomGeometry.Point center,
			double rx, double ry, double rz, double yaw, int seed, double theta, double phi) {
		double irregular = 1 + Math.sin(phi * 3 + seed * .73) * .10 * Math.sin(theta)
				+ Math.cos(phi * 5 - seed * .41) * .055;
		double x = Math.sin(theta) * Math.cos(phi) * rx * irregular;
		double y = Math.cos(theta) * ry * (1 + Math.sin(phi * 2 + seed) * .045);
		double z = Math.sin(theta) * Math.sin(phi) * rz * irregular;
		return center.add(new QliphothBloomGeometry.Point(x * Math.cos(yaw) - z * Math.sin(yaw), y,
				x * Math.sin(yaw) + z * Math.cos(yaw)));
	}

	private static void emitPointQuad(VertexConsumer out, Matrix4f mat, QliphothBloomGeometry.Point a,
			QliphothBloomGeometry.Point b, QliphothBloomGeometry.Point c, QliphothBloomGeometry.Point d,
			float red, float green, float blue, float alpha) {
		emitQuad(out, mat, (float)a.x(), (float)a.y(), (float)a.z(), red, green, blue, alpha,
				(float)b.x(), (float)b.y(), (float)b.z(), red, green, blue, alpha,
				(float)c.x(), (float)c.y(), (float)c.z(), red, green, blue, alpha,
				(float)d.x(), (float)d.y(), (float)d.z(), red, green, blue, alpha);
	}

	private static void emitTriangleAsQuad(VertexConsumer out, Matrix4f mat, QliphothBloomGeometry.Point a,
			QliphothBloomGeometry.Point b, QliphothBloomGeometry.Point c,
			float red, float green, float blue, float alpha) {
		emitPointQuad(out, mat, a, b, c, c, red, green, blue, alpha);
	}

	private static void emitMonolithTriangle(VertexConsumer consumer, Matrix4f mat,
			QliphothSeveredGeometry.RimPoint a, QliphothSeveredGeometry.RimPoint b,
			QliphothSeveredGeometry.RimPoint c) {
		emitMonolithVertex(consumer, mat, a);
		emitMonolithVertex(consumer, mat, b);
		emitMonolithVertex(consumer, mat, c);
	}

	private static void emitMonolithVertex(VertexConsumer consumer, Matrix4f mat,
			QliphothSeveredGeometry.RimPoint point) {
		consumer.addVertex(mat, point.x(), point.y(), point.z())
				.setColor(2, 0, 0, 255)
				.setUv(point.u(), point.v())
				.setLight(LightTexture.FULL_BRIGHT);
	}

	/**
	 * Draws the main trunk — a tapered column of dark quads with subtle red veining.
	 * Tapers smoothly to a point at the top. The base flares outward with organic
	 * buttress ridges that align with root positions, blending the trunk into the roots.
	 *
	 * @param heightFrac fraction of {@link #TREE_HEIGHT} to draw (0..1); drives early growth
	 */
	private static void drawTrunk(MultiBufferSource buffer,
			Matrix4f mat, float time, double pulse, float heightFrac) {

		float effectiveHeight = TREE_HEIGHT * heightFrac;

		// Precompute root base angles for buttress bulging
		double[] rootAngles = new double[ROOT_COUNT];
		for (int i = 0; i < ROOT_COUNT; i++) {
			rootAngles[i] = (Math.PI * 2.0 / ROOT_COUNT) * i;
		}

		for (int v = 0; v < TRUNK_SEGMENTS_V; v++) {
			float t0 = (float) v / TRUNK_SEGMENTS_V;
			float t1 = (float) (v + 1) / TRUNK_SEGMENTS_V;

			float y0 = t0 * effectiveHeight;
			float y1 = t1 * effectiveHeight;

			// Smooth taper to a point: quadratic ease-in (1-t²) gives a natural tree shape
			// (wider at base, accelerating taper toward the tip)
			float taper0 = 1.0f - t0 * t0;
			float taper1 = 1.0f - t1 * t1;
			float baseR0 = TRUNK_BASE_RADIUS * taper0;
			float baseR1 = TRUNK_BASE_RADIUS * taper1;

			// ── Buttress flare: widen the lowest portion of the trunk ──
			// flareStrength is 1.0 at the very base and drops to 0 by ~25% up the trunk
			float flareStrength0 = Math.max(0f, 1.0f - t0 * 4.0f);
			float flareStrength1 = Math.max(0f, 1.0f - t1 * 4.0f);
			// Smooth the falloff with a cubic ease-out so it's not a hard cutoff
			flareStrength0 = flareStrength0 * flareStrength0 * (3.0f - 2.0f * flareStrength0);
			flareStrength1 = flareStrength1 * flareStrength1 * (3.0f - 2.0f * flareStrength1);
			// Uniform radial flare amount
			float flareAmount = 0.25f;

			// Subtle sway
			float swayX0 = (float) (Math.sin(time * 0.02 + t0 * 2.0) * 0.05 * t0);
			float swayZ0 = (float) (Math.cos(time * 0.025 + t0 * 1.5) * 0.04 * t0);
			float swayX1 = (float) (Math.sin(time * 0.02 + t1 * 2.0) * 0.05 * t1);
			float swayZ1 = (float) (Math.cos(time * 0.025 + t1 * 1.5) * 0.04 * t1);

			for (int h = 0; h < TRUNK_SEGMENTS_H; h++) {
				double a1 = Math.toRadians((360.0 / TRUNK_SEGMENTS_H) * h);
				double a2 = Math.toRadians((360.0 / TRUNK_SEGMENTS_H) * (h + 1));

				float cos1 = (float) Math.cos(a1);
				float sin1 = (float) Math.sin(a1);
				float cos2 = (float) Math.cos(a2);
				float sin2 = (float) Math.sin(a2);

				// ── Per-vertex buttress bulge toward nearest root angles ──
				float bulge1_r0 = computeRootBulge(a1, rootAngles, flareStrength0);
				float bulge2_r0 = computeRootBulge(a2, rootAngles, flareStrength0);
				float bulge1_r1 = computeRootBulge(a1, rootAngles, flareStrength1);
				float bulge2_r1 = computeRootBulge(a2, rootAngles, flareStrength1);

				// Final radii: base taper + uniform flare + directional root bulge
				float r0_v1 = baseR0 + flareStrength0 * flareAmount + bulge1_r0;
				float r0_v2 = baseR0 + flareStrength0 * flareAmount + bulge2_r0;
				float r1_v1 = baseR1 + flareStrength1 * flareAmount + bulge1_r1;
				float r1_v2 = baseR1 + flareStrength1 * flareAmount + bulge2_r1;

				// Dark trunk color (near-black with subtle dark red)
				float trunkR = (float) (0.08 + 0.03 * pulse);
				float trunkG = 0.02f;
				float trunkB = 0.02f;
				float trunkA = 0.90f;

				// Red vein pulsing along trunk
				float veinPulse = (float) (Math.sin(time * 0.08 + h * 1.3 + v * 0.5) + 1.0) * 0.5f;
				float veinR = (float) (0.35 + 0.25 * veinPulse * pulse);
				float veinG = 0.02f;
				float veinB = 0.02f;
				float veinA = 0.15f * veinPulse;

				// Core trunk quad
				emitCore(buffer, mat,
						cos1 * r0_v1 + swayX0, y0, sin1 * r0_v1 + swayZ0, trunkR, trunkG, trunkB, trunkA,
						cos2 * r0_v2 + swayX0, y0, sin2 * r0_v2 + swayZ0, trunkR, trunkG, trunkB, trunkA,
						cos2 * r1_v2 + swayX1, y1, sin2 * r1_v2 + swayZ1, trunkR, trunkG, trunkB, trunkA,
						cos1 * r1_v1 + swayX1, y1, sin1 * r1_v1 + swayZ1, trunkR, trunkG, trunkB, trunkA);

				// Glow overlay for veins
				emitGlow(buffer, mat,
						cos1 * (r0_v1 + 0.02f) + swayX0, y0, sin1 * (r0_v1 + 0.02f) + swayZ0, veinR, veinG, veinB, veinA,
						cos2 * (r0_v2 + 0.02f) + swayX0, y0, sin2 * (r0_v2 + 0.02f) + swayZ0, veinR, veinG, veinB, veinA,
						cos2 * (r1_v2 + 0.02f) + swayX1, y1, sin2 * (r1_v2 + 0.02f) + swayZ1, veinR, veinG, veinB, veinA,
						cos1 * (r1_v1 + 0.02f) + swayX1, y1, sin1 * (r1_v1 + 0.02f) + swayZ1, veinR, veinG, veinB, veinA);
			}
		}
	}

	/**
	 * Computes the buttress bulge amount at a given circumference angle based on
	 * proximity to root base angles. Creates smooth ridges that radiate from the
	 * trunk toward each root, blending the two together organically.
	 *
	 * @param circumAngle  the angle around the trunk circumference (radians)
	 * @param rootAngles   array of root base angles
	 * @param flareStrength how much flare is active at this height (0 = none, 1 = full)
	 * @return additional radius to add at this angle
	 */
	private static float computeRootBulge(double circumAngle, double[] rootAngles, float flareStrength) {
		if (flareStrength <= 0f) return 0f;

		float totalBulge = 0f;
		// Width of each buttress ridge (in radians) — controls how sharp/broad the ridges are
		float ridgeWidth = 0.55f;
		// Maximum extra radius each ridge contributes
		float ridgeStrength = 0.30f;

		for (double rootAng : rootAngles) {
			// Angular distance (wrapped to -PI..PI)
			double diff = circumAngle - rootAng;
			diff = ((diff + Math.PI) % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2) - Math.PI;
			float absDiff = (float) Math.abs(diff);

			// Smooth ridge falloff: cosine-based bell curve
			if (absDiff < ridgeWidth) {
				float ridgeFactor = (float) (Math.cos(absDiff / ridgeWidth * Math.PI) + 1.0) * 0.5f;
				totalBulge = Math.max(totalBulge, ridgeFactor * ridgeStrength * flareStrength);
			}
		}

		return totalBulge;
	}

	/**
	 * Draws small dark orbs that fade in and out at different positions on the
	 * trunk surface. Each orb runs on its own time cycle — it fades in at a
	 * random spot, holds briefly, fades out, then reappears at a new location.
	 *
	 * @param heightFrac fraction of {@link #TREE_HEIGHT} the trunk currently reaches;
	 *                   orbs are clamped to this height so they never float above the trunk tip
	 */
	private static void drawTrunkOrbs(MultiBufferSource buffer,
			Matrix4f mat, float time, double pulse, float heightFrac) {

		float effectiveHeight = TREE_HEIGHT * heightFrac;

		for (int i = 0; i < TRUNK_ORB_COUNT; i++) {
			// ── Per-orb lifecycle timing ──
			// Each orb has a different cycle duration and phase offset
			double phaseOffset = hashIndex(i + 1300) * 200.0; // large offset so they don't sync
			float cycleDuration = 120f + (float) hashIndex(i + 1400) * 80f; // 120-200 ticks per cycle

			// Current position in the cycle (0..1)
			float cyclePos = (float) (((time + phaseOffset) % cycleDuration) / cycleDuration);

			// Fade envelope: fade in (0..0.2), hold (0.2..0.7), fade out (0.7..1.0)
			float fadeAlpha;
			if (cyclePos < 0.2f) {
				fadeAlpha = cyclePos / 0.2f; // fade in
			} else if (cyclePos < 0.7f) {
				fadeAlpha = 1.0f; // hold
			} else {
				fadeAlpha = 1.0f - (cyclePos - 0.7f) / 0.3f; // fade out
			}
			// Smoothstep the fade for a softer transition
			fadeAlpha = fadeAlpha * fadeAlpha * (3.0f - 2.0f * fadeAlpha);

			if (fadeAlpha < 0.01f) continue; // fully invisible, skip rendering

			// ── Position changes each cycle ──
			// Cycle index increments each time the orb completes a full cycle
			int cycleIndex = (int) ((time + phaseOffset) / cycleDuration);

			// Use cycle index + orb index to seed position so it changes each cycle
			double seedH = hashIndex(i * 73 + cycleIndex * 37 + 2000);
			double seedA = hashIndex(i * 59 + cycleIndex * 23 + 3000);
			double seedP = hashIndex(i * 41 + cycleIndex * 19 + 4000);

			// Height along the trunk: spread between 10% and 85% of effective height
			float t = 0.10f + (float) seedH * 0.75f;
			float orbCenterY = t * effectiveHeight;
			// Skip orbs that would be above the current trunk tip (can occur during growth)
			if (orbCenterY > effectiveHeight - TRUNK_ORB_RADIUS) continue;

			// Trunk radius at this height (matching drawTrunk taper logic)
			float taper = 1.0f - t * t;
			float trunkR = TRUNK_BASE_RADIUS * taper;

			// Circumference angle — fully random per cycle, no spiral
			double angle = seedA * Math.PI * 2.0;

			// Trunk sway at this height
			float swayX = (float) (Math.sin(time * 0.02 + t * 2.0) * 0.05 * t);
			float swayZ = (float) (Math.cos(time * 0.025 + t * 1.5) * 0.04 * t);

			// Orb center on the trunk surface (slightly inset so it looks embedded)
			float embedDepth = TRUNK_ORB_RADIUS * 0.4f;
			float surfaceDist = trunkR - embedDepth;
			float orbX = swayX + (float) Math.cos(angle) * surfaceDist;
			float orbZ = swayZ + (float) Math.sin(angle) * surfaceDist;

			// ── Dark sphere ──
			float orbPulse = (float) (Math.sin(time * 0.06 + seedP * 10.0) + 1.0) * 0.5f;
			float oR = (float) (0.02 + 0.015 * orbPulse);
			float oG = 0.004f;
			float oB = 0.004f;
			float oA = 0.95f * fadeAlpha;

			// Scale the orb radius with the fade so it grows in and shrinks out
			float currentRadius = TRUNK_ORB_RADIUS * (0.5f + 0.5f * fadeAlpha);

			for (int lat = 0; lat < TRUNK_ORB_LAT; lat++) {
				double theta0 = Math.PI * lat / TRUNK_ORB_LAT;
				double theta1 = Math.PI * (lat + 1) / TRUNK_ORB_LAT;

				float cosT0 = (float) Math.cos(theta0);
				float sinT0 = (float) Math.sin(theta0);
				float cosT1 = (float) Math.cos(theta1);
				float sinT1 = (float) Math.sin(theta1);

				for (int lon = 0; lon < TRUNK_ORB_LON; lon++) {
					double phi0 = 2.0 * Math.PI * lon / TRUNK_ORB_LON;
					double phi1 = 2.0 * Math.PI * (lon + 1) / TRUNK_ORB_LON;

					float cosP0 = (float) Math.cos(phi0);
					float sinP0 = (float) Math.sin(phi0);
					float cosP1 = (float) Math.cos(phi1);
					float sinP1 = (float) Math.sin(phi1);

					float x00 = orbX + sinT0 * cosP0 * currentRadius;
					float y00 = orbCenterY + cosT0 * currentRadius;
					float z00 = orbZ + sinT0 * sinP0 * currentRadius;

					float x01 = orbX + sinT0 * cosP1 * currentRadius;
					float y01 = orbCenterY + cosT0 * currentRadius;
					float z01 = orbZ + sinT0 * sinP1 * currentRadius;

					float x10 = orbX + sinT1 * cosP0 * currentRadius;
					float y10 = orbCenterY + cosT1 * currentRadius;
					float z10 = orbZ + sinT1 * sinP0 * currentRadius;

					float x11 = orbX + sinT1 * cosP1 * currentRadius;
					float y11 = orbCenterY + cosT1 * currentRadius;
					float z11 = orbZ + sinT1 * sinP1 * currentRadius;

					emitCore(buffer, mat,
							x00, y00, z00, oR, oG, oB, oA,
							x01, y01, z01, oR, oG, oB, oA,
							x11, y11, z11, oR, oG, oB, oA,
							x10, y10, z10, oR, oG, oB, oA);
				}
			}

			// ── Glow halo around each embedded orb ──
			float glowRadius = currentRadius * 2.2f;
			float glowPulse = (float) (Math.sin(time * 0.07 + seedP * 8.0 + i * 1.5) + 1.0) * 0.5f;
			float gR = (float) (0.50 + 0.30 * glowPulse);
			float gA = (float) (0.10 + 0.08 * glowPulse) * fadeAlpha;

			// Billboard-style glow diamond facing outward from trunk
			float outX = (float) Math.cos(angle);
			float outZ = (float) Math.sin(angle);
			float perpX = (float) -Math.sin(angle);
			float perpZ = (float) Math.cos(angle);

			float glowCX = swayX + outX * (trunkR + 0.01f);
			float glowCZ = swayZ + outZ * (trunkR + 0.01f);

			emitQuad(buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), mat,
					glowCX - perpX * glowRadius, orbCenterY, glowCZ - perpZ * glowRadius, gR, 0.01f, 0.01f, 0f,
					glowCX, orbCenterY + glowRadius, glowCZ, gR, 0.01f, 0.01f, gA,
					glowCX + perpX * glowRadius, orbCenterY, glowCZ + perpZ * glowRadius, gR, 0.01f, 0.01f, 0f,
					glowCX, orbCenterY - glowRadius, glowCZ, gR, 0.01f, 0.01f, 0f);
		}
	}

	/**
	 * Draws surface roots that spread outward from the trunk base.
	 * Roots connect seamlessly to the trunk, have 3D volume (tube-like cross section),
	 * writhe with time-based sinusoidal animation, and branch off into
	 * smaller sub-roots to look like veins rather than tentacles.
	 */
	private static void drawRoots(MultiBufferSource buffer,
			Matrix4f mat, float time, double pulse, float lengthFrac) {

		for (int i = 0; i < ROOT_COUNT; i++) {
			double baseAngle = (Math.PI * 2.0 / ROOT_COUNT) * i;
			double seed = hashIndex(i) * 0.5 + 0.5;
			float rootLength = (float) ((1.8 + seed * 2.0) * lengthFrac);
			double curvature = Math.sin(seed * 13.7) * 0.4;

			// Writhing frequency and phase unique to each root
			double writheFreq = 0.06 + seed * 0.03;
			double writhePhase = i * 2.3;

			float mainStartDist = TRUNK_BASE_RADIUS * 0.85f;

			drawSingleRoot(buffer, mat, time, pulse,
					baseAngle, rootLength, curvature,
					mainStartDist, 0f, 0f, 0f, writheFreq, writhePhase,
					0.10f, ROOT_SEGS, i);

			// Sub-branches fork off from the main root
			for (int sub = 0; sub < ROOT_SUB_BRANCHES; sub++) {
				double subSeed = hashIndex(i * 17 + sub * 7 + 500);
				// Fork point along the main root (between 25% and 70%)
				float forkT = 0.25f + (float) subSeed * 0.45f;

				// Compute the actual fork position on the main root (matching drawSingleRoot logic)
				float forkRad = mainStartDist + forkT * rootLength;
				double forkAngle = baseAngle + curvature * forkT * forkT;

				// Writhing displacement at the fork point (same formulae as in drawSingleRoot)
				float forkWritheAmp = 0.06f + forkT * 0.10f;
				float forkWritheX = (float) (Math.sin(time * writheFreq + writhePhase + forkT * 4.0) * forkWritheAmp);
				float forkWritheY = (float) (Math.sin(time * writheFreq * 0.7 + writhePhase + 1.5 + forkT * 3.5) * forkWritheAmp * 0.5f);

				// Y at the fork point on the main root (same formula as drawSingleRoot)
				float forkYBase = (float) (-0.02 - forkT * 0.35 * (1.0 + Math.sin(forkT * Math.PI) * 0.6));
				float forkY = forkYBase + forkWritheY;

				float forkPerpX = (float) -Math.sin(forkAngle);
				float forkPerpZ = (float) Math.cos(forkAngle);

				// Actual fork point on the main root in world-local coords
				float forkX = (float) Math.cos(forkAngle) * forkRad + forkPerpX * forkWritheX;
				float forkZ = (float) Math.sin(forkAngle) * forkRad + forkPerpZ * forkWritheX;

				// Sub-branch diverges at an angle from the main root's direction at the fork
				double subAngleOffset = (subSeed - 0.5) * 1.2 + (sub == 0 ? 0.35 : -0.35);
				double subAngle = forkAngle + subAngleOffset;
				float subLength = rootLength * (0.3f + (float) subSeed * 0.3f);

				// Writhe slightly differently from parent
				double subWritheFreq = writheFreq * 1.3;
				double subWrithePhase = writhePhase + sub * 3.7;

				// Starting radius smaller than main root at fork point
				float subStartRadius = 0.06f * (1.0f - forkT * 0.5f);

				// Draw sub-root starting from the actual fork point, growing outward from there
				drawSingleRoot(buffer, mat, time, pulse,
						subAngle, subLength, curvature * 0.6,
						0f, forkX, forkY, forkZ, subWritheFreq, subWrithePhase,
						subStartRadius, ROOT_SUB_SEGS, i * 100 + sub);
			}
		}
	}

	/**
	 * Draws a single root strand as a 3D tube with writhing animation.
	 * Used for both main roots and sub-branch roots.
	 * <p>
	 * All ring positions are precomputed so adjacent segments share exact vertex
	 * positions at their boundary, eliminating visible seams between segments.
	 *
	 * @param originX X offset for the root origin (0 for main roots, fork point X for sub-branches)
	 * @param originY Y offset for the root origin (0 for main roots, fork point Y for sub-branches)
	 * @param originZ Z offset for the root origin (0 for main roots, fork point Z for sub-branches)
	 */
	private static void drawSingleRoot(MultiBufferSource buffer,
			Matrix4f mat, float time, double pulse,
			double baseAngle, float rootLength, double curvature,
			float startDist, float originX, float originY, float originZ, double writheFreq, double writhePhase,
			float baseWidth, int segments, int colorSeed) {

		int tubeFaces = 4;
		int ringCount = segments + 1;

		// ── Precompute per-ring data ──
		float[] ringCX = new float[ringCount];
		float[] ringCZ = new float[ringCount];
		float[] ringY  = new float[ringCount];
		float[] ringW  = new float[ringCount];
		float[] ringPerpX = new float[ringCount];
		float[] ringPerpZ = new float[ringCount];

		for (int r = 0; r < ringCount; r++) {
			float t = (float) r / segments;

			float rad = startDist + t * rootLength;
			double ang = baseAngle + curvature * t * t;

			float writheAmp = 0.06f + t * 0.10f;
			float writheX = (float) (Math.sin(time * writheFreq + writhePhase + t * 4.0) * writheAmp);
			float writheY = (float) (Math.sin(time * writheFreq * 0.7 + writhePhase + 1.5 + t * 3.5) * writheAmp * 0.5f);

			float yBase = (float) (-0.02 - t * 0.35 * (1.0 + Math.sin(t * Math.PI) * 0.6));

			float cosA = (float) Math.cos(ang);
			float sinA = (float) Math.sin(ang);
			float pX = (float) -Math.sin(ang);
			float pZ = (float)  Math.cos(ang);

			ringCX[r] = originX + cosA * rad + pX * writheX;
			ringCZ[r] = originZ + sinA * rad + pZ * writheX;
			ringY[r]  = originY + yBase + writheY;
			ringW[r]  = baseWidth * (1.0f - t * 0.8f);
			ringPerpX[r] = pX;
			ringPerpZ[r] = pZ;
		}

		// ── Draw tube segments between adjacent precomputed rings ──
		for (int s = 0; s < segments; s++) {
			float t0 = (float) s / segments;

			// Colors
			float rootR = (float) (0.06 + 0.04 * pulse);
			float rootG = 0.01f;
			float rootB = 0.01f;
			float rootA = 0.85f * (1.0f - t0 * 0.35f);

			float veinPulse = (float) (Math.sin(time * 0.09 + colorSeed * 0.7 + t0 * 5.0) + 1.0) * 0.5f;
			float glowR = (float) (0.45 + 0.35 * veinPulse * pulse);
			float glowA = 0.12f * veinPulse * (1.0f - t0 * 0.5f);

			for (int f = 0; f < tubeFaces; f++) {
				double faceAngle0 = Math.toRadians((360.0 / tubeFaces) * f);
				double faceAngle1 = Math.toRadians((360.0 / tubeFaces) * (f + 1));

				float fCos0 = (float) Math.cos(faceAngle0);
				float fSin0 = (float) Math.sin(faceAngle0);
				float fCos1 = (float) Math.cos(faceAngle1);
				float fSin1 = (float) Math.sin(faceAngle1);

				// Ring s vertices
				float x00 = ringCX[s] + ringPerpX[s] * fCos0 * ringW[s];
				float y00 = ringY[s]  + fSin0 * ringW[s];
				float z00 = ringCZ[s] + ringPerpZ[s] * fCos0 * ringW[s];

				float x01 = ringCX[s] + ringPerpX[s] * fCos1 * ringW[s];
				float y01 = ringY[s]  + fSin1 * ringW[s];
				float z01 = ringCZ[s] + ringPerpZ[s] * fCos1 * ringW[s];

				// Ring s+1 vertices
				float x10 = ringCX[s+1] + ringPerpX[s+1] * fCos0 * ringW[s+1];
				float y10 = ringY[s+1]  + fSin0 * ringW[s+1];
				float z10 = ringCZ[s+1] + ringPerpZ[s+1] * fCos0 * ringW[s+1];

				float x11 = ringCX[s+1] + ringPerpX[s+1] * fCos1 * ringW[s+1];
				float y11 = ringY[s+1]  + fSin1 * ringW[s+1];
				float z11 = ringCZ[s+1] + ringPerpZ[s+1] * fCos1 * ringW[s+1];

				// Core root quad
				emitCore(buffer, mat,
						x00, y00, z00, rootR, rootG, rootB, rootA,
						x01, y01, z01, rootR, rootG, rootB, rootA,
						x11, y11, z11, rootR, rootG, rootB, rootA * 0.9f,
						x10, y10, z10, rootR, rootG, rootB, rootA * 0.9f);

				// Glow overlay for veining effect
				float glowOff = 0.015f;
				emitGlow(buffer, mat,
						x00 + ringPerpX[s] * glowOff, y00, z00 + ringPerpZ[s] * glowOff, glowR, 0.02f, 0.02f, glowA,
						x01 + ringPerpX[s] * glowOff, y01, z01 + ringPerpZ[s] * glowOff, glowR, 0.02f, 0.02f, glowA,
						x11 + ringPerpX[s+1] * glowOff, y11, z11 + ringPerpZ[s+1] * glowOff, glowR, 0.02f, 0.02f, glowA * 0.7f,
						x10 + ringPerpX[s+1] * glowOff, y10, z10 + ringPerpZ[s+1] * glowOff, glowR, 0.02f, 0.02f, glowA * 0.7f);
			}
		}
	}

	/**
	 * Draws branches that spread from the upper trunk, curving organically
	 * outward and upward with lateral bending and gentle writhing.
	 * Each segment bends from the previous one for a natural, non-linear shape.
	 * Sub-branches fork off main branches for added complexity.
	 *
	 * @param lengthFrac      branch length as a fraction of full length (0..1)
	 * @param drawSubBranches whether to draw sub-branches (true from stage 7+)
	 */
	private static void drawBranches(MultiBufferSource buffer,
			Matrix4f mat, float time, double pulse, float lengthFrac, boolean drawSubBranches) {

		for (int b = 0; b < BRANCH_COUNT; b++) {
			double baseAngle = (Math.PI * 2.0 / BRANCH_COUNT) * b;
			double seed = hashIndex(b + 100);
			double seed2 = hashIndex(b + 150);

			// Branches start between 40%-80% up the trunk
			float startHeight = TREE_HEIGHT * (0.4f + (float) seed * 0.4f);
			float branchLen = BRANCH_LENGTH * (0.6f + (float) seed * 0.4f) * lengthFrac;
			float segLen = branchLen / BRANCH_SEGS;

			// Initial direction: outward and upward (elevation from horizontal)
			float elevAngle = (float) (0.3 + seed * 0.5);

			// Per-branch lateral curvature strength and direction (unique bend per branch)
			double lateralCurve = (seed2 - 0.5) * 0.18;
			// Per-branch vertical curvature: some droop, some reach up
			double verticalCurve = 0.06 + seed * 0.08;

			// Accumulate position segment-by-segment
			float curX = 0f, curY = startHeight, curZ = 0f;
			double curAzimuth = baseAngle;
			double curElev = elevAngle;

			// Trunk sway at start height for seamless attachment
			float startT = startHeight / TREE_HEIGHT;
			float trunkSwayX = (float) (Math.sin(time * 0.02 + startT * 2.0) * 0.05 * startT);
			float trunkSwayZ = (float) (Math.cos(time * 0.025 + startT * 1.5) * 0.04 * startT);
			curX += trunkSwayX;
			curZ += trunkSwayZ;

			// Store per-segment endpoint data for sub-branch forking
			float[] segX = new float[BRANCH_SEGS + 1];
			float[] segY = new float[BRANCH_SEGS + 1];
			float[] segZ = new float[BRANCH_SEGS + 1];
			double[] segAz = new double[BRANCH_SEGS + 1];
			double[] segEl = new double[BRANCH_SEGS + 1];
			segX[0] = curX; segY[0] = curY; segZ[0] = curZ;
			segAz[0] = curAzimuth; segEl[0] = curElev;

			for (int s = 0; s < BRANCH_SEGS; s++) {
				float t0 = (float) s / BRANCH_SEGS;
				float t1 = (float) (s + 1) / BRANCH_SEGS;

				// Save start of this segment
				float x0 = curX;
				float y0 = curY;
				float z0 = curZ;
				double az0 = curAzimuth;

				// Bend azimuth (lateral curvature — S-curve via sin for organic feel)
				curAzimuth += lateralCurve + Math.sin(t1 * Math.PI * 1.5 + seed * 5.0) * 0.08;

				// Bend elevation upward progressively (branches reach for the sky)
				curElev += verticalCurve * (1.0 + t1 * 0.5);

				// Per-segment writhing/sway that grows along the branch
				float writheAmt = t1 * 0.12f;
				float writheAz = (float) (Math.sin(time * 0.03 + b * 1.7 + t1 * 3.0) * writheAmt);
				float writheElev = (float) (Math.sin(time * 0.025 + b * 2.3 + t1 * 2.5) * writheAmt * 0.5f);

				// Step forward along the current direction
				double az1 = curAzimuth + writheAz;
				double el1 = curElev + writheElev;
				float dx = (float) (Math.cos(az1) * Math.cos(el1) * segLen);
				float dy = (float) (Math.sin(el1) * segLen);
				float dz = (float) (Math.sin(az1) * Math.cos(el1) * segLen);

				curX += dx;
				curY += dy;
				curZ += dz;

				float x1 = curX;
				float y1 = curY;
				float z1 = curZ;

				// Record endpoint
				segX[s + 1] = curX; segY[s + 1] = curY; segZ[s + 1] = curZ;
				segAz[s + 1] = curAzimuth; segEl[s + 1] = curElev;

				// Branch width tapers
				float w0 = 0.06f * (1.0f - t0 * 0.75f);
				float w1 = 0.06f * (1.0f - t1 * 0.75f);

				// Dark branch color
				float brR = (float) (0.06 + 0.03 * pulse);
				float brG = 0.01f;
				float brB = 0.01f;
				float brA = 0.85f * (1.0f - t0 * 0.3f);

				// Perpendicular direction derived from actual segment direction for proper facing
				float segDx = x1 - x0;
				float segDz = z1 - z0;
				float segHLen = (float) Math.sqrt(segDx * segDx + segDz * segDz);
				float px, pz;
				if (segHLen > 0.001f) {
					px = -segDz / segHLen;
					pz = segDx / segHLen;
				} else {
					px = (float) -Math.sin(az0);
					pz = (float) Math.cos(az0);
				}

				emitCore(buffer, mat,
						x0 - px * w0, y0, z0 - pz * w0, brR, brG, brB, brA,
						x0 + px * w0, y0, z0 + pz * w0, brR, brG, brB, brA,
						x1 + px * w1, y1, z1 + pz * w1, brR, brG, brB, brA * 0.9f,
						x1 - px * w1, y1, z1 - pz * w1, brR, brG, brB, brA * 0.9f);

				// Red glow along branches
				float glowPulse = (float) (Math.sin(time * 0.07 + b * 1.2 + s * 0.8) + 1.0) * 0.5f;
				float gR = (float) (0.6 + 0.3 * glowPulse);
				float gA = 0.12f * glowPulse * (1.0f - t0 * 0.5f);

				emitGlow(buffer, mat,
						x0 - px * w0 * 2, y0, z0 - pz * w0 * 2, gR, 0.02f, 0.02f, gA,
						x0 + px * w0 * 2, y0, z0 + pz * w0 * 2, gR, 0.02f, 0.02f, gA,
						x1 + px * w1 * 2, y1, z1 + pz * w1 * 2, gR, 0.02f, 0.02f, gA * 0.7f,
						x1 - px * w1 * 2, y1, z1 - pz * w1 * 2, gR, 0.02f, 0.02f, gA * 0.7f);
			}

			// ── Sub-branches forking off the main branch ──
		if (drawSubBranches) {
			for (int sub = 0; sub < BRANCH_SUB_COUNT; sub++) {
				double subSeed = hashIndex(b * 31 + sub * 13 + 700);
				double subSeed2 = hashIndex(b * 23 + sub * 11 + 800);

				// Fork point: between 25% and 65% along the main branch
				float forkFrac = 0.25f + (float) subSeed * 0.40f;
				// Map to a segment index + interpolation fraction
				float forkSegF = forkFrac * BRANCH_SEGS;
				int forkSeg = Math.min((int) forkSegF, BRANCH_SEGS - 1);
				float forkLerp = forkSegF - forkSeg;

				// Interpolate fork position and direction from stored segment data
				float forkX = segX[forkSeg] + (segX[forkSeg + 1] - segX[forkSeg]) * forkLerp;
				float forkY = segY[forkSeg] + (segY[forkSeg + 1] - segY[forkSeg]) * forkLerp;
				float forkZ = segZ[forkSeg] + (segZ[forkSeg + 1] - segZ[forkSeg]) * forkLerp;
				double forkAz = segAz[forkSeg] + (segAz[forkSeg + 1] - segAz[forkSeg]) * forkLerp;
				double forkEl = segEl[forkSeg] + (segEl[forkSeg + 1] - segEl[forkSeg]) * forkLerp;

				// Sub-branch diverges from parent direction
				double subAzOffset = (subSeed2 - 0.5) * 1.6 + (sub == 0 ? 0.4 : -0.4);
				double subElOffset = (subSeed - 0.5) * 0.5;

				double subAz = forkAz + subAzOffset;
				double subEl = forkEl + subElOffset;

				// Sub-branch is shorter and thinner
				float subLen = branchLen * (0.25f + (float) subSeed * 0.2f);
				float subSegLen = subLen / BRANCH_SUB_SEGS;
				float subBaseWidth = 0.04f * (1.0f - forkFrac * 0.4f);

				// Sub-branch lateral and vertical curvature
				double subLatCurve = (subSeed2 - 0.5) * 0.22;
				double subVertCurve = 0.05 + subSeed * 0.06;

				float sCurX = forkX, sCurY = forkY, sCurZ = forkZ;
				double sCurAz = subAz, sCurEl = subEl;

				for (int ss = 0; ss < BRANCH_SUB_SEGS; ss++) {
					float st0 = (float) ss / BRANCH_SUB_SEGS;
					float st1 = (float) (ss + 1) / BRANCH_SUB_SEGS;

					float sx0 = sCurX, sy0 = sCurY, sz0 = sCurZ;
					double sAz0 = sCurAz;

					// Lateral and vertical bending
					sCurAz += subLatCurve + Math.sin(st1 * Math.PI * 1.3 + subSeed * 7.0) * 0.10;
					sCurEl += subVertCurve * (1.0 + st1 * 0.4);

					// Writhing
					float sWritheAmt = st1 * 0.09f;
					float sWritheAz = (float) (Math.sin(time * 0.035 + b * 2.1 + sub * 3.5 + st1 * 3.0) * sWritheAmt);
					float sWritheEl = (float) (Math.sin(time * 0.028 + b * 1.9 + sub * 2.7 + st1 * 2.5) * sWritheAmt * 0.5f);

					double sAz1 = sCurAz + sWritheAz;
					double sEl1 = sCurEl + sWritheEl;
					float sdx = (float) (Math.cos(sAz1) * Math.cos(sEl1) * subSegLen);
					float sdy = (float) (Math.sin(sEl1) * subSegLen);
					float sdz = (float) (Math.sin(sAz1) * Math.cos(sEl1) * subSegLen);

					sCurX += sdx;
					sCurY += sdy;
					sCurZ += sdz;

					float sx1 = sCurX, sy1 = sCurY, sz1 = sCurZ;

					// Width tapers
					float sw0 = subBaseWidth * (1.0f - st0 * 0.8f);
					float sw1 = subBaseWidth * (1.0f - st1 * 0.8f);

					// Color
					float sbrR = (float) (0.06 + 0.03 * pulse);
					float sbrG = 0.01f;
					float sbrB = 0.01f;
					float sbrA = 0.80f * (1.0f - st0 * 0.4f);

					// Perpendicular from segment direction
					float sSegDx = sx1 - sx0;
					float sSegDz = sz1 - sz0;
					float sSegHLen = (float) Math.sqrt(sSegDx * sSegDx + sSegDz * sSegDz);
					float spx, spz;
					if (sSegHLen > 0.001f) {
						spx = -sSegDz / sSegHLen;
						spz = sSegDx / sSegHLen;
					} else {
						spx = (float) -Math.sin(sAz0);
						spz = (float) Math.cos(sAz0);
					}

					emitCore(buffer, mat,
							sx0 - spx * sw0, sy0, sz0 - spz * sw0, sbrR, sbrG, sbrB, sbrA,
							sx0 + spx * sw0, sy0, sz0 + spz * sw0, sbrR, sbrG, sbrB, sbrA,
							sx1 + spx * sw1, sy1, sz1 + spz * sw1, sbrR, sbrG, sbrB, sbrA * 0.9f,
							sx1 - spx * sw1, sy1, sz1 - spz * sw1, sbrR, sbrG, sbrB, sbrA * 0.9f);

					// Glow
					float sGlowPulse = (float) (Math.sin(time * 0.08 + b * 1.5 + sub * 2.0 + ss * 0.9) + 1.0) * 0.5f;
					float sgR = (float) (0.55 + 0.3 * sGlowPulse);
					float sgA = 0.10f * sGlowPulse * (1.0f - st0 * 0.5f);

					emitGlow(buffer, mat,
							sx0 - spx * sw0 * 2, sy0, sz0 - spz * sw0 * 2, sgR, 0.02f, 0.02f, sgA,
							sx0 + spx * sw0 * 2, sy0, sz0 + spz * sw0 * 2, sgR, 0.02f, 0.02f, sgA,
							sx1 + spx * sw1 * 2, sy1, sz1 + spz * sw1 * 2, sgR, 0.02f, 0.02f, sgA * 0.7f,
							sx1 - spx * sw1 * 2, sy1, sz1 - spz * sw1 * 2, sgR, 0.02f, 0.02f, sgA * 0.7f);
				}
			} // end for (int sub...)
			} // end if (drawSubBranches)
		}
	}

	/**
	 * Draws canopy blooms — clusters of red/dark red glowing diamond shapes
	 * that twinkle and slowly wander around the tree crown, giving a living,
	 * shimmering canopy effect rather than static fixed-position blooms.
	 */
	private static void drawCanopy(MultiBufferSource buffer,
			Matrix4f mat, float time, double pulse) {

		for (int i = 0; i < CANOPY_BLOOMS; i++) {
			double seed = hashIndex(i + 200);
			double seed2 = hashIndex(i + 300);
			double seed3 = hashIndex(i + 400);

			// ── Wandering position: blooms drift slowly around the canopy ──
			// Base position (spread evenly) plus time-varying drift
			double baseAzimuth = (Math.PI * 2.0 / CANOPY_BLOOMS) * i + seed * 0.5;
			// Azimuth drifts back and forth over time (each bloom at its own speed/phase)
			double azimuthDrift = Math.sin(time * 0.012 + i * 2.7 + seed * 10.0) * 0.8
					+ Math.sin(time * 0.007 + i * 1.3) * 0.4;
			double azimuth = baseAzimuth + azimuthDrift;

			// Elevation wanders up and down the tree crown
			float baseElevation = TREE_HEIGHT * 0.55f + (float) seed * TREE_HEIGHT * 0.5f;
			float elevDrift = (float) (Math.sin(time * 0.015 + i * 1.9 + seed2 * 8.0) * TREE_HEIGHT * 0.15
					+ Math.sin(time * 0.009 + i * 3.1) * TREE_HEIGHT * 0.08);
			float elevation = baseElevation + elevDrift;

			// Outward distance drifts so blooms move closer/farther from trunk
			float baseOutward = BLOOM_RADIUS + (float) seed2 * 1.5f;
			float outwardDrift = (float) (Math.sin(time * 0.011 + i * 2.3 + seed3 * 6.0) * 0.6);
			float outward = baseOutward + outwardDrift;

			float bx = (float) (Math.cos(azimuth) * outward);
			float bz = (float) (Math.sin(azimuth) * outward);
			float by = elevation;

			// ── Twinkling: gentle shimmer with calm pulsing ──
			// Layer multiple slow sin waves for a soft sparkle effect
			float twinkle1 = (float) Math.max(0, Math.sin(time * 0.08 + i * 3.7 + seed * 20.0));
			float twinkle2 = (float) Math.max(0, Math.sin(time * 0.12 + i * 5.3 + seed2 * 15.0));
			float twinkle3 = (float) Math.max(0, Math.sin(time * 0.05 + i * 2.1 + seed3 * 12.0));
			// Soften the peaks — just a gentle square, not harsh
			twinkle1 *= twinkle1;
			twinkle2 *= twinkle2;
			twinkle3 *= twinkle3;
			// Combine: higher base visibility with gentle twinkle variation
			float twinkle = 0.35f + 0.65f * Math.max(twinkle1, Math.max(twinkle2, twinkle3));

			// Bloom size varies and pulses with the twinkle
			float baseBloomSize = 0.25f + (float) seed * 0.35f;
			float bloomSize = baseBloomSize * (0.6f + 0.4f * twinkle);

			// Alpha driven by twinkle — gentle variation, never fully dim
			float alpha = 0.30f + 0.55f * twinkle;

			// Red/dark-red bloom color — brighter during twinkle peaks
			float coreR = (float) (0.45 + 0.55 * twinkle);
			float coreG = (float) (0.02 + 0.03 * seed);
			float coreB = (float) (0.02 + 0.02 * seed);

			// Draw diamond-shaped bloom (4 triangles as quads)
			// Top
			emitQuad(buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), mat,
					bx - bloomSize * 0.5f, by, bz, coreR, coreG, coreB, alpha * 0.7f,
					bx, by + bloomSize, bz, coreR, coreG, coreB, alpha,
					bx + bloomSize * 0.5f, by, bz, coreR, coreG, coreB, alpha * 0.7f,
					bx, by, bz - bloomSize * 0.3f, coreR, coreG, coreB, alpha * 0.5f);

			// Bottom
			emitQuad(buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), mat,
					bx - bloomSize * 0.5f, by, bz, coreR, coreG, coreB, alpha * 0.7f,
					bx, by - bloomSize * 0.6f, bz, coreR * 0.5f, coreG, coreB, alpha * 0.5f,
					bx + bloomSize * 0.5f, by, bz, coreR, coreG, coreB, alpha * 0.7f,
					bx, by, bz + bloomSize * 0.3f, coreR, coreG, coreB, alpha * 0.5f);

			// Glow halo — also driven by twinkle so it flares during peaks
			float glowSize = bloomSize * 2.0f;
			float gA = alpha * 0.25f * twinkle;
			float gR = (float) (0.4 + 0.5 * twinkle);

			emitQuad(buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), mat,
					bx - glowSize, by, bz, gR, 0.01f, 0.01f, 0f,
					bx, by + glowSize, bz, gR, 0.01f, 0.01f, gA,
					bx + glowSize, by, bz, gR, 0.01f, 0.01f, 0f,
					bx, by - glowSize, bz, gR, 0.01f, 0.01f, 0f);
		}
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Apex Orb — dark "black hole" sphere at the very tip of the tree
	// ════════════════════════════════════════════════════════════════════════

	/** Radius of the dark apex orb. */
	private static final float ORB_RADIUS = 0.65f;
	/** Number of latitude bands for the orb sphere. */
	private static final int ORB_LAT = 10;
	/** Number of longitude segments for the orb sphere. */
	private static final int ORB_LON = 12;
	/** Number of concentric glow rings around the orb. */
	private static final int ORB_GLOW_RINGS = 3;

	/**
	 * Draws a large dark orb at the very tip of the tree that looks like a
	 * small black hole — an opaque near-black sphere surrounded by layers of
	 * pulsing dark-red / crimson glow halos that slowly rotate.
	 */
	private static void drawApexOrb(MultiBufferSource buffer,
			Matrix4f mat, float time, double pulse) {

		// Orb center: at the very top of the trunk, matching sway
		float tipT = 1.0f;
		float orbY = TREE_HEIGHT;
		float orbSwayX = (float) (Math.sin(time * 0.02 + tipT * 2.0) * 0.05 * tipT);
		float orbSwayZ = (float) (Math.cos(time * 0.025 + tipT * 1.5) * 0.04 * tipT);

		// Gentle slow bob
		orbY += (float) (Math.sin(time * 0.03) * 0.06);

		// ── Dark sphere core ──
		for (int lat = 0; lat < ORB_LAT; lat++) {
			double theta0 = Math.PI * lat / ORB_LAT;
			double theta1 = Math.PI * (lat + 1) / ORB_LAT;

			float cosT0 = (float) Math.cos(theta0);
			float sinT0 = (float) Math.sin(theta0);
			float cosT1 = (float) Math.cos(theta1);
			float sinT1 = (float) Math.sin(theta1);

			for (int lon = 0; lon < ORB_LON; lon++) {
				double phi0 = 2.0 * Math.PI * lon / ORB_LON;
				double phi1 = 2.0 * Math.PI * (lon + 1) / ORB_LON;

				float cosP0 = (float) Math.cos(phi0);
				float sinP0 = (float) Math.sin(phi0);
				float cosP1 = (float) Math.cos(phi1);
				float sinP1 = (float) Math.sin(phi1);

				// Sphere vertex positions
				float x00 = orbSwayX + sinT0 * cosP0 * ORB_RADIUS;
				float y00 = orbY     + cosT0 * ORB_RADIUS;
				float z00 = orbSwayZ + sinT0 * sinP0 * ORB_RADIUS;

				float x01 = orbSwayX + sinT0 * cosP1 * ORB_RADIUS;
				float y01 = orbY     + cosT0 * ORB_RADIUS;
				float z01 = orbSwayZ + sinT0 * sinP1 * ORB_RADIUS;

				float x10 = orbSwayX + sinT1 * cosP0 * ORB_RADIUS;
				float y10 = orbY     + cosT1 * ORB_RADIUS;
				float z10 = orbSwayZ + sinT1 * sinP0 * ORB_RADIUS;

				float x11 = orbSwayX + sinT1 * cosP1 * ORB_RADIUS;
				float y11 = orbY     + cosT1 * ORB_RADIUS;
				float z11 = orbSwayZ + sinT1 * sinP1 * ORB_RADIUS;

				// Very dark color — near-black with faint dark-red tinge pulsing
				float orbR = (float) (0.03 + 0.02 * pulse);
				float orbG = 0.005f;
				float orbB = 0.005f;
				float orbA = 0.95f;

				emitQuad(buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), mat,
						x00, y00, z00, orbR, orbG, orbB, orbA,
						x01, y01, z01, orbR, orbG, orbB, orbA,
						x11, y11, z11, orbR, orbG, orbB, orbA,
						x10, y10, z10, orbR, orbG, orbB, orbA);
			}
		}

		// ── Glow halos — rings that slowly rotate around the orb ──
		for (int ring = 0; ring < ORB_GLOW_RINGS; ring++) {
			double ringPhase = hashIndex(ring + 900);
			// Each ring tilts at a different angle and rotates at its own speed
			double tiltAngle = Math.PI * 0.3 * (ring + 1) / ORB_GLOW_RINGS;
			double rotSpeed = 0.015 + ring * 0.008;
			double rotAngle = time * rotSpeed + ringPhase * Math.PI * 2.0;

			float haloRadius = ORB_RADIUS * (1.3f + ring * 0.35f);
			float haloWidth = 0.06f + ring * 0.02f;

			// Glow color: deep red to crimson, pulsing
			float glowPulse = (float) (Math.sin(time * 0.05 + ring * 2.0) + 1.0) * 0.5f;
			float hR = (float) (0.55 + 0.35 * glowPulse);
			float hG = 0.02f;
			float hB = 0.02f;
			float hA = (float) (0.20 + 0.15 * glowPulse) * (1.0f - ring * 0.2f);

			int haloSegs = 36;
			for (int s = 0; s < haloSegs; s++) {
				double a0 = 2.0 * Math.PI * s / haloSegs;
				double a1 = 2.0 * Math.PI * (s + 1) / haloSegs;

				float innerR = haloRadius - haloWidth;
				float outerR = haloRadius + haloWidth;

				// Inner edge at a0, a1
				float ix0 = tiltAndRotateX(innerR, a0, tiltAngle, rotAngle);
				float iy0 = tiltAndRotateY(innerR, a0, tiltAngle) + orbY;
				float iz0 = tiltAndRotateZ(innerR, a0, tiltAngle, rotAngle);
				float ix1 = tiltAndRotateX(innerR, a1, tiltAngle, rotAngle);
				float iy1 = tiltAndRotateY(innerR, a1, tiltAngle) + orbY;
				float iz1 = tiltAndRotateZ(innerR, a1, tiltAngle, rotAngle);

				// Outer edge at a0, a1
				float ox0 = tiltAndRotateX(outerR, a0, tiltAngle, rotAngle);
				float oy0 = tiltAndRotateY(outerR, a0, tiltAngle) + orbY;
				float oz0 = tiltAndRotateZ(outerR, a0, tiltAngle, rotAngle);
				float ox1 = tiltAndRotateX(outerR, a1, tiltAngle, rotAngle);
				float oy1 = tiltAndRotateY(outerR, a1, tiltAngle) + orbY;
				float oz1 = tiltAndRotateZ(outerR, a1, tiltAngle, rotAngle);

				// Inner-to-outer strip quad with sway offset
				emitQuad(buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), mat,
						ix0 + orbSwayX, iy0, iz0 + orbSwayZ, hR, hG, hB, hA * 0.3f,
						ox0 + orbSwayX, oy0, oz0 + orbSwayZ, hR, hG, hB, hA,
						ox1 + orbSwayX, oy1, oz1 + orbSwayZ, hR, hG, hB, hA,
						ix1 + orbSwayX, iy1, iz1 + orbSwayZ, hR, hG, hB, hA * 0.3f);
			}
		}
	}

	/** Helper: tilt a ring point (radius, angle) by tiltAngle around X, then rotate around Y. Returns X component. */
	private static float tiltAndRotateX(float radius, double ringAngle, double tiltAngle, double rotAngle) {
		// Point on ring in XZ: (radius*cos(ringAngle), 0, radius*sin(ringAngle))
		// Tilt around X axis: y' = y*cos(tilt) - z*sin(tilt), z' = y*sin(tilt) + z*cos(tilt)
		float rx = radius * (float) Math.cos(ringAngle);
		float rz = radius * (float) Math.sin(ringAngle);
		float tiltedZ = (float) (rz * Math.cos(tiltAngle));
		// Rotate around Y axis: x'' = rx*cos(rot) - tiltedZ*sin(rot)
		return (float) (rx * Math.cos(rotAngle) - tiltedZ * Math.sin(rotAngle));
	}

	/** Helper: returns Y component after tilting. */
	private static float tiltAndRotateY(float radius, double ringAngle, double tiltAngle) {
		float rz = radius * (float) Math.sin(ringAngle);
		return (float) (-rz * Math.sin(tiltAngle));
	}

	/** Helper: returns Z component after tilt + Y rotation. */
	private static float tiltAndRotateZ(float radius, double ringAngle, double tiltAngle, double rotAngle) {
		float rx = radius * (float) Math.cos(ringAngle);
		float rz = radius * (float) Math.sin(ringAngle);
		float tiltedZ = (float) (rz * Math.cos(tiltAngle));
		return (float) (rx * Math.sin(rotAngle) + tiltedZ * Math.cos(rotAngle));
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Pulsing Rings — concentric red rings radiating from the tree base
	// ════════════════════════════════════════════════════════════════════════

	private static void drawPulsingRings(PoseStack stack, MultiBufferSource buffer,
			QliphothBloomClientData.BloomEntry bloom, float time, Vec3 cam) {

		BlockPos center = bloom.getCenter();
		double cx = center.getX() + 0.5;
		double cy = center.getY() + 0.08;
		double cz = center.getZ() + 0.5;

		stack.pushPose();
		stack.translate(cx - cam.x, cy - cam.y, cz - cam.z);
		drawRingsInternal(stack, buffer, time);
		stack.popPose();
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Helpers
	// ════════════════════════════════════════════════════════════════════════

	/** Golden ratio conjugate — used for deterministic pseudo-random distribution of geometry. */
	private static final double GOLDEN_RATIO_CONJUGATE = 0.6180339887;

	/** Deterministic pseudo-random 0..1 value for a given index. */
	private static double hashIndex(int index) {
		return ((index * GOLDEN_RATIO_CONJUGATE + 0.3) % 1.0 + 1.0) % 1.0;
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

	private static void emitCore(MultiBufferSource buffer, Matrix4f mat,
			float x1, float y1, float z1, float r1, float g1, float b1, float a1,
			float x2, float y2, float z2, float r2, float g2, float b2, float a2,
			float x3, float y3, float z3, float r3, float g3, float b3, float a3,
			float x4, float y4, float z4, float r4, float g4, float b4, float a4) {
		emitQuad(buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), mat,
				x1, y1, z1, r1, g1, b1, a1,
				x2, y2, z2, r2, g2, b2, a2,
				x3, y3, z3, r3, g3, b3, a3,
				x4, y4, z4, r4, g4, b4, a4);
	}

	private static void emitGlow(MultiBufferSource buffer, Matrix4f mat,
			float x1, float y1, float z1, float r1, float g1, float b1, float a1,
			float x2, float y2, float z2, float r2, float g2, float b2, float a2,
			float x3, float y3, float z3, float r3, float g3, float b3, float a3,
			float x4, float y4, float z4, float r4, float g4, float b4, float a4) {
		emitQuad(buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), mat,
				x1, y1, z1, r1, g1, b1, a1,
				x2, y2, z2, r2, g2, b2, a2,
				x3, y3, z3, r3, g3, b3, a3,
				x4, y4, z4, r4, g4, b4, a4);
	}

	private static final class QliphothBarkCache {
		private static final Map<QliphothBarkMeshKey, VertexBuffer> MESHES = new HashMap<>();

		private QliphothBarkCache() { }

		static void renderTree(PoseStack stack, int stage) {
			draw(stack, mesh(QliphothBarkMeshKey.create(stage, false)));
		}

		static void renderRoots(PoseStack stack, int stage) {
			draw(stack, mesh(QliphothBarkMeshKey.create(stage, true)));
		}

		static void clear() {
			MESHES.values().forEach(VertexBuffer::close);
			MESHES.clear();
		}

		private static VertexBuffer mesh(QliphothBarkMeshKey key) {
			VertexBuffer cached = MESHES.get(key);
			if (cached != null) return cached;
			QliphothBloomGeometry.StageSnapshot snapshot = QliphothBloomGeometry.snapshot(key.stage());
			VertexBuffer built = build(snapshot, key.severed());
			MESHES.put(key, built);
			return built;
		}

		private static VertexBuffer build(QliphothBloomGeometry.StageSnapshot snapshot, boolean rootsOnly) {
			BufferBuilder builder = Tesselator.getInstance().begin(
					VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
			Matrix4f identity = new Matrix4f();
			if (!rootsOnly) {
				drawTexturedLimb(builder, identity, snapshot.trunk(), 10, .40f, .52f, .92f, 2.4f);
			}
			for (QliphothBloomGeometry.Limb root : snapshot.roots()) {
				drawTexturedLimb(builder, identity, root, 8, .35f, .45f, .81f, 2.0f);
			}
			if (!rootsOnly) {
				for (QliphothBloomGeometry.Limb branch : snapshot.mainBranches()) {
					drawTexturedLimb(builder, identity, branch, 8, .34f, .44f, .78f, 2.1f);
				}
				for (QliphothBloomGeometry.Limb branch : snapshot.secondaryBranches()) {
					drawTexturedLimb(builder, identity, branch, 7, .30f, .39f, .70f, 2.1f);
				}
				for (QliphothBloomGeometry.Limb prong : snapshot.crownProngs()) {
					drawTexturedLimb(builder, identity, prong, 7, .30f, .39f, .70f, 2.1f);
				}
			}
			MeshData mesh = builder.build();
			if (mesh == null) return null;
			VertexBuffer result = new VertexBuffer(VertexBuffer.Usage.STATIC);
			result.bind();
			result.upload(mesh);
			VertexBuffer.unbind();
			return result;
		}

		private static void draw(PoseStack stack, VertexBuffer buffer) {
			if (buffer == null) return;
			ShaderInstance shader = GameRenderer.getRendertypeEntityCutoutNoCullShader();
			if (shader == null) return;
			BARK_TYPE.setupRenderState();
			buffer.bind();
			Matrix4f modelView = CachedMeshModelView.compose(
					RenderSystem.getModelViewMatrix(), stack.last().pose());
			buffer.drawWithShader(modelView, RenderSystem.getProjectionMatrix(), shader);
			VertexBuffer.unbind();
			BARK_TYPE.clearRenderState();
		}
	}
}

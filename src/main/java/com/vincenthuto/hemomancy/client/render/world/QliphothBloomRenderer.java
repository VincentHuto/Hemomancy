package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomData;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.QliphothBloomClientData;
import com.vincenthuto.hemomancy.client.render.CachedMeshModelView;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.RenderTypeInit;
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
		List<QliphothBloomData.BloomEntry> blooms = QliphothBloomClientData.getActiveBlooms();
		if (blooms.isEmpty()) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		float currentTime = AnimationTime.gameTime(mc.level, partialTick);
		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

		boolean sceneCopied = false;
		for (QliphothBloomData.BloomEntry bloom : blooms) {
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
			QliphothBloomData.BloomEntry bloom, float time, Vec3 cam, boolean dynamicEffects,
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
		QliphothSeveredGeometry.Stump stump = QliphothSeveredGeometry.stump();
		QliphothBloomGeometry.Limb trunk = QliphothSeveredGeometry.trunk();
		for (int index = 0; index < 3; index++) {
			drawVeinPath(buffer, mat, QliphothSeveredGeometry.clipBelowCut(
					QliphothBloomGeometry.surfaceVein(trunk, index), .05), trunk.seed(), index, time);
		}

		// Blood weeping out over the rind, brightest at the monolith edge.
		QliphothBloomGeometry.Point lift = QliphothSeveredGeometry.cutNormal().scale(.006);
		float seam = (float) ((open ? .30 : .15) + .10 * pulse);
		float red = (float) (.62 + .30 * pulse);
		QliphothBloomGeometry.Point[] rim = stump.rim(), inner = stump.inner();
		for (int side = 0; side < rim.length; side++) {
			int next = (side + 1) % rim.length;
			QliphothBloomGeometry.Point i0 = inner[side].add(lift), i1 = inner[next].add(lift);
			QliphothBloomGeometry.Point r0 = rim[side].add(lift), r1 = rim[next].add(lift);
			emitGlow(buffer, mat,
					(float) i0.x(), (float) i0.y(), (float) i0.z(), red, .010f, .016f, seam,
					(float) i1.x(), (float) i1.y(), (float) i1.z(), red, .010f, .016f, seam,
					(float) r1.x(), (float) r1.y(), (float) r1.z(), red, .010f, .016f, 0f,
					(float) r0.x(), (float) r0.y(), (float) r0.z(), red, .010f, .016f, 0f);
		}

		float shaderTime = time / 20.0F;
		float burden = open ? 0.34F : 0.12F;
		float attuned = open ? 0.96F : 0.84F;
		RenderType capType = HemoRenderTypes.monolithFragmentRigid(shaderTime, seed, burden, attuned, 0.0F);
		VertexConsumer cap = buffer.getBuffer(capType);
		for (int side = 0; side < inner.length; side++) {
			emitMonolithVertex(cap, mat, stump, stump.center());
			emitMonolithVertex(cap, mat, stump, inner[(side + 1) % inner.length]);
			emitMonolithVertex(cap, mat, stump, inner[side]);
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
			drawVeinPath(buffer, mat, QliphothBloomGeometry.surfaceVein(wood, index), wood.seed(), index, time);
		}
	}

	private static void drawVeinPath(MultiBufferSource buffer, Matrix4f mat,
			List<QliphothBloomGeometry.Point> path, int seed, int index, float time) {
		if (path.size() < 2) return;
		float traveling = (float) ((Math.sin(time * .11 - index * 1.7 + seed * .17) + 1) * .5);
		QliphothBloomGeometry.Limb vein = new QliphothBloomGeometry.Limb(path,
				path.stream().map(point -> .018 + traveling * .009).toList(), seed + index);
		drawLimbGlow(buffer, mat, vein, 4, .60f + traveling * .35f, .006f, .012f, .58f);
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

	/** Bark wall of the pruned trunk plus the end-grain rind ringing its monolith face. */
	private static void drawSeveredWood(VertexConsumer out, Matrix4f mat, QliphothSeveredGeometry.Stump stump,
			float red, float green, float blue) {
		QliphothBloomGeometry.Point[][] wall = stump.wall();
		QliphothBloomGeometry.Point[][] normals = stump.normals();
		double[][] v = stump.wallV();
		int sides = stump.rim().length;
		for (int row = 0; row < wall.length - 1; row++) {
			for (int side = 0; side < sides; side++) {
				int next = (side + 1) % sides;
				float u0 = side / (float) sides, u1 = (side + 1) / (float) sides;
				texturedVertex(out, mat, wall[row][side], normals[row][side], u0, (float) v[row][side], red, green, blue);
				texturedVertex(out, mat, wall[row + 1][side], normals[row + 1][side], u0, (float) v[row + 1][side], red, green, blue);
				texturedVertex(out, mat, wall[row + 1][next], normals[row + 1][next], u1, (float) v[row + 1][next], red, green, blue);
				texturedVertex(out, mat, wall[row][next], normals[row][next], u1, (float) v[row][next], red, green, blue);
			}
		}
		QliphothBloomGeometry.Point up = QliphothSeveredGeometry.cutNormal();
		float rind = (float) (QliphothSeveredGeometry.RIND * QliphothSeveredGeometry.BARK_REPEATS);
		float shade = .72f;
		for (int side = 0; side < sides; side++) {
			int next = (side + 1) % sides;
			float u0 = side / (float) sides, u1 = (side + 1) / (float) sides;
			texturedVertex(out, mat, stump.rim()[side], up, u0, 0, red * shade, green * shade, blue * shade);
			texturedVertex(out, mat, stump.inner()[side], up, u0, rind, red * shade, green * shade, blue * shade);
			texturedVertex(out, mat, stump.inner()[next], up, u1, rind, red * shade, green * shade, blue * shade);
			texturedVertex(out, mat, stump.rim()[next], up, u1, 0, red * shade, green * shade, blue * shade);
		}
	}

	private static void texturedVertex(VertexConsumer out, Matrix4f mat, QliphothBloomGeometry.Point point,
			QliphothBloomGeometry.Point normal, float u, float v, float red, float green, float blue) {
		out.addVertex(mat, (float)point.x(), (float)point.y(), (float)point.z())
				.setColor(red, green, blue, 1f).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(LightTexture.FULL_BRIGHT).setNormal((float)normal.x(), (float)normal.y(), (float)normal.z());
	}

	private static QliphothBloomGeometry.Point[][] tubeFrames(List<QliphothBloomGeometry.Point> points) {
		return QliphothBloomGeometry.tubeFrames(points);
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
		return QliphothBloomGeometry.ringPoint(center, n, b, radius, angle);
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

	private static void emitMonolithVertex(VertexConsumer consumer, Matrix4f mat,
			QliphothSeveredGeometry.Stump stump, QliphothBloomGeometry.Point point) {
		consumer.addVertex(mat, (float) point.x(), (float) point.y(), (float) point.z())
				.setColor(2, 0, 0, 255)
				.setUv(stump.faceU(point), stump.faceV(point))
				.setLight(LightTexture.FULL_BRIGHT);
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Apex Orb — dark "black hole" sphere at the very tip of the tree
	// ════════════════════════════════════════════════════════════════════════

	// ════════════════════════════════════════════════════════════════════════
	//  Pulsing Rings — concentric red rings radiating from the tree base
	// ════════════════════════════════════════════════════════════════════════

	private static void drawPulsingRings(PoseStack stack, MultiBufferSource buffer,
			QliphothBloomData.BloomEntry bloom, float time, Vec3 cam) {

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

		private static VertexBuffer build(QliphothBloomGeometry.StageSnapshot snapshot, boolean severed) {
			BufferBuilder builder = Tesselator.getInstance().begin(
					VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
			Matrix4f identity = new Matrix4f();
			if (severed) {
				drawSeveredWood(builder, identity, QliphothSeveredGeometry.stump(), .40f, .52f, .92f);
			} else {
				drawTexturedLimb(builder, identity, snapshot.trunk(), 10, .40f, .52f, .92f, 2.4f);
			}
			for (QliphothBloomGeometry.Limb root : snapshot.roots()) {
				drawTexturedLimb(builder, identity, root, 8, .35f, .45f, .81f, 2.0f);
			}
			if (!severed) {
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

package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteBoundaryProgress;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilOrganicGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Renders a living, undulating glowing red boundary circle for active cardinal
 * rites. The ring breathes — its radius wobbles per-segment — and vein-like
 * branches sprout inward, tapering to nothing, giving an organic capillary
 * feel. All geometry is flat POSITION_COLOR quads with proper depth testing.
 */
public class CardinalRiteBoundaryRenderer {

	// ── Ring geometry ──
	private static final int SEGMENTS = 96;
	private static final float CORE_WIDTH = 0.09f;
	private static final float GLOW_WIDTH = 0.22f;

	// ── Undulation parameters ──
	/** How many "bumps" around the circumference at any moment. */
	private static final double UNDULATE_FREQ = 6.0;
	/** Max radial displacement of the undulation (blocks). */
	private static final float UNDULATE_AMP = 0.12f;
	/** How fast the undulation pattern crawls around the ring. */
	private static final double UNDULATE_SPEED = 0.045;
	/** Secondary higher-frequency wobble layered on top. */
	private static final double UNDULATE_FREQ2 = 13.0;
	private static final float UNDULATE_AMP2 = 0.04f;
	private static final double UNDULATE_SPEED2 = 0.09;

	// ── Vein parameters ──
	/** How many vein branches around the ring. */
	private static final int VEIN_COUNT = 12;
	/** Number of quad segments per vein branch. */
	private static final int VEIN_SEGS = 6;
	private static final int SIGIL_VESSEL_SEGMENTS = 7;
	/** Max inward reach of a vein (blocks). */
	private static final float VEIN_LENGTH = 0.7f;
	/** Width at root of vein. */
	private static final float VEIN_ROOT_WIDTH = 0.04f;
	/** How fast veins crawl around the ring. */
	private static final double VEIN_CRAWL_SPEED = 0.012;

	public static void render(PoseStack poseStack, float partialTick) {
		List<ActiveRiteClientData.RiteEntry> rites = ActiveRiteClientData.getActiveRites();
		if (rites.isEmpty()) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		float currentTime = mc.level.getGameTime() + partialTick;
		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

		// Enclose the live ritual space in the established black/red Fane
		// material. From inside the circle this shades the world beyond the
		// outermost completed ring while leaving the ritual interior clear.
		for (ActiveRiteClientData.RiteEntry rite : rites) {
			if (rite.isUnstained()) continue;
			drawExteriorField(poseStack, buffer, rite, currentTime, cam);
		}

		// Two separate passes so only one non-fixed render type is active at a time.
		// NeoForge 1.21.1's sequential BufferSource flushes the current type when a
		// second different type is requested, leaving the first VertexConsumer dead.
		VertexConsumer glowVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);
		for (ActiveRiteClientData.RiteEntry rite : rites) {
			if (rite.isUnstained()) continue;
			drawBoundaryRing(poseStack, glowVC, null, rite, currentTime, cam);
			drawSigilSegments(poseStack, glowVC, rite, currentTime, cam, true);
			drawSanguineBlobs(poseStack, glowVC, rite, currentTime, cam, true);
		}
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_GLOW);

		VertexConsumer coreVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
		for (ActiveRiteClientData.RiteEntry rite : rites) {
			if (rite.isUnstained()) continue;
			drawBoundaryRing(poseStack, null, coreVC, rite, currentTime, cam);
			drawSigilSegments(poseStack, coreVC, rite, currentTime, cam, false);
			drawSanguineBlobs(poseStack, coreVC, rite, currentTime, cam, false);
		}
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_CORE);
	}

	private static void drawExteriorField(PoseStack poseStack, MultiBufferSource.BufferSource buffer,
			ActiveRiteClientData.RiteEntry rite, float currentTime, Vec3 cam) {
		if (!CardinalRiteBoundaryGeometry.shouldRenderExterior(rite.getTotalRings())) return;
		boolean legacy = "LEGACY".equals(rite.getPhase());
		float radius = rite.getFootprintRadius() > 0.0F
				? rite.getFootprintRadius()
				: CardinalRiteBoundaryGeometry.exteriorRadius(
						rite.getRiteSize(), rite.getCompletedRings(), legacy);
		if (radius <= 0.0F) return;
		float seed = FaneBoundaryRenderer.revealedFaneStyleSeed(rite.getCenter(), radius);
		FaneBoundaryRenderer.drawRevealedFaneStyleDome(
				poseStack, buffer, cam, rite.getCenter(), radius, currentTime, seed);
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Main ring + veins
	// ════════════════════════════════════════════════════════════════════════

	/**
	 * Draws one rite's boundary ring for a single pass.
	 * Pass {@code glowVC} non-null and {@code coreVC} null for the glow pass,
	 * or {@code glowVC} null and {@code coreVC} non-null for the core pass.
	 */
	private static void drawBoundaryRing(PoseStack stack, VertexConsumer glowVC, VertexConsumer coreVC,
			ActiveRiteClientData.RiteEntry rite, float currentTime, Vec3 cam) {

		BlockPos center = rite.getCenter();
		float baseRadius = (float) (rite.getRiteSize() / 2.0 + 1.0);
		double cx = center.getX() + 0.5;
		double cy = CardinalRiteBoundaryGeometry.boundaryPlaneY(center.getY());
		double cz = center.getZ() + 0.5;

		// Derive the rite tier from the size: MINOR=3→1, LESSER=5→2, GREATER=7→3, GRAND=9→4
		boolean legacy = "LEGACY".equals(rite.getPhase());
		int riteTier = (rite.getRiteSize() - 1) / 2;
		int ringCount = legacy ? Math.max(1, riteTier) : rite.getTotalRings();
		if (ringCount <= 0) return;

		// Global breathing pulse (0..1)
		double pulse = (Math.sin(currentTime * 0.08) + 1.0) * 0.5;

		stack.pushPose();
		stack.translate(cx - cam.x, cy - cam.y, cz - cam.z);
		Matrix4f mat = stack.last().pose();

		for (int ring = 0; ring < ringCount; ring++) {
			final int activeRing = ring;
			List<CardinalRiteBoundaryProgress.Segment> visibleArcs = legacy
					? List.of(new CardinalRiteBoundaryProgress.Segment(ring, 0.0D, Math.PI * 2.0D))
					: rite.getBoundarySegments().stream()
							.filter(segment -> segment.ring() == activeRing)
							.toList();
			if (visibleArcs.isEmpty()) continue;
			float ringRadius = legacy ? baseRadius + ring * 2.0f
					: CardinalRiteBoundaryGeometry.interactiveRingRadius(ring);
			// Alternate rotation direction: even rings go forward, odd rings reverse
			float directionSign = (ring % 2 == 0) ? 1.0f : -1.0f;
			// Outer rings become progressively more transparent
			float ringFade = 1.0f - ring * 0.15f;
			// Outer rings shift from dark blood-red toward a lighter, brighter red
			// lighten goes 0.0 (innermost, darkest) → ~0.6 (outermost, lightest)
			float lighten = (ringCount > 1) ? (float) ring / (ringCount - 1) * 0.6f : 0f;

			float coreAlpha = (float) (0.65 + 0.35 * pulse) * ringFade;
			float glowAlpha = (float) (0.15 + 0.15 * pulse) * ringFade;

			float coreR = Math.min(1.0f, (float) (0.85 + 0.15 * pulse) + lighten * 0.15f);
			float coreG = 0.05f + lighten * 0.20f;
			float coreB = 0.04f + lighten * 0.15f;
			float glowR = Math.min(1.0f, (float) (0.55 + 0.2 * pulse) + lighten * 0.25f);
			float glowG = 0.02f + lighten * 0.12f;
			float glowB = 0.02f + lighten * 0.10f;

			// ── Draw the undulating ring ──
			for (int i = 0; i < SEGMENTS; i++) {
				double a1 = Math.toRadians((360.0 / SEGMENTS) * i);
				double a2 = Math.toRadians((360.0 / SEGMENTS) * (i + 1));
				float arcIntegrity = legacy ? 1.0F
						: arcIntegrity(visibleArcs, (a1 + a2) * 0.5D, currentTime);
				if (arcIntegrity <= 0.01F) continue;
				float segmentCoreAlpha = coreAlpha * arcIntegrity;
				float segmentGlowAlpha = glowAlpha * arcIntegrity;

				float r1 = ringRadius + undulation(a1, currentTime, directionSign);
				float r2 = ringRadius + undulation(a2, currentTime, directionSign);

				// Subtle Y wave so it's not perfectly flat
				float y1 = (float) (Math.sin(a1 * 5.0 + currentTime * 0.06 * directionSign) * 0.012);
				float y2 = (float) (Math.sin(a2 * 5.0 + currentTime * 0.06 * directionSign) * 0.012);

				float cos1 = (float) Math.cos(a1);
				float sin1 = (float) Math.sin(a1);
				float cos2 = (float) Math.cos(a2);
				float sin2 = (float) Math.sin(a2);

				float iGlow1 = r1 - GLOW_WIDTH - CORE_WIDTH * 0.5f;
				float iCore1 = r1 - CORE_WIDTH * 0.5f;
				float oCore1 = r1 + CORE_WIDTH * 0.5f;
				float oGlow1 = r1 + GLOW_WIDTH + CORE_WIDTH * 0.5f;

				float iGlow2 = r2 - GLOW_WIDTH - CORE_WIDTH * 0.5f;
				float iCore2 = r2 - CORE_WIDTH * 0.5f;
				float oCore2 = r2 + CORE_WIDTH * 0.5f;
				float oGlow2 = r2 + GLOW_WIDTH + CORE_WIDTH * 0.5f;

				if (glowVC != null) {
					// Inner glow
					emitQuad(glowVC, mat,
							cos1 * iGlow1, y1, sin1 * iGlow1, glowR, glowG, glowB, 0f,
							cos1 * iCore1, y1, sin1 * iCore1, glowR, glowG, glowB, segmentGlowAlpha,
							cos2 * iCore2, y2, sin2 * iCore2, glowR, glowG, glowB, segmentGlowAlpha,
							cos2 * iGlow2, y2, sin2 * iGlow2, glowR, glowG, glowB, 0f);

					// Outer glow
					emitQuad(glowVC, mat,
							cos1 * oCore1, y1, sin1 * oCore1, glowR, glowG, glowB, segmentGlowAlpha,
							cos1 * oGlow1, y1, sin1 * oGlow1, glowR, glowG, glowB, 0f,
							cos2 * oGlow2, y2, sin2 * oGlow2, glowR, glowG, glowB, 0f,
							cos2 * oCore2, y2, sin2 * oCore2, glowR, glowG, glowB, segmentGlowAlpha);
				}

				if (coreVC != null) {
					// Core
					emitQuad(coreVC, mat,
							cos1 * iCore1, y1, sin1 * iCore1, coreR, coreG, coreB, segmentCoreAlpha,
							cos1 * oCore1, y1, sin1 * oCore1, coreR, coreG, coreB, segmentCoreAlpha,
							cos2 * oCore2, y2, sin2 * oCore2, coreR, coreG, coreB, segmentCoreAlpha,
							cos2 * iCore2, y2, sin2 * iCore2, coreR, coreG, coreB, segmentCoreAlpha);
				}
			}

			// ── Draw vein branches sprouting inward from each ring ──
			if (legacy || visibleArcs.stream().allMatch(segment -> segment.integrity() >= 0.999F)) {
				drawVeins(glowVC, coreVC, mat, ringRadius, currentTime, directionSign,
						coreR, coreG, coreB, coreAlpha, glowR, glowG, glowB);
			}
		}

		stack.popPose();
	}

	private static void drawSigilSegments(PoseStack stack, VertexConsumer consumer,
			ActiveRiteClientData.RiteEntry rite, float currentTime, Vec3 cam, boolean glow) {
		if (rite.getSigilSegments().isEmpty()) return;
		float pulse = (float) ((Math.sin(currentTime * 0.12D) + 1.0D) * 0.5D);
		float halfWidth = glow ? 0.18F : 0.055F;
		float alpha = glow ? 0.20F + pulse * 0.16F : 0.72F + pulse * 0.24F;
		Matrix4f matrix = stack.last().pose();
		for (ActiveRiteClientData.SigilSegment segment : rite.getSigilSegments()) {
			float red = ((segment.color() >> 16) & 255) / 255.0F;
			float green = ((segment.color() >> 8) & 255) / 255.0F;
			float blue = (segment.color() & 255) / 255.0F;
			red = Math.min(1.0F, red * 0.62F + 0.30F);
			green *= 0.55F;
			blue *= 0.55F;
			long seed = Double.doubleToLongBits(segment.startX())
					^ Long.rotateLeft(Double.doubleToLongBits(segment.startZ()), 17)
					^ Long.rotateLeft(Double.doubleToLongBits(segment.endX()), 31)
					^ segment.color();
			IchorianSigilOrganicGeometry.Sample previous = IchorianSigilOrganicGeometry.sample(
					segment.startX(), segment.startY(), segment.startZ(),
					segment.endX(), segment.endY(), segment.endZ(),
					currentTime, seed, 0, SIGIL_VESSEL_SEGMENTS, halfWidth);
			for (int step = 1; step <= SIGIL_VESSEL_SEGMENTS; step++) {
				IchorianSigilOrganicGeometry.Sample next = IchorianSigilOrganicGeometry.sample(
						segment.startX(), segment.startY(), segment.startZ(),
						segment.endX(), segment.endY(), segment.endZ(),
						currentTime, seed, step, SIGIL_VESSEL_SEGMENTS, halfWidth);
				drawOrganicSigilSection(consumer, matrix, previous, next, cam,
						red, green, blue, alpha, glow);
				previous = next;
			}
		}
	}

	private static void drawOrganicSigilSection(VertexConsumer consumer, Matrix4f matrix,
			IchorianSigilOrganicGeometry.Sample start, IchorianSigilOrganicGeometry.Sample end,
			Vec3 cam, float red, float green, float blue, float alpha, boolean glow) {
		double dx = end.x() - start.x();
		double dz = end.z() - start.z();
		double length = Math.hypot(dx, dz);
		if (length < 0.001D) return;
		float normalX = (float) (-dz / length);
		float normalZ = (float) (dx / length);
		float startX = (float) (start.x() - cam.x);
		float startY = (float) (start.y() - cam.y);
		float startZ = (float) (start.z() - cam.z);
		float endX = (float) (end.x() - cam.x);
		float endY = (float) (end.y() - cam.y);
		float endZ = (float) (end.z() - cam.z);
		emitQuad(consumer, matrix,
				startX - normalX * start.halfWidth(), startY,
				startZ - normalZ * start.halfWidth(), red, green, blue, glow ? 0.0F : alpha,
				startX + normalX * start.halfWidth(), startY,
				startZ + normalZ * start.halfWidth(), red, green, blue, alpha,
				endX + normalX * end.halfWidth(), endY,
				endZ + normalZ * end.halfWidth(), red, green, blue, alpha,
				endX - normalX * end.halfWidth(), endY,
				endZ - normalZ * end.halfWidth(), red, green, blue, glow ? 0.0F : alpha);
	}

	private static void drawSanguineBlobs(PoseStack stack, VertexConsumer consumer,
			ActiveRiteClientData.RiteEntry rite, float currentTime, Vec3 cam, boolean glow) {
		for (ActiveRiteClientData.SanguineBlob blob : rite.getSanguineBlobs()) {
			float pulse = 1.0F + 0.055F * (float) Math.sin(
					currentTime * 0.16F + (blob.seed() & 31L) * 0.31F);
			float radius = blob.renderRadius(currentTime - (float) Math.floor(currentTime))
					* pulse + (glow ? 0.09F : 0.0F);
			float damageFlicker = blob.integrity() <= 0.0F || blob.integrity() >= 1.0F
					? 1.0F
					: 1.0F - (1.0F - blob.integrity())
							* (0.65F + 0.35F * (float) ((Math.sin(
									currentTime * 1.75F + (blob.seed() & 15L)) + 1.0D) * 0.5D));
			float red = ((blob.color() >> 16) & 255) / 255.0F;
			float green = ((blob.color() >> 8) & 255) / 255.0F;
			float blue = (blob.color() & 255) / 255.0F;
			if (glow) {
				red = Math.min(1.0F, red * 1.18F + 0.08F);
				green = Math.min(1.0F, green * 1.18F + 0.02F);
				blue = Math.min(1.0F, blue * 1.18F + 0.02F);
			}
			stack.pushPose();
			stack.translate(blob.x() - cam.x, blob.y() - cam.y, blob.z() - cam.z);
			SanguineFormationProjectionRenderer.renderSphere(
					consumer, stack.last().pose(), radius, currentTime, blob.seed(),
					red, green, blue, (glow ? 0.24F : 0.84F) * damageFlicker);
			stack.popPose();
		}
	}

	private static float arcIntegrity(List<CardinalRiteBoundaryProgress.Segment> arcs,
			double angle, float currentTime) {
		double normalizedAngle = normalizeAngle(angle);
		for (CardinalRiteBoundaryProgress.Segment arc : arcs) {
			double fromStart = normalizeAngle(normalizedAngle - normalizeAngle(arc.startAngle()));
			if (fromStart > arc.sweepAngle()) continue;
			float integrity = Math.max(0.0F, Math.min(1.0F, arc.integrity()));
			if (integrity <= 0.0F || integrity >= 1.0F) return integrity;
			float flicker = (float) ((Math.sin(
					currentTime * 1.65F + arc.startAngle() * 3.0D) + 1.0D) * 0.5D);
			return 1.0F - (1.0F - integrity) * (0.65F + 0.35F * flicker);
		}
		return 0.0F;
	}

	private static double normalizeAngle(double angle) {
		double fullCircle = Math.PI * 2.0D;
		double normalized = angle % fullCircle;
		return normalized < 0.0D ? normalized + fullCircle : normalized;
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Undulation — layered sine waves that make the ring breathe
	// ════════════════════════════════════════════════════════════════════════

	/** Returns radial offset for a given angle at the current time.
	 *  @param directionSign +1 for forward crawl, -1 for reverse crawl */
	private static float undulation(double angleRad, float time, float directionSign) {
		// Primary slow, large wave
		double w1 = Math.sin(angleRad * UNDULATE_FREQ + time * UNDULATE_SPEED * directionSign) * UNDULATE_AMP;
		// Secondary faster, smaller wave for complexity
		double w2 = Math.sin(angleRad * UNDULATE_FREQ2 - time * UNDULATE_SPEED2 * directionSign) * UNDULATE_AMP2;
		// Tertiary ultra-slow global throb (all segments expand/contract together)
		double throb = Math.sin(time * 0.04) * 0.035;
		return (float) (w1 + w2 + throb);
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Vein branches — tapered quads that sprout inward from the ring
	// ════════════════════════════════════════════════════════════════════════

	private static void drawVeins(VertexConsumer glowVC, VertexConsumer coreVC, Matrix4f mat,
			float baseRadius, float time, float directionSign,
			float cR, float cG, float cB, float cA,
			float gR, float gG, float gB) {

		// Veins are evenly spaced but their angular position crawls slowly over time
		double crawlOffset = time * VEIN_CRAWL_SPEED * directionSign;

		for (int v = 0; v < VEIN_COUNT; v++) {
			double baseAngle = crawlOffset + (Math.PI * 2.0 / VEIN_COUNT) * v;

			// Each vein has a slightly different length and curve based on its index
			// Use a deterministic hash-like value from the vein index for variety
			double veinSeed = hashVein(v);
			float length = VEIN_LENGTH * (0.6f + 0.4f * (float) veinSeed);

			// Vein pulsing — individual veins throb at slightly offset phases
			double veinPulse = (Math.sin(time * 0.1 + v * 1.7) + 1.0) * 0.5;
			float veinAlpha = (float) (0.3 + 0.5 * veinPulse);

			// Slight angular wander as it grows inward (gives curve)
			double curvature = Math.sin(veinSeed * 17.3) * 0.35;

			for (int s = 0; s < VEIN_SEGS; s++) {
				float t0 = (float) s / VEIN_SEGS;
				float t1 = (float) (s + 1) / VEIN_SEGS;

				// Radius decreases inward from the ring
				float rad0 = baseRadius - t0 * length + undulation(baseAngle, time, directionSign);
				float rad1 = baseRadius - t1 * length + undulation(baseAngle, time, directionSign);

				// Angular position curves slightly
				double ang0 = baseAngle + curvature * t0 * t0;
				double ang1 = baseAngle + curvature * t1 * t1;

				// Width tapers from root to tip
				float w0 = VEIN_ROOT_WIDTH * (1.0f - t0 * 0.85f);
				float w1 = VEIN_ROOT_WIDTH * (1.0f - t1 * 0.85f);

				// Alpha fades toward the tip
				float a0 = veinAlpha * (1.0f - t0 * 0.6f);
				float a1 = veinAlpha * (1.0f - t1 * 0.9f);

				float cos0 = (float) Math.cos(ang0);
				float sin0 = (float) Math.sin(ang0);
				float cos1 = (float) Math.cos(ang1);
				float sin1 = (float) Math.sin(ang1);

				// Perpendicular direction for width (tangent to the circle)
				float px0 = (float) -Math.sin(ang0);
				float pz0 = (float) Math.cos(ang0);
				float px1 = (float) -Math.sin(ang1);
				float pz1 = (float) Math.cos(ang1);

				if (coreVC != null) {
					// Core vein quad
					emitQuad(coreVC, mat,
							cos0 * rad0 - px0 * w0, 0.001f, sin0 * rad0 - pz0 * w0, cR, cG, cB, a0 * cA,
							cos0 * rad0 + px0 * w0, 0.001f, sin0 * rad0 + pz0 * w0, cR, cG, cB, a0 * cA,
							cos1 * rad1 + px1 * w1, 0.001f, sin1 * rad1 + pz1 * w1, cR, cG, cB, a1 * cA,
							cos1 * rad1 - px1 * w1, 0.001f, sin1 * rad1 - pz1 * w1, cR, cG, cB, a1 * cA);
				}

				if (glowVC != null) {
					// Glow halo around vein (wider, more transparent)
					float gw0 = w0 * 3.0f;
					float gw1 = w1 * 3.0f;
					float ga0 = a0 * 0.35f;
					float ga1 = a1 * 0.25f;

					emitQuad(glowVC, mat,
							cos0 * rad0 - px0 * gw0, 0.001f, sin0 * rad0 - pz0 * gw0, gR, gG, gB, 0f,
							cos0 * rad0 + px0 * gw0, 0.001f, sin0 * rad0 + pz0 * gw0, gR, gG, gB, 0f,
							cos1 * rad1 + px1 * gw1, 0.001f, sin1 * rad1 + pz1 * gw1, gR, gG, gB, 0f,
							cos1 * rad1 - px1 * gw1, 0.001f, sin1 * rad1 - pz1 * gw1, gR, gG, gB, 0f);

					// Inner part of glow (brighter core of the halo)
					emitQuad(glowVC, mat,
							cos0 * rad0 - px0 * gw0, 0.001f, sin0 * rad0 - pz0 * gw0, gR, gG, gB, 0f,
							cos0 * rad0 - px0 * w0, 0.001f, sin0 * rad0 - pz0 * w0, gR, gG, gB, ga0,
							cos1 * rad1 - px1 * w1, 0.001f, sin1 * rad1 - pz1 * w1, gR, gG, gB, ga1,
							cos1 * rad1 - px1 * gw1, 0.001f, sin1 * rad1 - pz1 * gw1, gR, gG, gB, 0f);

					emitQuad(glowVC, mat,
							cos0 * rad0 + px0 * w0, 0.001f, sin0 * rad0 + pz0 * w0, gR, gG, gB, ga0,
							cos0 * rad0 + px0 * gw0, 0.001f, sin0 * rad0 + pz0 * gw0, gR, gG, gB, 0f,
							cos1 * rad1 + px1 * gw1, 0.001f, sin1 * rad1 + pz1 * gw1, gR, gG, gB, 0f,
							cos1 * rad1 + px1 * w1, 0.001f, sin1 * rad1 + pz1 * w1, gR, gG, gB, ga1);
				}
			}
		}
	}

	/** Deterministic pseudo-random 0..1 value for a given vein index. */
	private static double hashVein(int index) {
		// Simple golden-ratio hash
		return (index * 0.6180339887) % 1.0;
	}

	// ════════════════════════════════════════════════════════════════════════
	//  Quad helper
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
}

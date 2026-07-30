package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteBoundaryProgress;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilOrganicGeometry;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilLandmarkGeometry;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRenderPalette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
	private static final int ANCHOR_SOCKET_SEGMENTS = 24;
	private static final float ANCHOR_SOCKET_RADIUS = 0.34F;
	private static final float ANCHOR_SOCKET_CORE_WIDTH = 0.055F;
	private static final float ANCHOR_SOCKET_GLOW_WIDTH = 0.14F;
	private static final float ANCHOR_SOCKET_UNDULATION_SCALE = 0.15F;
	private static final double ANCHOR_SOCKET_GATE_HALF_ANGLE = 0.38D;
	private static final double BOUNDARY_SOCKET_WAVE_FADE_ANGLE = 0.20D;
	private static final double SOCKET_GATE_WAVE_FADE_ANGLE = 0.16D;
	private static final int EFFECT_DISC_SEGMENTS = 12;
	private static final int SOCKET_STAIN_SEGMENTS = 16;
	private static final int STAIN_RADIAL_SEGMENTS = 6;

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

		CardinalRiteFogRenderer.render(poseStack, buffer, rites, currentTime,
				mc.gameRenderer.getMainCamera());
		if (rites.isEmpty()) return;
		playBoundaryCompletionSounds(mc, rites);

		// Two separate passes so only one non-fixed render type is active at a time.
		// NeoForge 1.21.1's sequential BufferSource flushes the current type when a
		// second different type is requested, leaving the first VertexConsumer dead.
		VertexConsumer glowVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);
		for (ActiveRiteClientData.RiteEntry rite : rites) {
			if (rite.isUnstained()) continue;
			drawBoundaryRing(poseStack, glowVC, null, rite, currentTime, partialTick, cam);
			drawSigilSegments(poseStack, glowVC, rite, currentTime, cam, true);
			drawSanguineBlobs(poseStack, glowVC, rite, currentTime, cam, true);
		}
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_GLOW);

		VertexConsumer coreVC = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
		for (ActiveRiteClientData.RiteEntry rite : rites) {
			if (rite.isUnstained()) continue;
			drawBoundaryRing(poseStack, null, coreVC, rite, currentTime, partialTick, cam);
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

	private static void playBoundaryCompletionSounds(Minecraft mc,
			List<ActiveRiteClientData.RiteEntry> rites) {
		if (mc.level == null) return;
		for (ActiveRiteClientData.RiteEntry rite : rites) {
			if (rite.isUnstained() || "LEGACY".equals(rite.getPhase())) continue;
			for (CardinalRiteBoundaryProgress.Segment segment : rite.getBoundarySegments()) {
				if (!rite.consumeBoundaryCompletion(segment)) continue;
				float ringRadius = CardinalRiteBoundaryGeometry.interactiveRingRadius(segment.ring());
				double midpoint = segment.startAngle() + segment.sweepAngle() * 0.5D;
				double x = rite.getCenter().getX() + 0.5D + Math.cos(midpoint) * ringRadius;
				double y = CardinalRiteBoundaryGeometry.boundaryPlaneY(rite.getCenter().getY());
				double z = rite.getCenter().getZ() + 0.5D + Math.sin(midpoint) * ringRadius;
				mc.level.playLocalSound(x, y, z, SoundEvents.WARDEN_HEARTBEAT,
						SoundSource.BLOCKS, 0.26F, 1.45F, false);
				mc.level.playLocalSound(x, y, z, SoundEvents.RESPAWN_ANCHOR_CHARGE,
						SoundSource.BLOCKS, 0.14F, 0.58F, false);
			}
		}
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
			ActiveRiteClientData.RiteEntry rite, float currentTime, float partialTick, Vec3 cam) {

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
		CardinalRiteRecipe recipe = legacy ? null
				: CardinalRiteRecipe.getRiteByLocation(Minecraft.getInstance().level, rite.getRecipeId());
		boolean hasAnchorSockets = recipe != null && recipe.getCeremony() != null;
		float stainOpacity = legacy ? 1.0F
				: CardinalRiteBoundaryGeometry.stainOpacity(
						rite.stainFadeProgress(partialTick));
		stack.pushPose();
		stack.translate(cx - cam.x, cy - cam.y, cz - cam.z);
		Matrix4f mat = stack.last().pose();

		for (int ring = 0; ring < ringCount; ring++) {
			final int activeRing = ring;
			float nominalRingRadius = legacy ? baseRadius + ring * 2.0f
					: CardinalRiteBoundaryGeometry.interactiveRingRadius(ring);
			float ringRadius = !hasAnchorSockets ? nominalRingRadius
					: CardinalRiteBoundaryGeometry.anchorAlignedRingRadius(
							nominalRingRadius,
							recipe.getCeremony().anchors().stream()
									.filter(anchor -> anchor.ring() == activeRing)
									.map(CardinalRiteCeremonyDefinition.Anchor::offset)
									.toList());
			List<CardinalRiteBoundaryProgress.Segment> visibleSegments = legacy
					? List.of()
					: rite.getBoundarySegments().stream()
							.filter(segment -> segment.ring() == activeRing)
							.toList();
			List<Double> socketEndpoints = hasAnchorSockets
					? socketEndpointAngles(recipe.getCeremony(), ring, ringRadius)
					: List.of();
			List<CardinalRiteBoundaryProgress.Segment> visibleArcs = legacy
					? List.of(new CardinalRiteBoundaryProgress.Segment(ring, 0.0D, Math.PI * 2.0D))
					: visibleSegments.stream()
							.flatMap(segment -> {
								SegmentClearances clearances = segmentClearances(
										segment, recipe.getCeremony(), ringRadius);
								return CardinalRiteBoundaryGeometry.animatedSocketArcs(
										segment, rite.boundaryGrowth(segment, partialTick),
										clearances.start(), clearances.end()).stream();
							})
							.toList();
			if (!legacy && glowVC != null) {
				List<CardinalRiteBoundaryProgress.Segment> backingArcs =
						CardinalRiteBoundaryProgress.authoredSegments(
										recipe.getCeremony().anchors()).stream()
								.filter(segment -> segment.ring() == activeRing)
								.toList();
				List<CardinalRiteCeremonyDefinition.Anchor> stainAnchors =
						recipe.getCeremony().anchors().stream()
								.filter(anchor -> anchor.ring() == activeRing)
								.toList();
				drawBoundaryFloorStain(
						glowVC, mat, ringRadius, backingArcs,
						stainAnchors, ring, stainOpacity);
			}
			if (visibleArcs.isEmpty()) continue;
			// Alternate rotation direction: even rings go forward, odd rings reverse
			float directionSign = (ring % 2 == 0) ? 1.0f : -1.0f;
			// Outer rings become progressively more transparent
			float ringFade = 1.0f - ring * 0.15f;
			// Outer rings shift from dark blood-red toward a lighter, brighter red
			// lighten goes 0.0 (innermost, darkest) → ~0.6 (outermost, lightest)
			float lighten = (ringCount > 1) ? (float) ring / (ringCount - 1) * 0.6f : 0f;

			float heartbeat = IchorianSigilOrganicGeometry.heartbeat(currentTime);
			float coreAlpha = (0.76F + (heartbeat - 0.92F) * 0.8F) * ringFade;
			float glowAlpha = (0.12F + (heartbeat - 0.92F) * 0.5F) * ringFade;

			float coreR = 0.34F + lighten * 0.10F;
			float coreG = 0.008F + lighten * 0.018F;
			float coreB = 0.012F + lighten * 0.014F;
			float glowR = 0.72F + lighten * 0.12F;
			float glowG = 0.025F + lighten * 0.04F;
			float glowB = 0.03F + lighten * 0.03F;

			// ── Draw the undulating ring ──
			List<CardinalRiteBoundaryGeometry.AngularArc> boundaryPieces =
					visibleArcs.stream()
							.flatMap(arc -> CardinalRiteBoundaryGeometry.tessellateArc(
									arc.startAngle(), arc.sweepAngle(), SEGMENTS).stream())
							.toList();
			for (CardinalRiteBoundaryGeometry.AngularArc piece : boundaryPieces) {
				double a1 = piece.startAngle();
				double a2 = a1 + piece.sweepAngle();
				float arcIntegrity = legacy ? 1.0F
						: arcIntegrity(visibleArcs, (a1 + a2) * 0.5D, currentTime);
				if (arcIntegrity <= 0.01F) continue;
				float segmentCoreAlpha = coreAlpha * arcIntegrity;
				float segmentGlowAlpha = glowAlpha * arcIntegrity;
				float dry = CardinalRiteBoundaryGeometry.integrityBrightness(arcIntegrity);
				float arterial = CardinalRiteBoundaryGeometry.arterialHighlight(
						(a1 + a2) * 0.5D, currentTime, ring);
				float segmentCoreR = Math.min(1.0F, coreR * dry + arterial * 0.34F * arcIntegrity);
				float segmentCoreG = coreG * dry + arterial * 0.018F;
				float segmentCoreB = coreB * dry + arterial * 0.012F;

				float waveScale1 = legacy ? 1.0F
						: CardinalRiteBoundaryGeometry.endpointWaveScale(
								a1, socketEndpoints,
								BOUNDARY_SOCKET_WAVE_FADE_ANGLE);
				float waveScale2 = legacy ? 1.0F
						: CardinalRiteBoundaryGeometry.endpointWaveScale(
								a2, socketEndpoints,
								BOUNDARY_SOCKET_WAVE_FADE_ANGLE);
				float r1 = ringRadius
						+ undulation(a1, currentTime, directionSign) * waveScale1;
				float r2 = ringRadius
						+ undulation(a2, currentTime, directionSign) * waveScale2;

				// Subtle Y wave so it's not perfectly flat
				float y1 = CardinalRiteBoundaryGeometry.surfaceSafeOffset(
						(float) (Math.sin(a1 * 5.0 + currentTime * 0.06 * directionSign)
								* 0.012) * waveScale1);
				float y2 = CardinalRiteBoundaryGeometry.surfaceSafeOffset(
						(float) (Math.sin(a2 * 5.0 + currentTime * 0.06 * directionSign)
								* 0.012) * waveScale2);

				float cos1 = (float) Math.cos(a1);
				float sin1 = (float) Math.sin(a1);
				float cos2 = (float) Math.cos(a2);
				float sin2 = (float) Math.sin(a2);

				float integrityCoreWidth = CardinalRiteBoundaryGeometry.integrityWidth(
						CORE_WIDTH, arcIntegrity);
				float integrityGlowWidth = CardinalRiteBoundaryGeometry.integrityWidth(
						GLOW_WIDTH, arcIntegrity);
				float iGlow1 = r1 - integrityGlowWidth - integrityCoreWidth * 0.5f;
				float iCore1 = r1 - integrityCoreWidth * 0.5f;
				float oCore1 = r1 + integrityCoreWidth * 0.5f;
				float oGlow1 = r1 + integrityGlowWidth + integrityCoreWidth * 0.5f;

				float iGlow2 = r2 - integrityGlowWidth - integrityCoreWidth * 0.5f;
				float iCore2 = r2 - integrityCoreWidth * 0.5f;
				float oCore2 = r2 + integrityCoreWidth * 0.5f;
				float oGlow2 = r2 + integrityGlowWidth + integrityCoreWidth * 0.5f;

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
							cos1 * iCore1, y1, sin1 * iCore1, segmentCoreR, segmentCoreG, segmentCoreB, segmentCoreAlpha,
							cos1 * oCore1, y1, sin1 * oCore1, segmentCoreR, segmentCoreG, segmentCoreB, segmentCoreAlpha,
							cos2 * oCore2, y2, sin2 * oCore2, segmentCoreR, segmentCoreG, segmentCoreB, segmentCoreAlpha,
							cos2 * iCore2, y2, sin2 * iCore2, segmentCoreR, segmentCoreG, segmentCoreB, segmentCoreAlpha);
				}
			}

			// ── Draw vein branches sprouting inward from each ring ──
			drawVeins(glowVC, coreVC, mat, ringRadius, currentTime, directionSign,
					coreR, coreG, coreB, coreAlpha, glowR, glowG, glowB,
					visibleArcs, legacy);
		}

		if (!legacy) {
			drawBoundaryCompletionEffects(
					glowVC, coreVC, mat, rite, recipe.getCeremony(), partialTick);
		}
		if (hasAnchorSockets) {
			drawAnchorSockets(glowVC, coreVC, mat, rite, recipe.getCeremony(),
					currentTime, partialTick, ringCount, stainOpacity);
		}

		stack.popPose();
	}

	private static void drawBoundaryFloorStain(VertexConsumer consumer, Matrix4f mat,
			float ringRadius, List<CardinalRiteBoundaryProgress.Segment> backingArcs,
			List<CardinalRiteCeremonyDefinition.Anchor> stainAnchors,
			int ring, float stainOpacity) {
		if (backingArcs.isEmpty()) return;
		float halfWidth = CardinalRiteBoundaryGeometry.boundaryStainWidth() * 0.5F;
		float boundaryAlpha =
				CardinalRiteBoundaryGeometry.boundaryStainAlpha(ring) * stainOpacity;
		float stainY = CardinalRiteBoundaryGeometry.boundaryStainSurfaceOffset();
		List<CardinalRiteBoundaryGeometry.AngularArc> stainPieces =
				backingArcs.stream()
						.flatMap(arc -> CardinalRiteBoundaryGeometry.tessellateArc(
								arc.startAngle(), arc.sweepAngle(), SEGMENTS).stream())
						.toList();
		for (CardinalRiteBoundaryGeometry.AngularArc piece : stainPieces) {
			double a1 = piece.startAngle();
			double a2 = a1 + piece.sweepAngle();
			float cos1 = (float) Math.cos(a1);
			float sin1 = (float) Math.sin(a1);
			float cos2 = (float) Math.cos(a2);
			float sin2 = (float) Math.sin(a2);
			float inner = ringRadius - halfWidth;
			float outer = ringRadius + halfWidth;
			for (int radial = 0; radial < STAIN_RADIAL_SEGMENTS; radial++) {
				float t0 = radial / (float) STAIN_RADIAL_SEGMENTS;
				float t1 = (radial + 1) / (float) STAIN_RADIAL_SEGMENTS;
				float radius0 = inner + (outer - inner) * t0;
				float radius1 = inner + (outer - inner) * t1;
				float x10 = cos1 * radius0;
				float z10 = sin1 * radius0;
				float x11 = cos1 * radius1;
				float z11 = sin1 * radius1;
				float x21 = cos2 * radius1;
				float z21 = sin2 * radius1;
				float x20 = cos2 * radius0;
				float z20 = sin2 * radius0;
				float alpha10 = boundaryAlpha * stainRadialProfile(t0)
						* boundaryStainSocketMask(x10, z10, stainAnchors);
				float alpha11 = boundaryAlpha * stainRadialProfile(t1)
						* boundaryStainSocketMask(x11, z11, stainAnchors);
				float alpha21 = boundaryAlpha * stainRadialProfile(t1)
						* boundaryStainSocketMask(x21, z21, stainAnchors);
				float alpha20 = boundaryAlpha * stainRadialProfile(t0)
						* boundaryStainSocketMask(x20, z20, stainAnchors);
				emitQuad(consumer, mat,
						x10, stainY, z10, 0.006F, 0.0F, 0.010F, alpha10,
						x11, stainY, z11, 0.008F, 0.0F, 0.012F, alpha11,
						x21, stainY, z21, 0.008F, 0.0F, 0.012F, alpha21,
						x20, stainY, z20, 0.006F, 0.0F, 0.010F, alpha20);
			}
		}
	}

	private static float stainRadialProfile(float position) {
		return Math.max(0.0F, 1.0F - Math.abs(position * 2.0F - 1.0F));
	}

	private static float boundaryStainSocketMask(
			float x, float z,
			List<CardinalRiteCeremonyDefinition.Anchor> anchors) {
		float mask = 1.0F;
		float featherStart =
				CardinalRiteBoundaryGeometry.socketStainFeatherStartRadius(
						ANCHOR_SOCKET_RADIUS);
		float outerRadius = CardinalRiteBoundaryGeometry.socketStainOuterRadius(
				ANCHOR_SOCKET_RADIUS);
		for (CardinalRiteCeremonyDefinition.Anchor anchor : anchors) {
			mask = Math.min(mask,
					CardinalRiteBoundaryGeometry.boundaryStainSocketMask(
							x, z, anchor.x(), anchor.z(),
							featherStart, outerRadius - featherStart));
		}
		return mask;
	}

	private static void drawBoundaryCompletionEffects(VertexConsumer glowVC, VertexConsumer coreVC,
			Matrix4f mat, ActiveRiteClientData.RiteEntry rite,
			CardinalRiteCeremonyDefinition ceremony, float partialTick) {
		for (CardinalRiteBoundaryProgress.Segment segment : rite.getBoundarySegments()) {
			float effectAge = rite.boundaryEffectAge(segment, partialTick);
			if (effectAge < 0.0F || effectAge > 17.0F) continue;
			float ringRadius = CardinalRiteBoundaryGeometry.interactiveRingRadius(segment.ring());
			SegmentClearances clearances = segmentClearances(
					segment, ceremony, ringRadius);
			List<CardinalRiteBoundaryProgress.Segment> clipped =
					CardinalRiteBoundaryGeometry.animatedSocketArcs(
							segment, 1.0F, clearances.start(), clearances.end());
			if (clipped.isEmpty()) continue;
			CardinalRiteBoundaryProgress.Segment arc = clipped.getFirst();
			double midpoint = arc.startAngle() + arc.sweepAngle() * 0.5D;
			float sealAlpha = CardinalRiteBoundaryGeometry.sealPulseAlpha(effectAge);
			float sealTravel = CardinalRiteBoundaryGeometry.sealTravel(effectAge);
			float midpointX = (float) Math.cos(midpoint) * ringRadius;
			float midpointZ = (float) Math.sin(midpoint) * ringRadius;
			if (sealAlpha > 0.0F) {
				float sealRadius = 0.10F + sealTravel * 0.16F;
				if (glowVC != null) {
					drawEffectRing(glowVC, mat, midpointX, midpointZ, 0.020F,
							sealRadius, 0.085F, 0.94F, 0.035F, 0.025F,
							sealAlpha * 0.44F);
				}
				if (coreVC != null) {
					drawEffectRing(coreVC, mat, midpointX, midpointZ, 0.022F,
							sealRadius, 0.028F, 0.72F, 0.012F, 0.014F,
							sealAlpha * 0.92F);
				}
				double halfSweep = arc.sweepAngle() * 0.5D;
				double firstHead = midpoint - halfSweep * sealTravel;
				double secondHead = midpoint + halfSweep * sealTravel;
				drawBoundaryEffectPair(glowVC, coreVC, mat, ringRadius,
						firstHead, secondHead, 0.065F, sealAlpha * 0.68F,
						0.78F, 0.018F, 0.02F);
			}

			float bolusAlpha = CardinalRiteBoundaryGeometry.bolusAlpha(effectAge);
			if (bolusAlpha > 0.0F) {
				float bolusProgress = CardinalRiteBoundaryGeometry.bolusProgress(effectAge);
				double halfSweep = arc.sweepAngle() * 0.5D;
				double firstBolus = midpoint - halfSweep * bolusProgress;
				double secondBolus = midpoint + halfSweep * bolusProgress;
				drawBoundaryEffectPair(glowVC, coreVC, mat, ringRadius,
						firstBolus, secondBolus, 0.047F, bolusAlpha,
						1.0F, 0.075F, 0.035F);
			}
		}
	}

	private static void drawBoundaryEffectPair(VertexConsumer glowVC, VertexConsumer coreVC,
			Matrix4f mat, float ringRadius, double firstAngle, double secondAngle,
			float coreRadius, float alpha, float red, float green, float blue) {
		for (double angle : new double[] {firstAngle, secondAngle}) {
			float x = (float) Math.cos(angle) * ringRadius;
			float z = (float) Math.sin(angle) * ringRadius;
			if (glowVC != null) {
				drawFlatDisc(glowVC, mat, x, z, 0.024F,
						coreRadius * 3.1F, red, green, blue, alpha * 0.28F);
			}
			if (coreVC != null) {
				drawFlatDisc(coreVC, mat, x, z, 0.026F,
						coreRadius, red, green, blue, alpha * 0.92F);
			}
		}
	}

	private static void drawAnchorSockets(VertexConsumer glowVC, VertexConsumer coreVC, Matrix4f mat,
			ActiveRiteClientData.RiteEntry rite, CardinalRiteCeremonyDefinition ceremony,
			float currentTime, float partialTick, int ringCount, float stainOpacity) {
		BlockPos center = rite.getCenter();
		List<CardinalRiteCeremonyDefinition.Anchor> anchors = ceremony.anchors();
		for (int anchorIndex = 0; anchorIndex < anchors.size(); anchorIndex++) {
			CardinalRiteCeremonyDefinition.Anchor anchor = anchors.get(anchorIndex);
			int ring = Math.max(0, anchor.ring());
			float directionSign = ring % 2 == 0 ? 1.0F : -1.0F;
			float ringFade = 1.0F - ring * 0.15F;
			float lighten = ringCount > 1
					? (float) ring / (ringCount - 1) * 0.6F
					: 0.0F;
			float integrity = anchorIntegrity(rite, anchorIndex);
			float dry = CardinalRiteBoundaryGeometry.integrityBrightness(integrity);
			float visibility = 0.24F + integrity * 0.76F;
			double worldX = center.getX() + anchor.x() + 0.5D;
			double worldZ = center.getZ() + anchor.z() + 0.5D;
			float blobRadius = boundaryBlobRadius(rite, worldX, worldZ, partialTick);
			float fill = CardinalRiteBoundaryGeometry.socketFill(blobRadius);
			float intensity = CardinalRiteBoundaryGeometry.socketIntensity(blobRadius);
			float heartbeat = IchorianSigilOrganicGeometry.heartbeat(currentTime);
			float coreAlpha = (0.76F + (heartbeat - 0.92F) * 0.8F)
					* ringFade * intensity * visibility;
			float glowAlpha = (0.12F + (heartbeat - 0.92F) * 0.5F)
					* ringFade * intensity * visibility;
			float coreR = (0.34F + lighten * 0.10F) * dry;
			float coreG = (0.008F + lighten * 0.018F) * dry;
			float coreB = (0.012F + lighten * 0.014F) * dry;
			float glowR = (0.72F + lighten * 0.12F) * dry;
			float glowG = (0.025F + lighten * 0.04F) * dry;
			float glowB = (0.03F + lighten * 0.03F) * dry;
			CardinalRiteBoundaryGeometry.SocketDistortion centerDistortion =
					CardinalRiteBoundaryGeometry.socketDistortion(
							0.0D, currentTime, anchorIndex, integrity);
			float localX = anchor.x() + centerDistortion.offsetX();
			float localZ = anchor.z() + centerDistortion.offsetZ();
			if (glowVC != null) {
				drawSocketStain(glowVC, mat, localX, localZ, intensity, stainOpacity);
			}

			List<CardinalRiteBoundaryGeometry.AngularArc> socketPieces =
					CardinalRiteBoundaryGeometry.socketOverlayArcs().stream()
							.flatMap(arc -> CardinalRiteBoundaryGeometry.tessellateArc(
									arc.startAngle(), arc.sweepAngle(),
									ANCHOR_SOCKET_SEGMENTS).stream())
							.toList();
			for (CardinalRiteBoundaryGeometry.AngularArc piece : socketPieces) {
				double a1 = piece.startAngle();
				double a2 = a1 + piece.sweepAngle();
				double phaseOffset = anchorIndex * 0.73D;
				CardinalRiteBoundaryGeometry.SocketDistortion distortion1 =
						CardinalRiteBoundaryGeometry.socketDistortion(
								a1, currentTime, anchorIndex, integrity);
				CardinalRiteBoundaryGeometry.SocketDistortion distortion2 =
						CardinalRiteBoundaryGeometry.socketDistortion(
								a2, currentTime, anchorIndex, integrity);
				float waveScale1 = 1.0F;
				float waveScale2 = 1.0F;
				float distortionScale1 = 1.0F
						+ (distortion1.radialScale() - 1.0F) * waveScale1;
				float distortionScale2 = 1.0F
						+ (distortion2.radialScale() - 1.0F) * waveScale2;
				float r1 = (ANCHOR_SOCKET_RADIUS + undulation(
						a1 + phaseOffset, currentTime, directionSign)
						* ANCHOR_SOCKET_UNDULATION_SCALE * waveScale1)
						* distortionScale1;
				float r2 = (ANCHOR_SOCKET_RADIUS + undulation(
						a2 + phaseOffset, currentTime, directionSign)
						* ANCHOR_SOCKET_UNDULATION_SCALE * waveScale2)
						* distortionScale2;
				float y1 = CardinalRiteBoundaryGeometry.surfaceSafeOffset(
						(float) Math.sin(a1 * 5.0D
								+ currentTime * 0.06D * directionSign + phaseOffset)
								* 0.006F * waveScale1);
				float y2 = CardinalRiteBoundaryGeometry.surfaceSafeOffset(
						(float) Math.sin(a2 * 5.0D
								+ currentTime * 0.06D * directionSign + phaseOffset)
								* 0.006F * waveScale2);
				float cos1 = (float) Math.cos(a1);
				float sin1 = (float) Math.sin(a1);
				float cos2 = (float) Math.cos(a2);
				float sin2 = (float) Math.sin(a2);
				float coreWidth = CardinalRiteBoundaryGeometry.integrityWidth(
						ANCHOR_SOCKET_CORE_WIDTH, integrity);
				float glowWidth = CardinalRiteBoundaryGeometry.integrityWidth(
						ANCHOR_SOCKET_GLOW_WIDTH, integrity);
				float iGlow1 = r1 - glowWidth - coreWidth * 0.5F;
				float iCore1 = r1 - coreWidth * 0.5F;
				float oCore1 = r1 + coreWidth * 0.5F;
				float oGlow1 = r1 + glowWidth + coreWidth * 0.5F;
				float iGlow2 = r2 - glowWidth - coreWidth * 0.5F;
				float iCore2 = r2 - coreWidth * 0.5F;
				float oCore2 = r2 + coreWidth * 0.5F;
				float oGlow2 = r2 + glowWidth + coreWidth * 0.5F;
				float arterial = CardinalRiteBoundaryGeometry.arterialHighlight(
						(a1 + a2) * 0.5D + phaseOffset, currentTime, ring);
				float highlightedCoreR = Math.min(1.0F,
						coreR + arterial * 0.34F * integrity);
				float highlightedCoreG = coreG + arterial * 0.018F;
				float highlightedCoreB = coreB + arterial * 0.012F;

				if (glowVC != null) {
					emitQuad(glowVC, mat,
							localX + cos1 * iGlow1, y1, localZ + sin1 * iGlow1,
							glowR, glowG, glowB, 0.0F,
							localX + cos1 * iCore1, y1, localZ + sin1 * iCore1,
							glowR, glowG, glowB, glowAlpha,
							localX + cos2 * iCore2, y2, localZ + sin2 * iCore2,
							glowR, glowG, glowB, glowAlpha,
							localX + cos2 * iGlow2, y2, localZ + sin2 * iGlow2,
							glowR, glowG, glowB, 0.0F);
					emitQuad(glowVC, mat,
							localX + cos1 * oCore1, y1, localZ + sin1 * oCore1,
							glowR, glowG, glowB, glowAlpha,
							localX + cos1 * oGlow1, y1, localZ + sin1 * oGlow1,
							glowR, glowG, glowB, 0.0F,
							localX + cos2 * oGlow2, y2, localZ + sin2 * oGlow2,
							glowR, glowG, glowB, 0.0F,
							localX + cos2 * oCore2, y2, localZ + sin2 * oCore2,
							glowR, glowG, glowB, glowAlpha);
				}
				if (coreVC != null) {
					emitQuad(coreVC, mat,
							localX + cos1 * iCore1, y1, localZ + sin1 * iCore1,
							highlightedCoreR, highlightedCoreG, highlightedCoreB, coreAlpha,
							localX + cos1 * oCore1, y1, localZ + sin1 * oCore1,
							highlightedCoreR, highlightedCoreG, highlightedCoreB, coreAlpha,
							localX + cos2 * oCore2, y2, localZ + sin2 * oCore2,
							highlightedCoreR, highlightedCoreG, highlightedCoreB, coreAlpha,
							localX + cos2 * iCore2, y2, localZ + sin2 * iCore2,
							highlightedCoreR, highlightedCoreG, highlightedCoreB, coreAlpha);
				}
			}
			drawOrbitingBlood(glowVC, coreVC, mat, localX, localZ,
					currentTime, anchorIndex, fill, directionSign);
		}
	}

	private static void drawSocketNecks(
			VertexConsumer glowVC, VertexConsumer coreVC, Matrix4f mat,
			double anchorAngle, float socketCenterRadius,
			int ring, float socketX, float socketZ,
			float currentTime, float directionSign, int anchorIndex, float integrity,
			float coreR, float coreG, float coreB, float coreAlpha,
			float glowR, float glowG, float glowB, float glowAlpha) {
		float ringRadius = CardinalRiteBoundaryGeometry.interactiveRingRadius(ring);
		float throatRadius = CardinalRiteBoundaryGeometry.socketThroatRadius(
				ANCHOR_SOCKET_RADIUS, ANCHOR_SOCKET_CORE_WIDTH);
		float authoredSocketX = (float) Math.cos(anchorAngle) * socketCenterRadius;
		float authoredSocketZ = (float) Math.sin(anchorAngle) * socketCenterRadius;
		CardinalRiteBoundaryGeometry.SocketJunction firstJunction =
				CardinalRiteBoundaryGeometry.socketJunction(
						ringRadius, authoredSocketX, authoredSocketZ,
						throatRadius, -1);
		CardinalRiteBoundaryGeometry.SocketJunction secondJunction =
				CardinalRiteBoundaryGeometry.socketJunction(
						ringRadius, authoredSocketX, authoredSocketZ,
						throatRadius, 1);
		double firstGateAngle = Math.atan2(
				firstJunction.z() - socketZ, firstJunction.x() - socketX);
		double secondGateAngle = Math.atan2(
				secondJunction.z() - socketZ, secondJunction.x() - socketX);
		for (CardinalRiteBoundaryGeometry.SocketJunction junction
				: new CardinalRiteBoundaryGeometry.SocketJunction[] {
						firstJunction, secondJunction}) {
			double boundaryAngle = junction.boundaryAngle();
			double gateCenter = junction == firstJunction
					? firstGateAngle : secondGateAngle;
			float boundaryRadius = ringRadius;
			float throatY = CardinalRiteBoundaryGeometry.surfaceSafeOffset(0.0F);
			float radialX = (float) Math.cos(boundaryAngle);
			float radialZ = (float) Math.sin(boundaryAngle);
			float throatX = radialX * boundaryRadius;
			float throatZ = radialZ * boundaryRadius;
			float boundaryHalfWidth = CardinalRiteBoundaryGeometry.integrityWidth(
					CORE_WIDTH, integrity) * 0.5F;
			float throatInnerX = radialX * (boundaryRadius - boundaryHalfWidth);
			float throatInnerZ = radialZ * (boundaryRadius - boundaryHalfWidth);
			float throatOuterX = radialX * (boundaryRadius + boundaryHalfWidth);
			float throatOuterZ = radialZ * (boundaryRadius + boundaryHalfWidth);

			double gateMinus = gateCenter - ANCHOR_SOCKET_GATE_HALF_ANGLE;
			double gatePlus = gateCenter + ANCHOR_SOCKET_GATE_HALF_ANGLE;
			SocketBandEdge minus = socketBandEdge(
					socketX, socketZ, gateMinus, currentTime, directionSign,
					anchorIndex, integrity, firstGateAngle, secondGateAngle);
			SocketBandEdge plus = socketBandEdge(
					socketX, socketZ, gatePlus, currentTime, directionSign,
					anchorIndex, integrity, firstGateAngle, secondGateAngle);
			boolean minusIsOuter = Math.cos(gateMinus - anchorAngle)
					> Math.cos(gatePlus - anchorAngle);
			SocketBandEdge outerBranch = minusIsOuter ? minus : plus;
			SocketBandEdge innerBranch = minusIsOuter ? plus : minus;

			if (glowVC != null) {
				emitSocketNeckPass(glowVC, mat,
						throatX, throatZ, throatInnerX, throatInnerZ,
						throatOuterX, throatOuterZ, throatY,
						outerBranch, innerBranch,
						glowR, glowG, glowB, glowAlpha * 0.72F);
			}
			if (coreVC != null) {
				emitSocketNeckPass(coreVC, mat,
						throatX, throatZ, throatInnerX, throatInnerZ,
						throatOuterX, throatOuterZ, throatY,
						outerBranch, innerBranch,
						coreR, coreG, coreB, coreAlpha);
			}
		}
	}

	private static void emitSocketNeckPass(
			VertexConsumer consumer, Matrix4f mat,
			float throatX, float throatZ,
			float throatInnerX, float throatInnerZ,
			float throatOuterX, float throatOuterZ, float throatY,
			SocketBandEdge outerBranch, SocketBandEdge innerBranch,
			float red, float green, float blue, float alpha) {
		emitQuad(consumer, mat,
				throatX, throatY, throatZ, red, green, blue, alpha,
				throatOuterX, throatY, throatOuterZ, red, green, blue, alpha,
				outerBranch.outerX(), outerBranch.y(), outerBranch.outerZ(),
				red, green, blue, alpha,
				outerBranch.innerX(), outerBranch.y(), outerBranch.innerZ(),
				red, green, blue, alpha);
		emitQuad(consumer, mat,
				throatInnerX, throatY, throatInnerZ, red, green, blue, alpha,
				throatX, throatY, throatZ, red, green, blue, alpha,
				innerBranch.innerX(), innerBranch.y(), innerBranch.innerZ(),
				red, green, blue, alpha,
				innerBranch.outerX(), innerBranch.y(), innerBranch.outerZ(),
				red, green, blue, alpha);
	}

	private static SocketBandEdge socketBandEdge(
			float socketX, float socketZ, double angle,
			float currentTime, float directionSign, int anchorIndex, float integrity,
			double firstGateAngle, double secondGateAngle) {
		double phaseOffset = anchorIndex * 0.73D;
		CardinalRiteBoundaryGeometry.SocketDistortion distortion =
				CardinalRiteBoundaryGeometry.socketDistortion(
						angle, currentTime, anchorIndex, integrity);
		float waveScale = CardinalRiteBoundaryGeometry.socketGateWaveScale(
				angle, firstGateAngle, secondGateAngle,
				ANCHOR_SOCKET_GATE_HALF_ANGLE,
				SOCKET_GATE_WAVE_FADE_ANGLE);
		float distortionScale = 1.0F
				+ (distortion.radialScale() - 1.0F) * waveScale;
		float radius = (ANCHOR_SOCKET_RADIUS + undulation(
				angle + phaseOffset, currentTime, directionSign)
				* ANCHOR_SOCKET_UNDULATION_SCALE * waveScale) * distortionScale;
		float halfWidth = CardinalRiteBoundaryGeometry.integrityWidth(
				ANCHOR_SOCKET_CORE_WIDTH, integrity) * 0.5F;
		float y = CardinalRiteBoundaryGeometry.surfaceSafeOffset(
				(float) Math.sin(angle * 5.0D
						+ currentTime * 0.06D * directionSign + phaseOffset)
						* 0.006F * waveScale);
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);
		return new SocketBandEdge(
				socketX + cos * (radius - halfWidth),
				socketZ + sin * (radius - halfWidth),
				socketX + cos * (radius + halfWidth),
				socketZ + sin * (radius + halfWidth),
				y);
	}

	private record SocketBandEdge(
			float innerX, float innerZ, float outerX, float outerZ, float y) {
	}

	private static SegmentClearances segmentClearances(
			CardinalRiteBoundaryProgress.Segment segment,
			CardinalRiteCeremonyDefinition ceremony, float ringRadius) {
		double fallback = CardinalRiteBoundaryGeometry.socketClearanceAngle(
				ringRadius, ringRadius,
				CardinalRiteBoundaryGeometry.socketThroatRadius(
						ANCHOR_SOCKET_RADIUS, ANCHOR_SOCKET_CORE_WIDTH));
		if (ceremony == null || segment.startAnchorIndex() < 0
				|| segment.startAnchorIndex() >= ceremony.anchors().size()) {
			return new SegmentClearances(fallback, fallback);
		}
		CardinalRiteCeremonyDefinition.Anchor start =
				ceremony.anchors().get(segment.startAnchorIndex());
		double endAngle = normalizeAngle(
				segment.startAngle() + segment.sweepAngle());
		CardinalRiteCeremonyDefinition.Anchor end = null;
		double nearest = Double.POSITIVE_INFINITY;
		for (CardinalRiteCeremonyDefinition.Anchor candidate : ceremony.anchors()) {
			if (candidate.ring() != segment.ring()) continue;
			double candidateAngle = Math.atan2(candidate.z(), candidate.x());
			double distance = angularDistance(candidateAngle, endAngle);
			if (distance < nearest) {
				nearest = distance;
				end = candidate;
			}
		}
		return new SegmentClearances(
				socketClearance(start, ringRadius),
				end == null ? fallback : socketClearance(end, ringRadius));
	}

	private static List<Double> socketEndpointAngles(
			CardinalRiteCeremonyDefinition ceremony, int ring, float ringRadius) {
		if (ceremony == null) return List.of();
		List<Double> endpoints = new java.util.ArrayList<>();
		for (CardinalRiteCeremonyDefinition.Anchor anchor : ceremony.anchors()) {
			if (anchor.ring() != ring) continue;
			double anchorAngle = Math.atan2(anchor.z(), anchor.x());
			double clearance = socketClearance(anchor, ringRadius);
			endpoints.add(anchorAngle - clearance);
			endpoints.add(anchorAngle + clearance);
		}
		return List.copyOf(endpoints);
	}

	private static double socketClearance(
			CardinalRiteCeremonyDefinition.Anchor anchor, float ringRadius) {
		float socketCenterRadius = (float) Math.hypot(anchor.x(), anchor.z());
		return CardinalRiteBoundaryGeometry.socketClearanceAngle(
				ringRadius, socketCenterRadius,
				CardinalRiteBoundaryGeometry.socketThroatRadius(
						ANCHOR_SOCKET_RADIUS, ANCHOR_SOCKET_CORE_WIDTH));
	}

	private static double angularDistance(double first, double second) {
		double clockwise = normalizeAngle(first - second);
		return Math.min(clockwise, Math.PI * 2.0D - clockwise);
	}

	private record SegmentClearances(double start, double end) {
	}

	private static float anchorIntegrity(ActiveRiteClientData.RiteEntry rite, int anchorIndex) {
		for (CardinalRiteBoundaryProgress.Segment segment : rite.getBoundarySegments()) {
			if (segment.startAnchorIndex() == anchorIndex) return segment.integrity();
		}
		return 1.0F;
	}

	private static float boundaryBlobRadius(ActiveRiteClientData.RiteEntry rite,
			double worldX, double worldZ, float partialTick) {
		for (ActiveRiteClientData.SanguineBlob blob : rite.getSanguineBlobs()) {
			if (blob.kind() != ActiveRiteClientData.NodeKind.BOUNDARY_ANCHOR) continue;
			if (Math.abs(blob.x() - worldX) <= 0.01D
					&& Math.abs(blob.z() - worldZ) <= 0.01D) {
				return blob.renderRadius(partialTick);
			}
		}
		return 0.0F;
	}

	private static void drawSocketStain(VertexConsumer consumer, Matrix4f mat,
			float centerX, float centerZ, float intensity, float stainOpacity) {
		float innerAlpha = CardinalRiteBoundaryGeometry.socketStainInnerAlpha(intensity)
				* stainOpacity;
		float edgeAlpha = CardinalRiteBoundaryGeometry.socketStainEdgeAlpha(intensity)
				* stainOpacity;
		float innerRadius = 0.025F;
		float featherStart =
				CardinalRiteBoundaryGeometry.socketStainFeatherStartRadius(
						ANCHOR_SOCKET_RADIUS);
		float outerRadius = CardinalRiteBoundaryGeometry.socketStainOuterRadius(
				ANCHOR_SOCKET_RADIUS);
		float outerAlpha = CardinalRiteBoundaryGeometry.socketStainFeatherAlpha(
				intensity, ANCHOR_SOCKET_RADIUS, outerRadius) * stainOpacity;
		float stainY = CardinalRiteBoundaryGeometry.socketStainSurfaceOffset();
		for (int index = 0; index < SOCKET_STAIN_SEGMENTS; index++) {
			double a1 = Math.PI * 2.0D * index / SOCKET_STAIN_SEGMENTS;
			double a2 = Math.PI * 2.0D * (index + 1) / SOCKET_STAIN_SEGMENTS;
			float cos1 = (float) Math.cos(a1);
			float sin1 = (float) Math.sin(a1);
			float cos2 = (float) Math.cos(a2);
			float sin2 = (float) Math.sin(a2);
			emitQuad(consumer, mat,
					centerX + cos1 * innerRadius, stainY,
					centerZ + sin1 * innerRadius, 0.008F, 0.0F, 0.012F, innerAlpha,
					centerX + cos2 * innerRadius, stainY,
					centerZ + sin2 * innerRadius, 0.008F, 0.0F, 0.012F, innerAlpha,
					centerX + cos2 * featherStart, stainY,
					centerZ + sin2 * featherStart, 0.004F, 0.0F, 0.008F, edgeAlpha,
					centerX + cos1 * featherStart, stainY,
					centerZ + sin1 * featherStart, 0.004F, 0.0F, 0.008F, edgeAlpha);
			emitQuad(consumer, mat,
					centerX + cos1 * featherStart, stainY,
					centerZ + sin1 * featherStart, 0.004F, 0.0F, 0.008F, edgeAlpha,
					centerX + cos2 * featherStart, stainY,
					centerZ + sin2 * featherStart, 0.004F, 0.0F, 0.008F, edgeAlpha,
					centerX + cos2 * outerRadius, stainY,
					centerZ + sin2 * outerRadius, 0.004F, 0.0F, 0.008F, outerAlpha,
					centerX + cos1 * outerRadius, stainY,
					centerZ + sin1 * outerRadius, 0.004F, 0.0F, 0.008F, outerAlpha);
		}
	}

	private static void drawOrbitingBlood(VertexConsumer glowVC, VertexConsumer coreVC,
			Matrix4f mat, float centerX, float centerZ, float currentTime,
			int anchorIndex, float fill, float directionSign) {
		if (fill <= 0.02F || fill >= 0.98F) return;
		float envelope = (float) Math.sin(fill * Math.PI);
		float orbitRadius = 0.25F - fill * 0.06F;
		float beadRadius = 0.016F + fill * 0.014F;
		for (int bead = 0; bead < 3; bead++) {
			double angle = currentTime * 0.24D * directionSign
					+ anchorIndex * 1.17D + bead * Math.PI * 2.0D / 3.0D;
			float x = centerX + (float) Math.cos(angle) * orbitRadius;
			float z = centerZ + (float) Math.sin(angle) * orbitRadius;
			if (glowVC != null) {
				drawFlatDisc(glowVC, mat, x, z, 0.030F,
						beadRadius * 3.0F, 0.86F, 0.018F, 0.025F,
						envelope * 0.30F);
			}
			if (coreVC != null) {
				drawFlatDisc(coreVC, mat, x, z, 0.032F,
						beadRadius, 0.72F, 0.008F, 0.015F,
						envelope * 0.86F);
			}
		}
	}

	private static void drawFlatDisc(VertexConsumer consumer, Matrix4f mat,
			float centerX, float centerZ, float y, float radius,
			float red, float green, float blue, float alpha) {
		for (int index = 0; index < EFFECT_DISC_SEGMENTS; index++) {
			double a1 = Math.PI * 2.0D * index / EFFECT_DISC_SEGMENTS;
			double a2 = Math.PI * 2.0D * (index + 1) / EFFECT_DISC_SEGMENTS;
			emitQuad(consumer, mat,
					centerX, y, centerZ, red, green, blue, alpha,
					centerX, y, centerZ, red, green, blue, alpha,
					centerX + (float) Math.cos(a2) * radius, y,
					centerZ + (float) Math.sin(a2) * radius,
					red, green, blue, 0.0F,
					centerX + (float) Math.cos(a1) * radius, y,
					centerZ + (float) Math.sin(a1) * radius,
					red, green, blue, 0.0F);
		}
	}

	private static void drawEffectRing(VertexConsumer consumer, Matrix4f mat,
			float centerX, float centerZ, float y, float radius, float width,
			float red, float green, float blue, float alpha) {
		float inner = Math.max(0.0F, radius - width * 0.5F);
		float outer = radius + width * 0.5F;
		for (int index = 0; index < EFFECT_DISC_SEGMENTS; index++) {
			double a1 = Math.PI * 2.0D * index / EFFECT_DISC_SEGMENTS;
			double a2 = Math.PI * 2.0D * (index + 1) / EFFECT_DISC_SEGMENTS;
			float cos1 = (float) Math.cos(a1);
			float sin1 = (float) Math.sin(a1);
			float cos2 = (float) Math.cos(a2);
			float sin2 = (float) Math.sin(a2);
			emitQuad(consumer, mat,
					centerX + cos1 * inner, y, centerZ + sin1 * inner,
					red, green, blue, alpha,
					centerX + cos1 * outer, y, centerZ + sin1 * outer,
					red, green, blue, 0.0F,
					centerX + cos2 * outer, y, centerZ + sin2 * outer,
					red, green, blue, 0.0F,
					centerX + cos2 * inner, y, centerZ + sin2 * inner,
					red, green, blue, alpha);
		}
	}

	private static void drawSigilSegments(PoseStack stack, VertexConsumer consumer,
			ActiveRiteClientData.RiteEntry rite, float currentTime, Vec3 cam, boolean glow) {
		if (rite.getSigilSegments().isEmpty()) return;
		float pulse = (float) ((Math.sin(currentTime * 0.12D) + 1.0D) * 0.5D);
		float halfWidth = glow ? 0.18F : 0.055F;
		float alpha = glow ? 0.20F + pulse * 0.16F : 0.72F + pulse * 0.24F;
		Matrix4f matrix = stack.last().pose();
		IchorianSigilRenderPalette.Color vesselColor = IchorianSigilRenderPalette.vessel(glow);
		for (ActiveRiteClientData.SigilSegment segment : rite.getSigilSegments()) {
			long seed = Double.doubleToLongBits(segment.startX())
					^ Long.rotateLeft(Double.doubleToLongBits(segment.startZ()), 17)
					^ Long.rotateLeft(Double.doubleToLongBits(segment.endX()), 31)
					^ segment.color();
			List<IchorianSigilOrganicGeometry.Sample> samples =
					new java.util.ArrayList<>(SIGIL_VESSEL_SEGMENTS + 1);
			for (int step = 0; step <= SIGIL_VESSEL_SEGMENTS; step++) {
				samples.add(IchorianSigilOrganicGeometry.sample(
						segment.startX(), segment.startY(), segment.startZ(),
						segment.endX(), segment.endY(), segment.endZ(),
						currentTime, seed, step, SIGIL_VESSEL_SEGMENTS, halfWidth));
			}
			for (IchorianSigilOrganicGeometry.RibbonSegment vessel
					: IchorianSigilOrganicGeometry.ribbonSegments(samples)) {
				drawOrganicSigilSection(consumer, matrix, vessel, cam,
						vesselColor.red(), vesselColor.green(), vesselColor.blue(), alpha, glow);
			}
		}
	}

	private static void drawOrganicSigilSection(VertexConsumer consumer, Matrix4f matrix,
			IchorianSigilOrganicGeometry.RibbonSegment segment,
			Vec3 cam, float red, float green, float blue, float alpha, boolean glow) {
		IchorianSigilOrganicGeometry.RibbonJoint start = segment.start();
		IchorianSigilOrganicGeometry.RibbonJoint end = segment.end();
		float startRed = Math.min(1.0F, red * start.redIntensity());
		float endRed = Math.min(1.0F, red * end.redIntensity());
		float startY = (float) (start.centerY() - cam.y);
		float endY = (float) (end.centerY() - cam.y);
		emitQuad(consumer, matrix,
				(float) (start.leftX() - cam.x), startY,
				(float) (start.leftZ() - cam.z), startRed, green, blue, glow ? 0.0F : alpha,
				(float) (start.rightX() - cam.x), startY,
				(float) (start.rightZ() - cam.z), startRed, green, blue, alpha,
				(float) (end.rightX() - cam.x), endY,
				(float) (end.rightZ() - cam.z), endRed, green, blue, alpha,
				(float) (end.leftX() - cam.x), endY,
				(float) (end.leftZ() - cam.z), endRed, green, blue, glow ? 0.0F : alpha);
	}

	private static void drawSanguineBlobs(PoseStack stack, VertexConsumer consumer,
			ActiveRiteClientData.RiteEntry rite, float currentTime, Vec3 cam, boolean glow) {
		for (ActiveRiteClientData.SanguineBlob blob : rite.getSanguineBlobs()) {
			boolean boundaryAnchor =
					blob.kind() == ActiveRiteClientData.NodeKind.BOUNDARY_ANCHOR;
			float radius = CardinalRiteBoundaryGeometry.landmarkRenderRadius(
					blob.renderRadius(currentTime - (float) Math.floor(currentTime)),
					boundaryAnchor, glow);
			stack.pushPose();
			stack.translate(blob.x() - cam.x, blob.y() - cam.y, blob.z() - cam.z);
			var recipe = boundaryAnchor
					? IchorianSigilLandmarkGeometry.boundaryAnchor(blob.seed())
					: IchorianSigilLandmarkGeometry.forRole(blob.role(), blob.seed());
			IchorianSigilLandmarkRenderer.render(
					consumer, stack, recipe, radius, blob.color(), currentTime,
					blob.seed(), blob.integrity(), glow, true);
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
			float gR, float gG, float gB,
			List<CardinalRiteBoundaryProgress.Segment> visibleArcs, boolean legacy) {

		// Veins are evenly spaced but their angular position crawls slowly over time
		double crawlOffset = time * VEIN_CRAWL_SPEED * directionSign;

		for (int v = 0; v < VEIN_COUNT; v++) {
			double baseAngle = crawlOffset + (Math.PI * 2.0 / VEIN_COUNT) * v;
			float boundarySurfaceY = CardinalRiteBoundaryGeometry.surfaceSafeOffset(
					(float) (Math.sin(baseAngle * 5.0D
							+ time * 0.06D * directionSign) * 0.012D));
			float surfaceY = CardinalRiteBoundaryGeometry.veinSurfaceOffset(boundarySurfaceY);
			if (!legacy && !CardinalRiteBoundaryGeometry.hasVisibleBeamAt(
					visibleArcs, baseAngle)) continue;
			float rootIntegrity = legacy ? 1.0F
					: arcIntegrity(visibleArcs, baseAngle, time);
			if (rootIntegrity <= 0.01F) continue;

			// Each vein has a slightly different length and curve based on its index
			// Use a deterministic hash-like value from the vein index for variety
			double veinSeed = hashVein(v);
			float length = VEIN_LENGTH * (0.6f + 0.4f * (float) veinSeed);

			// Vein pulsing — individual veins throb at slightly offset phases
			double veinPulse = (Math.sin(time * 0.1 + v * 1.7) + 1.0) * 0.5;
			float veinAlpha = (float) (0.3 + 0.5 * veinPulse) * rootIntegrity;

			// Slight angular wander as it grows inward (gives curve)
			double curvature = Math.sin(veinSeed * 17.3) * 0.35;
			float rootRadius = CardinalRiteBoundaryGeometry.veinRootRadius(
					baseRadius, CORE_WIDTH,
					undulation(baseAngle, time, directionSign));

			for (int s = 0; s < VEIN_SEGS; s++) {
				float t0 = (float) s / VEIN_SEGS;
				float t1 = (float) (s + 1) / VEIN_SEGS;

				// Radius decreases inward from the ring
				float rad0 = rootRadius - t0 * length;
				float rad1 = rootRadius - t1 * length;

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
							cos0 * rad0 - px0 * w0, surfaceY, sin0 * rad0 - pz0 * w0, cR, cG, cB, a0 * cA,
							cos0 * rad0 + px0 * w0, surfaceY, sin0 * rad0 + pz0 * w0, cR, cG, cB, a0 * cA,
							cos1 * rad1 + px1 * w1, surfaceY, sin1 * rad1 + pz1 * w1, cR, cG, cB, a1 * cA,
							cos1 * rad1 - px1 * w1, surfaceY, sin1 * rad1 - pz1 * w1, cR, cG, cB, a1 * cA);
				}

				if (glowVC != null) {
					// Glow halo around vein (wider, more transparent)
					float gw0 = w0 * 3.0f;
					float gw1 = w1 * 3.0f;
					float ga0 = a0 * 0.35f;
					float ga1 = a1 * 0.25f;

					emitQuad(glowVC, mat,
							cos0 * rad0 - px0 * gw0, surfaceY, sin0 * rad0 - pz0 * gw0, gR, gG, gB, 0f,
							cos0 * rad0 + px0 * gw0, surfaceY, sin0 * rad0 + pz0 * gw0, gR, gG, gB, 0f,
							cos1 * rad1 + px1 * gw1, surfaceY, sin1 * rad1 + pz1 * gw1, gR, gG, gB, 0f,
							cos1 * rad1 - px1 * gw1, surfaceY, sin1 * rad1 - pz1 * gw1, gR, gG, gB, 0f);

					// Inner part of glow (brighter core of the halo)
					emitQuad(glowVC, mat,
							cos0 * rad0 - px0 * gw0, surfaceY, sin0 * rad0 - pz0 * gw0, gR, gG, gB, 0f,
							cos0 * rad0 - px0 * w0, surfaceY, sin0 * rad0 - pz0 * w0, gR, gG, gB, ga0,
							cos1 * rad1 - px1 * w1, surfaceY, sin1 * rad1 - pz1 * w1, gR, gG, gB, ga1,
							cos1 * rad1 - px1 * gw1, surfaceY, sin1 * rad1 - pz1 * gw1, gR, gG, gB, 0f);

					emitQuad(glowVC, mat,
							cos0 * rad0 + px0 * w0, surfaceY, sin0 * rad0 + pz0 * w0, gR, gG, gB, ga0,
							cos0 * rad0 + px0 * gw0, surfaceY, sin0 * rad0 + pz0 * gw0, gR, gG, gB, 0f,
							cos1 * rad1 + px1 * gw1, surfaceY, sin1 * rad1 + pz1 * gw1, gR, gG, gB, 0f,
							cos1 * rad1 + px1 * w1, surfaceY, sin1 * rad1 + pz1 * w1, gR, gG, gB, ga1);
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

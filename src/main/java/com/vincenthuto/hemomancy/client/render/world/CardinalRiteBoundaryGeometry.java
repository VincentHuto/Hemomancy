package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteBoundaryProgress;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteMath;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAnchorVisualRules;
import net.minecraft.core.BlockPos;

import java.util.List;

/**
 * Shared geometry rules for the visible Cardinal Rite rings and the enclosing
 * exterior field. Keeping both calculations together prevents the Fane shell
 * from drifting inside or outside the ring that visually defines the rite.
 */
public final class CardinalRiteBoundaryGeometry {
	private static final float DORMANT_SOCKET_INTENSITY = 0.35F;
	private static final float FULL_BOUNDARY_BLOB_RADIUS = 0.19F;

	private CardinalRiteBoundaryGeometry() {
	}

	public static boolean shouldRenderExterior(int degree) {
		return degree >= 3;
	}

	public static float boundaryPlaneY(int centerY) {
		return (float) CardinalRiteAnchorVisualRules.ritePlaneY(centerY);
	}

	public static float surfaceSafeOffset(float verticalWave) {
		return 0.025F + verticalWave;
	}

	public static float boundaryStainSurfaceOffset() {
		return 0.002F;
	}

	public static float socketStainSurfaceOffset() {
		return 0.008F;
	}

	public static float socketThroatRadius(float socketRadius, float socketBandWidth) {
		return Math.max(0.0F,
				socketRadius - Math.max(0.0F, socketBandWidth) * 0.5F);
	}

	public static double socketClearanceAngle(
			float ringRadius, float socketCenterRadius, float socketRadius) {
		if (ringRadius <= 0.0F || socketCenterRadius <= 0.0F) return 0.0D;
		double denominator = 2.0D * ringRadius * socketCenterRadius;
		double cosine = (ringRadius * ringRadius
				+ socketCenterRadius * socketCenterRadius
				- socketRadius * socketRadius) / denominator;
		return Math.acos(Math.max(-1.0D, Math.min(1.0D, cosine)));
	}

	/**
	 * Number of equal pieces a sweep is cut into so no piece is wider than one
	 * segment of a full circle; the last piece ends exactly on the sweep's end.
	 */
	public static int pieceCount(double sweepAngle, int fullCircleSegments) {
		if (sweepAngle <= 0.0D || fullCircleSegments <= 0) return 0;
		return Math.max(1, (int) Math.ceil(sweepAngle / (Math.PI * 2.0D / fullCircleSegments)));
	}

	public static float endpointWaveScale(
			double angle, List<Double> endpointAngles, double fadeAngle) {
		if (endpointAngles.isEmpty() || fadeAngle <= 0.0D) return 1.0F;
		double nearest = fadeAngle;
		for (int index = 0; index < endpointAngles.size(); index++) {
			nearest = Math.min(nearest, CardinalRiteMath.angularDistance(angle, endpointAngles.get(index)));
		}
		return CardinalRiteMath.smoothstep((float) Math.min(1.0D, nearest / fadeAngle));
	}

	public static float veinRootRadius(float boundaryRadius, float boundaryWidth, float undulation) {
		return boundaryRadius + undulation - boundaryWidth * 0.5F;
	}

	public static float veinSurfaceOffset(float boundarySurfaceOffset) {
		return boundarySurfaceOffset + 0.006F;
	}

	public static float landmarkRenderRadius(
			float coreRadius, boolean boundaryAnchor, boolean glow) {
		return coreRadius + (glow && !boundaryAnchor ? 0.09F : 0.0F);
	}

	public static float interactiveRingRadius(int ringIndex) {
		return 2.0F + Math.max(0, ringIndex);
	}

	public static float anchorAlignedRingRadius(
			float fallbackRadius, List<BlockPos> anchors) {
		if (anchors == null || anchors.isEmpty()) return fallbackRadius;
		double radiusSum = 0.0D;
		int count = 0;
		for (BlockPos anchor : anchors) {
			if (anchor == null) continue;
			radiusSum += Math.hypot(anchor.getX(), anchor.getZ());
			count++;
		}
		return count == 0 ? fallbackRadius : (float) (radiusSum / count);
	}

	public static float exteriorRadius(int riteSize, int completedRings, boolean legacy) {
		int ringCount = legacy ? Math.max(1, (riteSize - 1) / 2) : Math.max(0, completedRings);
		if (ringCount == 0) return 0.0F;
		return legacy
				? (float) (riteSize / 2.0D + 1.0D + (ringCount - 1) * 2.0D)
				: interactiveRingRadius(ringCount - 1);
	}

	public static List<CardinalRiteBoundaryProgress.Segment> animatedSocketArcs(
			CardinalRiteBoundaryProgress.Segment segment, float growth,
			double startClearance, double endClearance) {
		float clampedGrowth = CardinalRiteMath.clamp01(growth);
		if (clampedGrowth <= 0.0F) return List.of();
		double clampedStart = Math.max(0.0D, startClearance);
		double clampedEnd = Math.max(0.0D, endClearance);
		double availableSweep = segment.sweepAngle() - clampedStart - clampedEnd;
		if (availableSweep <= 0.0D) return List.of();
		double clippedStart = segment.startAngle() + clampedStart;
		if (clampedGrowth >= 1.0F) {
			return List.of(new CardinalRiteBoundaryProgress.Segment(
					segment.ring(), clippedStart, availableSweep,
					segment.startAnchorIndex(), segment.integrity()));
		}
		double endpointSweep = availableSweep * clampedGrowth * 0.5D;
		return List.of(
				new CardinalRiteBoundaryProgress.Segment(
						segment.ring(), clippedStart, endpointSweep,
						segment.startAnchorIndex(), segment.integrity()),
				new CardinalRiteBoundaryProgress.Segment(
						segment.ring(), clippedStart + availableSweep - endpointSweep,
						endpointSweep, segment.startAnchorIndex(), segment.integrity()));
	}

	public static float socketIntensity(float blobRadius) {
		float fill = socketFill(blobRadius);
		return DORMANT_SOCKET_INTENSITY
				+ (1.0F - DORMANT_SOCKET_INTENSITY) * fill;
	}

	public static float socketFill(float blobRadius) {
		return CardinalRiteMath.clamp01(blobRadius / FULL_BOUNDARY_BLOB_RADIUS);
	}

	public static float sealPulseAlpha(float effectAge) {
		if (effectAge < 0.0F) return 0.0F;
		float remaining = CardinalRiteMath.clamp01(1.0F - effectAge / 10.0F);
		return remaining * remaining;
	}

	public static float sealTravel(float effectAge) {
		return CardinalRiteMath.clamp01(effectAge / 8.0F);
	}

	public static float bolusProgress(float effectAge) {
		return CardinalRiteMath.clamp01((effectAge - 3.0F) / 14.0F);
	}

	public static float bolusAlpha(float effectAge) {
		if (effectAge < 3.0F || effectAge > 17.0F) return 0.0F;
		return Math.max(0.0F, (float) Math.sin(
				bolusProgress(effectAge) * Math.PI));
	}

	public static SocketDistortion socketDistortion(
			double angle, float time, int anchorIndex, float integrity) {
		float damage = 1.0F - CardinalRiteMath.clamp01(integrity);
		if (damage <= 0.0F) return SocketDistortion.NONE;
		float offsetX = (float) Math.sin(time * 0.91F + anchorIndex * 1.7F)
				* 0.06F * damage;
		float offsetZ = (float) Math.cos(time * 1.13F + anchorIndex * 0.83F)
				* 0.06F * damage;
		float radialScale = 1.0F + (float) Math.sin(
				angle * 3.0D + time * 0.72F + anchorIndex)
				* 0.16F * damage;
		return new SocketDistortion(offsetX, offsetZ, radialScale);
	}

	public record SocketDistortion(float offsetX, float offsetZ, float radialScale) {
		public static final SocketDistortion NONE = new SocketDistortion(0.0F, 0.0F, 1.0F);
	}

	public static boolean hasVisibleBeamAt(
			List<CardinalRiteBoundaryProgress.Segment> arcs, double angle) {
		double normalizedAngle = CardinalRiteMath.normalizeAngle(angle);
		for (int index = 0; index < arcs.size(); index++) {
			CardinalRiteBoundaryProgress.Segment arc = arcs.get(index);
			if (arc.integrity() <= 0.01F) continue;
			double fromStart = CardinalRiteMath.normalizeAngle(
					normalizedAngle - CardinalRiteMath.normalizeAngle(arc.startAngle()));
			if (fromStart <= arc.sweepAngle()) return true;
		}
		return false;
	}

	public static float integrityWidth(float healthyWidth, float integrity) {
		return healthyWidth * (0.22F + 0.78F * CardinalRiteMath.clamp01(integrity));
	}

	public static float integrityBrightness(float integrity) {
		return 0.12F + 0.88F * CardinalRiteMath.clamp01(integrity);
	}

	public static float arterialHighlight(double angle, float time, int ring) {
		double distance = CardinalRiteMath.angularDistance(angle, arterialHighlightPosition(time, ring));
		double normalized = Math.max(0.0D, 1.0D - distance / 0.42D);
		return (float) (normalized * normalized * (3.0D - 2.0D * normalized));
	}

	public static float boundaryStainWidth() {
		return 0.82F;
	}

	public static float boundaryStainAlpha(int ring) {
		return 0.28F * Math.max(0.30F, 1.0F - ring * 0.10F);
	}

	public static float stainOpacity(float progress) {
		return CardinalRiteMath.smoothstep(progress);
	}

	public static float socketStainOuterRadius(float socketRadius) {
		float boundaryHalfWidth = boundaryStainWidth() * 0.5F;
		return (float) Math.hypot(Math.max(0.0F, socketRadius), boundaryHalfWidth) * 1.08F;
	}

	public static float socketStainFeatherStartRadius(float socketRadius) {
		float safeSocketRadius = Math.max(0.0F, socketRadius);
		float outerRadius = socketStainOuterRadius(safeSocketRadius);
		return safeSocketRadius + (outerRadius - safeSocketRadius) * 0.35F;
	}

	public static float socketStainFeatherAlpha(
			float intensity, float socketRadius, float radialDistance) {
		float featherStart = socketStainFeatherStartRadius(socketRadius);
		float outerRadius = socketStainOuterRadius(socketRadius);
		if (radialDistance <= featherStart) return socketStainEdgeAlpha(intensity);
		if (radialDistance >= outerRadius) return 0.0F;
		float progress = (radialDistance - featherStart) / (outerRadius - featherStart);
		return socketStainEdgeAlpha(intensity) * (1.0F - CardinalRiteMath.smoothstep(progress));
	}

	public static float boundaryStainSocketMask(
			float x, float z, float socketX, float socketZ,
			float socketRadius, float feather) {
		float distance = (float) Math.hypot(x - socketX, z - socketZ);
		float safeRadius = Math.max(0.0F, socketRadius);
		if (distance <= safeRadius) return 0.0F;
		if (feather <= 0.0F) return 1.0F;
		return CardinalRiteMath.smoothstep((distance - safeRadius) / feather);
	}

	public static float socketStainInnerAlpha(float intensity) {
		return 0.34F + intensity * 0.12F;
	}

	public static float socketStainEdgeAlpha(float intensity) {
		return 0.12F + intensity * 0.06F;
	}

	public static double arterialHighlightPosition(float time, int ring) {
		return CardinalRiteMath.normalizeAngle(time * 0.035D + ring * 0.73D);
	}
}

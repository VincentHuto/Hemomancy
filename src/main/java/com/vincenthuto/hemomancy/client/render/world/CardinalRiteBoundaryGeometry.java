package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteBoundaryProgress;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteFootprintRules;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAnchorVisualRules;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Optional;

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

	public static SocketJunction socketJunction(
			float ringRadius, float socketX, float socketZ,
			float socketRadius, int side) {
		double anchorAngle = Math.atan2(socketZ, socketX);
		float socketCenterRadius = (float) Math.hypot(socketX, socketZ);
		double clearance = socketClearanceAngle(
				ringRadius, socketCenterRadius, socketRadius);
		double boundaryAngle = anchorAngle + (side < 0 ? -clearance : clearance);
		float x = (float) Math.cos(boundaryAngle) * ringRadius;
		float z = (float) Math.sin(boundaryAngle) * ringRadius;
		double socketAngle = Math.atan2(z - socketZ, x - socketX);
		return new SocketJunction(boundaryAngle, socketAngle, x, z);
	}

	public static List<AngularArc> socketBodyArcs(
			double tangentAngle, double gateHalfAngle) {
		return socketBodyArcsBetweenGates(
				tangentAngle, tangentAngle + Math.PI, gateHalfAngle);
	}

	public static List<AngularArc> socketOverlayArcs() {
		return List.of(new AngularArc(0.0D, Math.PI * 2.0D));
	}

	public static List<AngularArc> socketBodyArcsBetweenGates(
			double firstGateAngle, double secondGateAngle,
			double gateHalfAngle) {
		double clampedGate = Math.max(0.0D,
				Math.min(Math.PI * 0.45D, gateHalfAngle));
		double first = normalizeAngle(firstGateAngle);
		double second = normalizeAngle(secondGateAngle);
		double firstSweep = Math.max(0.0D,
				normalizeAngle(second - first) - clampedGate * 2.0D);
		double secondSweep = Math.max(0.0D,
				normalizeAngle(first - second) - clampedGate * 2.0D);
		return List.of(
				new AngularArc(first + clampedGate, firstSweep),
				new AngularArc(second + clampedGate, secondSweep));
	}

	public static double normalizeRadians(double angle) {
		return normalizeAngle(angle);
	}

	public static List<AngularArc> tessellateArc(
			double startAngle, double sweepAngle, int fullCircleSegments) {
		if (sweepAngle <= 0.0D || fullCircleSegments <= 0) return List.of();
		double maximumSweep = Math.PI * 2.0D / fullCircleSegments;
		int pieceCount = Math.max(1, (int) Math.ceil(sweepAngle / maximumSweep));
		double pieceSweep = sweepAngle / pieceCount;
		java.util.ArrayList<AngularArc> pieces = new java.util.ArrayList<>(pieceCount);
		for (int index = 0; index < pieceCount; index++) {
			pieces.add(new AngularArc(startAngle + pieceSweep * index, pieceSweep));
		}
		return List.copyOf(pieces);
	}

	public static float socketEndpointWaveScale(
			double angle, List<CardinalRiteBoundaryProgress.Segment> segments,
			float ringRadius, float socketThroatRadius, double fadeAngle) {
		if (segments.isEmpty() || ringRadius <= 0.0F || fadeAngle <= 0.0D) {
			return 1.0F;
		}
		double ratio = Math.max(0.0D, Math.min(1.0D,
				socketThroatRadius / (ringRadius * 2.0D)));
		double clearance = 2.0D * Math.asin(ratio);
		double nearest = Double.POSITIVE_INFINITY;
		for (CardinalRiteBoundaryProgress.Segment segment : segments) {
			double start = segment.startAngle() + clearance;
			double end = segment.startAngle() + segment.sweepAngle() - clearance;
			nearest = Math.min(nearest, angularDistance(angle, start));
			nearest = Math.min(nearest, angularDistance(angle, end));
		}
		return smoothstep((float) Math.min(1.0D, nearest / fadeAngle));
	}

	public static float endpointWaveScale(
			double angle, List<Double> endpointAngles, double fadeAngle) {
		if (endpointAngles.isEmpty() || fadeAngle <= 0.0D) return 1.0F;
		double nearest = endpointAngles.stream()
				.mapToDouble(endpoint -> angularDistance(angle, endpoint))
				.min()
				.orElse(fadeAngle);
		return smoothstep((float) Math.min(1.0D, nearest / fadeAngle));
	}

	public static float socketGateWaveScale(
			double angle, double tangentAngle, double gateHalfAngle,
			double fadeAngle) {
		return socketGateWaveScale(angle, tangentAngle,
				tangentAngle + Math.PI, gateHalfAngle, fadeAngle);
	}

	public static float socketGateWaveScale(
			double angle, double firstGateAngle, double secondGateAngle,
			double gateHalfAngle, double fadeAngle) {
		if (fadeAngle <= 0.0D) return 1.0F;
		double nearest = Double.POSITIVE_INFINITY;
		for (double gateCenter : new double[] {
				firstGateAngle, secondGateAngle}) {
			nearest = Math.min(nearest,
					angularDistance(angle, gateCenter - gateHalfAngle));
			nearest = Math.min(nearest,
					angularDistance(angle, gateCenter + gateHalfAngle));
		}
		return smoothstep((float) Math.min(1.0D, nearest / fadeAngle));
	}

	private static double angularDistance(double first, double second) {
		double difference = normalizeAngle(first - second);
		return Math.min(difference, Math.PI * 2.0D - difference);
	}

	private static float smoothstep(float value) {
		float clamped = Math.max(0.0F, Math.min(1.0F, value));
		return clamped * clamped * (3.0F - 2.0F * clamped);
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

	public static float footprintRadius(List<BlockPos> boundaryPoints, List<BlockPos> sigilPoints) {
		return CardinalRiteFootprintRules.radius(boundaryPoints, sigilPoints);
	}

	public static List<CardinalRiteBoundaryProgress.Segment> animatedSocketArcs(
			CardinalRiteBoundaryProgress.Segment segment, float growth,
			float ringRadius, float socketRadius) {
		double clearance = socketClearanceAngle(
				ringRadius, ringRadius, socketRadius);
		return animatedSocketArcs(segment, growth, clearance, clearance);
	}

	public static List<CardinalRiteBoundaryProgress.Segment> animatedSocketArcs(
			CardinalRiteBoundaryProgress.Segment segment, float growth,
			double startClearance, double endClearance) {
		float clampedGrowth = Math.max(0.0F, Math.min(1.0F, growth));
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

	public static Optional<CardinalRiteBoundaryProgress.Segment> completedSocketArc(
			CardinalRiteBoundaryProgress.Segment segment, float growth,
			float ringRadius, float socketRadius) {
		double clearance = socketClearanceAngle(
				ringRadius, ringRadius, socketRadius);
		return completedSocketArc(segment, growth, clearance, clearance);
	}

	public static Optional<CardinalRiteBoundaryProgress.Segment> completedSocketArc(
			CardinalRiteBoundaryProgress.Segment segment, float growth,
			double startClearance, double endClearance) {
		if (growth < 1.0F) return Optional.empty();
		List<CardinalRiteBoundaryProgress.Segment> arcs =
				animatedSocketArcs(segment, 1.0F, startClearance, endClearance);
		return arcs.isEmpty() ? Optional.empty() : Optional.of(arcs.getFirst());
	}

	public static float socketIntensity(float blobRadius) {
		float fill = socketFill(blobRadius);
		return DORMANT_SOCKET_INTENSITY
				+ (1.0F - DORMANT_SOCKET_INTENSITY) * fill;
	}

	public static float socketFill(float blobRadius) {
		return Math.max(0.0F, Math.min(1.0F,
				blobRadius / FULL_BOUNDARY_BLOB_RADIUS));
	}

	public static float sealPulseAlpha(float effectAge) {
		if (effectAge < 0.0F) return 0.0F;
		float remaining = Math.max(0.0F, Math.min(1.0F,
				1.0F - effectAge / 10.0F));
		return remaining * remaining;
	}

	public static float sealTravel(float effectAge) {
		return Math.max(0.0F, Math.min(1.0F, effectAge / 8.0F));
	}

	public static float bolusProgress(float effectAge) {
		return Math.max(0.0F, Math.min(1.0F,
				(effectAge - 3.0F) / 14.0F));
	}

	public static float bolusAlpha(float effectAge) {
		if (effectAge < 3.0F || effectAge > 17.0F) return 0.0F;
		return Math.max(0.0F, (float) Math.sin(
				bolusProgress(effectAge) * Math.PI));
	}

	public static SocketDistortion socketDistortion(
			double angle, float time, int anchorIndex, float integrity) {
		float damage = 1.0F - Math.max(0.0F, Math.min(1.0F, integrity));
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
	}

	public record AngularArc(double startAngle, double sweepAngle) {
	}

	public record SocketJunction(
			double boundaryAngle, double socketAngle, float x, float z) {
	}

	public static boolean hasVisibleBeamAt(
			List<CardinalRiteBoundaryProgress.Segment> arcs, double angle) {
		double normalizedAngle = normalizeAngle(angle);
		for (CardinalRiteBoundaryProgress.Segment arc : arcs) {
			if (arc.integrity() <= 0.01F) continue;
			double fromStart = normalizeAngle(
					normalizedAngle - normalizeAngle(arc.startAngle()));
			if (fromStart <= arc.sweepAngle()) return true;
		}
		return false;
	}

	public static float integrityWidth(float healthyWidth, float integrity) {
		float clamped = Math.max(0.0F, Math.min(1.0F, integrity));
		return healthyWidth * (0.22F + 0.78F * clamped);
	}

	public static float integrityBrightness(float integrity) {
		float clamped = Math.max(0.0F, Math.min(1.0F, integrity));
		return 0.12F + 0.88F * clamped;
	}

	public static float arterialHighlight(double angle, float time, int ring) {
		double phase = normalizeAngle(angle - arterialHighlightPosition(time, ring));
		double distance = Math.min(phase, Math.PI * 2.0D - phase);
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
		float clamped = Math.max(0.0F, Math.min(1.0F, progress));
		return clamped * clamped * (3.0F - 2.0F * clamped);
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
		return socketStainEdgeAlpha(intensity) * (1.0F - smoothstep(progress));
	}

	public static double socketStainClearanceAngle(
			float ringRadius, float socketCenterRadius, float socketRadius) {
		return socketClearanceAngle(
				ringRadius, socketCenterRadius, socketStainOuterRadius(socketRadius));
	}

	public static float boundaryStainSocketMask(
			float x, float z, float socketX, float socketZ,
			float socketRadius, float feather) {
		float distance = (float) Math.hypot(x - socketX, z - socketZ);
		float safeRadius = Math.max(0.0F, socketRadius);
		if (distance <= safeRadius) return 0.0F;
		if (feather <= 0.0F) return 1.0F;
		return smoothstep((distance - safeRadius) / feather);
	}

	public static float socketStainInnerAlpha(float intensity) {
		return 0.34F + intensity * 0.12F;
	}

	public static float socketStainEdgeAlpha(float intensity) {
		return 0.12F + intensity * 0.06F;
	}

	public static double arterialHighlightPosition(float time, int ring) {
		return normalizeAngle(time * 0.035D + ring * 0.73D);
	}

	private static double normalizeAngle(double angle) {
		double fullCircle = Math.PI * 2.0D;
		double normalized = angle % fullCircle;
		return normalized < 0.0D ? normalized + fullCircle : normalized;
	}
}

package com.vincenthuto.hemomancy.common.world.fold;

import java.util.Optional;

public final class FoldActivationBounds {
	private static final double LOOSE_APERTURE_EPSILON = 0.08D;
	private static final double DEPTH_ENTRY_EPSILON = 0.005D;
	private static final double MIN_ENTRY_DEPTH_DELTA = 0.01D;
	private static final double MIN_ENTRY_NORMAL_DOT = 0.30D;
	private static final double ENTRY_PLAYER_CENTER_MARGIN = 0.30D;

	private FoldActivationBounds() {
	}

	public static Optional<FoldViewerSide> entrySideForCrossing(FoldedHallwaySpec spec, FoldVector previousWorld,
			FoldVector currentWorld) {
		return entryCrossing(spec, previousWorld, currentWorld).map(EntryCrossing::entrySide);
	}

	public static Optional<EntryCrossing> entryCrossing(FoldedHallwaySpec spec, FoldVector previousWorld,
			FoldVector currentWorld) {
		FoldTransform transform = FoldTransform.of(spec);
		FoldTransform.FoldCoordinates previous = transform.toFold(previousWorld);
		FoldTransform.FoldCoordinates current = transform.toFold(currentWorld);
		double previousDepth = previous.realDepth();
		double currentDepth = current.realDepth();
		double depthDelta = currentDepth - previousDepth;
		double lateralDelta = current.lateral() - previous.lateral();
		if (Math.abs(depthDelta) < MIN_ENTRY_DEPTH_DELTA) {
			return Optional.empty();
		}
		double horizontalLength = Math.sqrt(depthDelta * depthDelta + lateralDelta * lateralDelta);
		if (horizontalLength < 1.0E-7D || Math.abs(depthDelta) / horizontalLength < MIN_ENTRY_NORMAL_DOT) {
			return Optional.empty();
		}

		FoldViewerSide entrySide;
		if (previousDepth <= 0.0D && currentDepth > 0.0D) {
			entrySide = FoldViewerSide.FRONT;
		} else if (previousDepth >= 0.0D && currentDepth < 0.0D) {
			entrySide = FoldViewerSide.BACK;
		} else {
			return Optional.empty();
		}

		if (Math.abs(depthDelta) < 1.0E-7D) {
			return Optional.empty();
		}
		double t = -previousDepth / depthDelta;
		if (t < 0.0D || t > 1.0D) {
			return Optional.empty();
		}

		double lateral = lerp(previous.lateral(), current.lateral(), t);
		double vertical = lerp(previous.vertical(), current.vertical(), t);
		FoldTransform.FoldCoordinates crossing = new FoldTransform.FoldCoordinates(lateral, vertical, 0.0D, 0.0D);
		return insideEntryAperture(spec, crossing) ? Optional.of(new EntryCrossing(entrySide, t)) : Optional.empty();
	}

	public static boolean containsTraversalPoint(FoldedHallwaySpec spec, FoldViewerSide entrySide, FoldVector world) {
		FoldTransform.FoldCoordinates coordinates = FoldTransform.of(spec).toFold(world);
		double progress = realDepthProgress(spec, entrySide, world);
		return insideAperture(spec, coordinates)
				&& progress >= -DEPTH_ENTRY_EPSILON
				&& progress <= spec.realDepth() + DEPTH_ENTRY_EPSILON;
	}

	public static Optional<ExitCrossing> exitCrossing(FoldedHallwaySpec spec, FoldViewerSide entrySide,
			FoldVector previousWorld, FoldVector compressedMovement) {
		double previousProgress = realDepthProgress(spec, entrySide, previousWorld);
		double currentProgress = realDepthProgress(spec, entrySide, previousWorld.add(compressedMovement));
		double progressDelta = currentProgress - previousProgress;
		if (Math.abs(progressDelta) < 1.0E-7D) {
			return Optional.empty();
		}

		double threshold;
		if (previousProgress >= 0.0D && currentProgress < 0.0D) {
			threshold = 0.0D;
		} else if (previousProgress <= spec.realDepth() && currentProgress > spec.realDepth()) {
			threshold = spec.realDepth();
		} else {
			return Optional.empty();
		}

		double fraction = (threshold - previousProgress) / progressDelta;
		return fraction >= 0.0D && fraction <= 1.0D
				? Optional.of(new ExitCrossing(fraction))
				: Optional.empty();
	}

	public static double realDepthProgress(FoldedHallwaySpec spec, FoldViewerSide entrySide, FoldVector world) {
		double realDepth = FoldTransform.of(spec).toFold(world).realDepth();
		return entrySide == FoldViewerSide.FRONT ? realDepth : -realDepth;
	}

	public static FoldVector toWorldAtProgress(FoldedHallwaySpec spec, FoldViewerSide entrySide,
			double lateral, double vertical, double realDepthProgress) {
		double signedDepth = entrySide == FoldViewerSide.FRONT ? realDepthProgress : -realDepthProgress;
		return FoldTransform.of(spec).toWorld(lateral, vertical, signedDepth);
	}

	private static boolean insideAperture(FoldedHallwaySpec spec, FoldTransform.FoldCoordinates coordinates) {
		return Math.abs(coordinates.lateral()) <= spec.apertureHalfWidth() + LOOSE_APERTURE_EPSILON
				&& coordinates.vertical() >= -LOOSE_APERTURE_EPSILON
				&& coordinates.vertical() <= spec.apertureHeight() + LOOSE_APERTURE_EPSILON;
	}

	private static boolean insideEntryAperture(FoldedHallwaySpec spec, FoldTransform.FoldCoordinates coordinates) {
		double maxEntryLateral = Math.max(0.0D, spec.apertureHalfWidth() - ENTRY_PLAYER_CENTER_MARGIN);
		return Math.abs(coordinates.lateral()) <= maxEntryLateral
				&& coordinates.vertical() >= -LOOSE_APERTURE_EPSILON
				&& coordinates.vertical() <= spec.apertureHeight() + LOOSE_APERTURE_EPSILON;
	}

	private static double lerp(double start, double end, double t) {
		return start + (end - start) * t;
	}

	public record EntryCrossing(FoldViewerSide entrySide, double fraction) {
	}

	public record ExitCrossing(double fraction) {
	}
}

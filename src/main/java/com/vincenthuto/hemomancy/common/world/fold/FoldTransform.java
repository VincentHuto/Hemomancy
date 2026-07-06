package com.vincenthuto.hemomancy.common.world.fold;

public final class FoldTransform {
	private final FoldedHallwaySpec spec;

	private FoldTransform(FoldedHallwaySpec spec) {
		this.spec = spec;
	}

	public static FoldTransform of(FoldedHallwaySpec spec) {
		return new FoldTransform(spec);
	}

	public double compressionRatio() {
		return spec.realDepth() / spec.virtualLength();
	}

	public FoldCoordinates toFold(FoldVector world) {
		FoldVector origin = spec.origin();
		double dx = world.x - origin.x;
		double dz = world.z - origin.z;
		FoldFacing facing = spec.facing();
		double lateral = dx * facing.rightX() + dz * facing.rightZ();
		double realDepth = dx * facing.forwardX() + dz * facing.forwardZ();
		return new FoldCoordinates(lateral, world.y - origin.y, realDepth, realDepth / compressionRatio());
	}

	public FoldVector toWorld(double lateral, double vertical, double realDepth) {
		FoldVector origin = spec.origin();
		FoldFacing facing = spec.facing();
		double x = origin.x + lateral * facing.rightX() + realDepth * facing.forwardX();
		double z = origin.z + lateral * facing.rightZ() + realDepth * facing.forwardZ();
		return new FoldVector(x, origin.y + vertical, z);
	}

	public FoldViewerSide viewerSide(FoldVector world) {
		return toFold(world).realDepth() < 0.0D ? FoldViewerSide.FRONT : FoldViewerSide.BACK;
	}

	public FoldVector toVisualWorld(FoldViewerSide viewerSide, double lateral, double vertical, double visualDepth) {
		return toWorld(lateral, vertical, visualDepth * viewerSide.visualDepthSign());
	}

	public FoldVector oppositeExitCamera(FoldViewerSide viewerSide, double lateral, double vertical,
			double exitOffset) {
		double realDepth = viewerSide == FoldViewerSide.FRONT ? spec.realDepth() + exitOffset : -exitOffset;
		return toWorld(lateral, vertical, realDepth);
	}

	public FoldVector compressMovement(FoldVector movement) {
		FoldFacing facing = spec.facing();
		double lateral = movement.x * facing.rightX() + movement.z * facing.rightZ();
		double forward = movement.x * facing.forwardX() + movement.z * facing.forwardZ();
		double compressedForward = forward * compressionRatio();
		double x = lateral * facing.rightX() + compressedForward * facing.forwardX();
		double z = lateral * facing.rightZ() + compressedForward * facing.forwardZ();
		return new FoldVector(x, movement.y, z);
	}

	public FoldVector compressMovementAfterEntry(FoldVector movement, double entryFraction) {
		double clampedEntryFraction = clamp(entryFraction, 0.0D, 1.0D);
		FoldVector beforeEntry = movement.scale(clampedEntryFraction);
		FoldVector afterEntry = movement.scale(1.0D - clampedEntryFraction);
		return beforeEntry.add(compressMovement(afterEntry));
	}

	public FoldVector compressMovementUntilExit(FoldVector movement, double exitFraction) {
		double clampedExitFraction = clamp(exitFraction, 0.0D, 1.0D);
		FoldVector insideFold = movement.scale(clampedExitFraction);
		FoldVector outsideFold = movement.scale(1.0D - clampedExitFraction);
		return compressMovement(insideFold).add(outsideFold);
	}

	public FoldVector clampWorldPosition(FoldVector world) {
		FoldCoordinates fold = toFold(world);
		return toWorld(
				clamp(fold.lateral(), -spec.apertureHalfWidth(), spec.apertureHalfWidth()),
				clamp(fold.vertical(), 0.0D, spec.apertureHeight()),
				clamp(fold.realDepth(), 0.0D, spec.realDepth()));
	}

	public ExitSide exitSide(FoldVector world) {
		double realDepth = toFold(world).realDepth();
		if (realDepth < 0.0D) {
			return ExitSide.FRONT;
		}
		if (realDepth > spec.realDepth()) {
			return ExitSide.BACK;
		}
		return ExitSide.NONE;
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	public record FoldCoordinates(double lateral, double vertical, double realDepth, double virtualDepth) {
	}

	public enum ExitSide {
		NONE,
		FRONT,
		BACK
	}
}

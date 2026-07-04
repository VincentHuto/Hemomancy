package com.vincenthuto.hemomancy.common.world.fold;

public final class FoldRenderWindow {
	private static final double CAMERA_NEAR_CLIP = 0.08D;

	private FoldRenderWindow() {
	}

	public static DepthSpan unclipped(double start, double end) {
		return new DepthSpan(start, end);
	}

	public static boolean needsApertureStencil(boolean insideFold) {
		return !insideFold;
	}

	public static DepthSpan clipForInsideCamera(FoldViewerSide viewerSide, double visualRealDepthProgress,
			double start, double end) {
		double cameraVisualDepth = visualRealDepthProgress / viewerSide.visualDepthSign();
		double clippedStart = Math.max(start, cameraVisualDepth + CAMERA_NEAR_CLIP);
		return new DepthSpan(clippedStart, end);
	}

	public record DepthSpan(double start, double end) {
		public boolean visible() {
			return end > start;
		}
	}
}

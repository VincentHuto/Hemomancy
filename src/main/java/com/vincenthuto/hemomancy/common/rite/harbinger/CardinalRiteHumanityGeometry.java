package com.vincenthuto.hemomancy.common.rite.harbinger;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a sparse layered point cloud shaped like a tall, tapering daemon.
 * Enlarged particles bridge the samples so the figure remains readable
 * without maintaining hundreds of long-lived translucent particles.
 */
public final class CardinalRiteHumanityGeometry {
	public static final int EMISSION_INTERVAL_TICKS = 8;
	public static final double DEFAULT_ENTITY_HEIGHT = 4.0D;
	private static final int OUTLINE_LEVELS = 16;
	private static final int EYE_RED = 145;
	private static final int EYE_GREEN = 3;
	private static final int EYE_BLUE = 6;

	private CardinalRiteHumanityGeometry() {
	}

	public static List<Point> cloud(double requestedHeight, double phase,
			double forwardX, double forwardZ) {
		return buildCloud(requestedHeight, requestedHeight, phase, forwardX, forwardZ);
	}

	private static List<Point> buildCloud(double requestedHeight, double maturityHeight, double phase,
			double forwardX, double forwardZ) {
		double height = Math.max(0.75D, requestedHeight);
		double forwardLength = Math.hypot(forwardX, forwardZ);
		double normalizedForwardX = forwardLength < 0.0001D ? 0.0D : forwardX / forwardLength;
		double normalizedForwardZ = forwardLength < 0.0001D ? 1.0D : forwardZ / forwardLength;
		double rightX = normalizedForwardZ;
		double rightZ = -normalizedForwardX;
		double width = clamp(height * 0.11D, 0.32D, 0.72D);
		double manifestation = clamp((maturityHeight - 0.75D) / 4.25D, 0.0D, 1.0D);
		int corePointCount = 16 + (int) Math.round(manifestation * 8.0D);
		List<Point> points = new ArrayList<>(OUTLINE_LEVELS * 2 + corePointCount + 6);

		for (int level = 0; level < OUTLINE_LEVELS; level++) {
			double heightFraction = level / (double) (OUTLINE_LEVELS - 1);
			double halfWidth = contour(heightFraction) * width;
			double depth = Math.sin(phase * 0.7D + level * 1.73D) * width * 0.24D;
			double flicker = Math.sin(phase * 1.9D + level * 2.41D) * width * 0.035D;
			addSwayedLocal(points, Layer.PALE_AURA, -halfWidth - flicker,
					verticalPosition(heightFraction, height), depth,
					heightFraction, height, width, phase,
					rightX, rightZ, normalizedForwardX, normalizedForwardZ);
			addSwayedLocal(points, Layer.PALE_AURA, halfWidth + flicker,
					verticalPosition(heightFraction, height), -depth,
					heightFraction, height, width, phase,
					rightX, rightZ, normalizedForwardX, normalizedForwardZ);
		}

		for (int index = 0; index < corePointCount; index++) {
			double heightFraction = 0.10D + index / (double) (corePointCount - 1) * 0.86D;
			double bodyWidth = contour(heightFraction) * width * 0.84D;
			double angle = phase * 0.55D + index * 2.399963D;
			addSwayedLocal(points, Layer.VOID_CORE,
					Math.sin(angle) * bodyWidth,
					verticalPosition(heightFraction, height),
					Math.cos(angle) * width * 0.36D,
					heightFraction, height, width, phase,
					rightX, rightZ, normalizedForwardX, normalizedForwardZ);
		}

		double eyeY = verticalPosition(0.885D, height);
		double eyeSeparation = width * 0.21D;
		double eyeDepth = width * 0.62D;
		for (int side : new int[] { -1, 1 }) {
			double eyeX = side * eyeSeparation;
			addColoredSwayedLocal(points, Layer.EYE, eyeX, eyeY, eyeDepth,
					0.885D, height, width, phase, EYE_RED, EYE_GREEN, EYE_BLUE,
					rightX, rightZ, normalizedForwardX, normalizedForwardZ);
		}

		for (int index = 0; index < 4; index++) {
			double heightFraction = 0.12D + index * 0.22D;
			double angle = phase * 1.4D + index * 1.27D;
			double wispWidth = contour(heightFraction) * width * 0.76D;
			addSwayedLocal(points, Layer.BLOOD_WISP,
					Math.sin(angle) * wispWidth,
					verticalPosition(heightFraction, height),
					Math.cos(angle) * width * 0.36D,
					heightFraction, height, width, phase,
					rightX, rightZ, normalizedForwardX, normalizedForwardZ);
		}

		return List.copyOf(points);
	}

	public static float particleScale(Layer layer, double spriteScale) {
		double sanitizedScale = Double.isFinite(spriteScale) ? spriteScale : 1.0D;
		double sizeFactor = clamp(Math.sqrt(Math.max(0.1D, sanitizedScale)), 0.65D, 1.45D);
		double baseScale = switch (layer) {
			case VOID_CORE -> 0.05D;
			case PALE_AURA -> 0.12D;
			case EYE -> 0.19D;
			case BLOOD_WISP -> 0.105D;
		};
		return (float) (baseScale * sizeFactor);
	}

	public static int particleLifetime(Layer layer) {
		return switch (layer) {
			case VOID_CORE -> 32;
			case PALE_AURA -> 34;
			case EYE -> 38;
			case BLOOD_WISP -> 26;
		};
	}

	public static ParticleStyle particleStyle(Layer layer) {
		return layer == Layer.VOID_CORE ? ParticleStyle.DIFFUSE_GLOW : ParticleStyle.GLOW;
	}

	public static List<Point> scaledCloud(double requestedScale, double phase,
			double forwardX, double forwardZ) {
		double scale = Double.isFinite(requestedScale)
				? clamp(requestedScale, 0.1D, 8.0D)
				: 1.0D;
		double visibleHeight = DEFAULT_ENTITY_HEIGHT * scale;
		return buildCloud(DEFAULT_ENTITY_HEIGHT, visibleHeight, phase, forwardX, forwardZ).stream()
				.map(point -> new Point(point.layer(),
						point.x() * scale,
						point.y() * scale,
						point.z() * scale,
						point.red(), point.green(), point.blue()))
				.toList();
	}

	public static Point orientPoint(Point point, double forwardX, double forwardZ, float proneDegrees) {
		double forwardLength = Math.hypot(forwardX, forwardZ);
		double normalizedForwardX = forwardLength < 0.0001D ? 0.0D : forwardX / forwardLength;
		double normalizedForwardZ = forwardLength < 0.0001D ? 1.0D : forwardZ / forwardLength;
		double rightX = normalizedForwardZ;
		double rightZ = -normalizedForwardX;
		double lateral = point.x() * rightX + point.z() * rightZ;
		double longitudinal = point.x() * normalizedForwardX + point.z() * normalizedForwardZ;
		double pitchRadians = Math.toRadians(proneDegrees);
		double vertical = point.y() * Math.cos(pitchRadians) - longitudinal * Math.sin(pitchRadians);
		double forward = point.y() * Math.sin(pitchRadians) + longitudinal * Math.cos(pitchRadians);
		return new Point(point.layer(),
				rightX * lateral + normalizedForwardX * forward,
				vertical,
				rightZ * lateral + normalizedForwardZ * forward,
				point.red(), point.green(), point.blue());
	}

	private static double verticalPosition(double heightFraction, double height) {
		double headFraction = clamp((heightFraction - 0.76D) / 0.24D, 0.0D, 1.0D);
		double easedStretch = headFraction * headFraction * (3.0D - 2.0D * headFraction);
		return heightFraction * height + easedStretch * height * 0.06D;
	}

	public static double absorptionProgress(double riteProgress) {
		double linear = clamp((riteProgress - 0.95D) / 0.05D, 0.0D, 1.0D);
		return linear * linear * (3.0D - 2.0D * linear);
	}

	public static double contractionScale(double riteProgress) {
		return lerp(absorptionProgress(riteProgress), 1.0D, 0.12D);
	}

	private static double contour(double heightFraction) {
		if (heightFraction < 0.12D) {
			return lerp(heightFraction / 0.12D, 0.08D, 0.25D);
		}
		if (heightFraction < 0.58D) {
			return lerp((heightFraction - 0.12D) / 0.46D, 0.25D, 0.72D);
		}
		if (heightFraction < 0.72D) {
			return lerp((heightFraction - 0.58D) / 0.14D, 0.72D, 1.0D);
		}
		if (heightFraction < 0.78D) {
			return lerp((heightFraction - 0.72D) / 0.06D, 1.0D, 0.48D);
		}
		double headAxis = (heightFraction - 0.89D) / 0.11D;
		return 0.08D + 0.48D * Math.sqrt(Math.max(0.0D, 1.0D - headAxis * headAxis));
	}

	private static int layerRed(Layer layer) {
		return switch (layer) {
			case VOID_CORE -> 3;
			case PALE_AURA -> 235;
			case EYE -> 255;
			case BLOOD_WISP -> 190;
		};
	}

	private static int layerGreen(Layer layer) {
		return layer == Layer.PALE_AURA ? 230 : 0;
	}

	private static int layerBlue(Layer layer) {
		return switch (layer) {
			case VOID_CORE -> 2;
			case PALE_AURA -> 225;
			case EYE -> 255;
			case BLOOD_WISP -> 12;
		};
	}

	private static void addColoredLocal(List<Point> points, Layer layer,
			double localX, double y, double localZ, int red, int green, int blue,
			double rightX, double rightZ, double forwardX, double forwardZ) {
		points.add(new Point(layer,
				rightX * localX + forwardX * localZ,
				y,
				rightZ * localX + forwardZ * localZ,
				red, green, blue));
	}

	private static void addSwayedLocal(List<Point> points, Layer layer,
			double localX, double y, double localZ,
			double heightFraction, double height, double width, double phase,
			double rightX, double rightZ, double forwardX, double forwardZ) {
		addColoredSwayedLocal(points, layer, localX, y, localZ,
				heightFraction, height, width, phase,
				layerRed(layer), layerGreen(layer), layerBlue(layer),
				rightX, rightZ, forwardX, forwardZ);
	}

	private static void addColoredSwayedLocal(List<Point> points, Layer layer,
			double localX, double y, double localZ,
			double heightFraction, double height, double width, double phase,
			int red, int green, int blue,
			double rightX, double rightZ, double forwardX, double forwardZ) {
		double growth = clamp((height - 0.75D) / 4.25D, 0.0D, 1.0D);
		double swayWeight = heightFraction * heightFraction * (3.0D - 2.0D * heightFraction);
		double lateralSway = Math.sin(phase * 0.22D) * width
				* lerp(growth, 0.06D, 0.22D) * swayWeight;
		double depthSway = Math.sin(phase * 0.17D + 1.1D) * width
				* lerp(growth, 0.025D, 0.09D) * swayWeight;
		addColoredLocal(points, layer, localX + lateralSway, y, localZ + depthSway,
				red, green, blue, rightX, rightZ, forwardX, forwardZ);
	}

	private static double lerp(double amount, double start, double end) {
		return start + (end - start) * amount;
	}

	private static double clamp(double value, double minimum, double maximum) {
		return Math.max(minimum, Math.min(maximum, value));
	}

	public enum Layer {
		VOID_CORE,
		PALE_AURA,
		EYE,
		BLOOD_WISP
	}

	public enum ParticleStyle {
		DIFFUSE_GLOW,
		GLOW
	}

	public record Point(Layer layer, double x, double y, double z, int red, int green, int blue) {
	}
}

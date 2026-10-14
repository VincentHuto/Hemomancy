package com.vincenthuto.hemomancy.client.render.world.rite;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * The maths behind the daemon's smoke volume: the box it is marched inside, the 3D trail of its moving smoke, and
 * the moments the shader is told about (emergence, thinning, the flash of a swallowed offering). No rendering
 * here, so all of it is unit-tested.
 */
public final class DaemonHaze {
	/** The box's half-width, as a share of the daemon's height: the figure, its soot shell and a sideways trail. */
	public static final double BOX_HALF_WIDTH = 0.30D;
	/** The box's half-height, as a share of the daemon's height: room below for wisps trailing a rising daemon. */
	public static final double BOX_HALF_HEIGHT = 0.66D;
	/**
	 * How far past a face (blocks) a camera still counts as inside the box. The shader uses the same margin, so a
	 * camera grazing a face, where the near face is clipped by the near plane, still marches from the eye.
	 */
	public static final double INSIDE_MARGIN = 0.15D;
	/** The staff haze box's half-width and half-height (blocks): about the size of the staff's head. */
	public static final double STAFF_HAZE_HALF_WIDTH = 0.6D;
	public static final double STAFF_HAZE_HALF_HEIGHT = 0.85D;
	/** How far the staff haze box sits above the staff's head, so the rising wisps have room. */
	public static final double STAFF_HAZE_LIFT = 0.25D;
	/**
	 * Streaming at full speed, how much longer the teardrop below the chest stretches (the shader's TAIL_STRETCH).
	 */
	public static final double TAIL_STRETCH = 0.9D;
	/** The teardrop's length below the chest, from the chest at 0.60 to its tip at 0.03 of the height. */
	private static final double TAIL_LENGTH = 0.57D;
	/** Room for the tail's fanning wisps, its coil and the trail, as a share of the height at full streaming. */
	private static final double STREAM_MARGIN = 0.17D;
	/** Height fraction where swallowed offerings come to rest. */
	public static final double CHEST = 0.62D;
	/** How long a swallowed offering takes to dissolve, and the haze to settle after it. */
	public static final float SWALLOW_TICKS = 20.0F;
	private static final float TRAIL_GAIN = 2.5F;
	private static final float TRAIL_MAX = 0.6F;

	private DaemonHaze() {
	}

	/**
	 * The box's half-width and half-height in blocks, centred on the entity. Tilted off upright (radians) the body
	 * lies along its path, so the box grows across as it tips over; streaming (0 to 1) its tail stretches out and its
	 * coil swings wider, so the box reaches past them.
	 */
	public static double[] box(double scale, double tilt, double stream) {
		double height = CardinalRiteVisualRules.DAEMON_HEIGHT * Math.max(0.0D, scale);
		double streaming = Math.max(0.0D, Math.min(1.0D, stream));
		double along = BOX_HALF_HEIGHT + (TAIL_LENGTH * TAIL_STRETCH + STREAM_MARGIN) * streaming;
		double across = BOX_HALF_WIDTH + STREAM_MARGIN * 0.6D * streaming;
		double sin = Math.abs(Math.sin(tilt));
		double cos = Math.abs(Math.cos(tilt));
		return new double[] {(along * sin + across * cos) * height, (along * cos + across * sin) * height};
	}

	/**
	 * The tilt that turns the upright body so its crown points along {@code crown} (a unit vector): {axis x, axis y,
	 * axis z, angle in radians}, about a level axis. Straight down, it tips over through the front of {@code yaw}
	 * (radians, Minecraft's yaw).
	 */
	public static float[] tilt(double crownX, double crownY, double crownZ, double yaw) {
		double across = Math.hypot(crownX, crownZ);
		float angle = (float) Math.atan2(across, crownY);
		if (across < 1.0E-6D) return new float[] {(float) Math.cos(yaw), 0.0F, (float) Math.sin(yaw), angle};
		return new float[] {(float) (crownZ / across), 0.0F, (float) (-crownX / across), angle};
	}

	/**
	 * Centre of the staff haze box for a Focus. The planted staff's head (model y 20 to 29 of every variant, through
	 * the planted pose and the model's fixed display transform, turned 90 degrees) is centred on the block about 2.84
	 * blocks up; the box is lifted {@link #STAFF_HAZE_LIFT} above it.
	 */
	public static double[] staffHazeCentre(double focusX, double focusY, double focusZ) {
		return new double[] {focusX + 0.47D, focusY + 2.84D + STAFF_HAZE_LIFT, focusZ + 0.51D};
	}

	/**
	 * The eye the frame is drawn from, in a box's local space: the camera centre of projection x model-view x the
	 * box's pose. Minecraft folds view bobbing into the projection, so this moves with the bob where the camera's
	 * position does not, and the volume stays put as the viewer walks.
	 */
	public static Vector3f eyeInBox(Matrix4f projection, Matrix4f modelView, Matrix4f pose) {
		Matrix4f toLocal = new Matrix4f(projection).mul(modelView).mul(pose).invert();
		Vector4f eye = toLocal.transform(new Vector4f(0.0F, 0.0F, 1.0F, 0.0F));
		float w = Math.abs(eye.w) < 1.0E-9F ? 1.0E-9F : eye.w;
		return new Vector3f(eye.x / w, eye.y / w, eye.z / w);
	}

	/** Whether a camera (relative to the daemon's centre, blocks) is inside its box, within {@link #INSIDE_MARGIN}. */
	public static boolean cameraInside(double x, double y, double z, double halfWidth, double halfHeight) {
		return Math.abs(x) < halfWidth + INSIDE_MARGIN && Math.abs(y) < halfHeight + INSIDE_MARGIN
				&& Math.abs(z) < halfWidth + INSIDE_MARGIN;
	}

	/** Where the smoke trails, in world axes, from the daemon's motion per tick: always opposite the motion. */
	public static float[] trail(double motionX, double motionY, double motionZ) {
		return new float[] {cap(-motionX * TRAIL_GAIN), cap(-motionY * TRAIL_GAIN), cap(-motionZ * TRAIL_GAIN)};
	}

	/** The eyes open only at the very end of emergence. */
	public static float eyesOpen(float emergence) {
		return (float) CardinalRiteVisualRules.smooth((emergence - 0.8D) / 0.2D);
	}

	/** The haze's flash after an offering arrives: quick to rise, gone by {@link #SWALLOW_TICKS}. */
	public static float swallow(float sinceArrival) {
		if (sinceArrival < 0.0F) return 0.0F;
		return (float) (CardinalRiteVisualRules.smooth(sinceArrival / 3.0D)
				* (1.0D - CardinalRiteVisualRules.smooth(sinceArrival / SWALLOW_TICKS)));
	}

	/** Density of the haze: loose before the seal, thinned to a quarter as it is drawn back into the staff. */
	public static float strength(double firmness, float cancellation) {
		return (float) (Math.max(0.0D, Math.min(1.0D, firmness))
				* (1.0D - 0.75D * Math.max(0.0F, Math.min(1.0F, cancellation))));
	}

	public static double sinkProgress(float sinceArrival) {
		return CardinalRiteVisualRules.smooth(sinceArrival / SWALLOW_TICKS);
	}

	/** A swallowed offering's place {x, y, z} relative to the entity and its item scale, drifting to the chest. */
	public static double[] sink(double fromX, double fromY, double fromZ, double scale, float sinceArrival) {
		double t = sinkProgress(Math.max(0.0F, sinceArrival));
		double chest = CardinalRiteVisualRules.bodyY(CHEST, scale);
		double size = 0.3D * Math.max(0.35D, scale) * (1.0D - t);
		return new double[] {fromX * (1.0D - t), fromY + (chest - fromY) * t, fromZ * (1.0D - t), size};
	}

	private static float cap(double value) {
		return (float) Math.max(-TRAIL_MAX, Math.min(TRAIL_MAX, value));
	}
}

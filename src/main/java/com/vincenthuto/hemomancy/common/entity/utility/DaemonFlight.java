package com.vincenthuto.hemomancy.common.entity.utility;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * How the daemon's smoke carries itself as it moves, eased tick by tick on the client from how it actually moved:
 * a smoothed velocity; the way its crown points (straight up while it hovers, swinging over to lead along its path,
 * diving or climbing, as it picks up speed); how far its tail streams out behind; and how far it has flowed, which
 * the shader streams its smoke along. Nothing here snaps: every part eases toward where the motion says it belongs.
 */
public final class DaemonFlight {
	/** Share of each tick's motion folded into the smoothed velocity. */
	public static final double VELOCITY_RATE = 0.3D;
	/** Share of the remaining swing the crown makes toward where it belongs each tick. */
	public static final double CROWN_RATE = 0.16D;
	/** Share of the remaining change the tail's streaming makes each tick. */
	public static final double STREAM_RATE = 0.12D;
	/** Slower than this (blocks per tick) it hovers upright. */
	public static final double HOVER_SPEED = 0.015D;
	/** From this speed on it lies along its path, its tail streaming out fully. */
	public static final double FULL_SPEED = 0.12D;
	/** The flow wraps here (heights), keeping the shader's float precision. */
	static final double FLOW_WRAP = 4096.0D;

	private double velocityX;
	private double velocityY;
	private double velocityZ;
	private double crownX;
	private double crownY = 1.0D;
	private double crownZ;
	private double crownOX;
	private double crownOY = 1.0D;
	private double crownOZ;
	private double stream;
	private double streamO;
	private double flow;
	private double flowO;

	/**
	 * One tick: how far the daemon moved this tick (blocks), the way it faces (radians, Minecraft's yaw) and its
	 * height (blocks).
	 */
	public void tick(double motionX, double motionY, double motionZ, double yaw, double height) {
		crownOX = crownX;
		crownOY = crownY;
		crownOZ = crownZ;
		streamO = stream;
		flowO = flow;
		velocityX += (motionX - velocityX) * VELOCITY_RATE;
		velocityY += (motionY - velocityY) * VELOCITY_RATE;
		velocityZ += (motionZ - velocityZ) * VELOCITY_RATE;
		double speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ);
		double lean = HemoEasing.smoothstep((speed - HOVER_SPEED) / (FULL_SPEED - HOVER_SPEED));
		double forwardX = -Math.sin(yaw);
		double forwardZ = Math.cos(yaw);

		// Where the crown belongs: tipped from upright toward the path, as far as its speed leans it.
		double[] target = {0.0D, 1.0D, 0.0D};
		if (lean > 0.0D) {
			double pathX = velocityX / speed;
			double pathY = velocityY / speed;
			double pathZ = velocityZ / speed;
			double across = Math.hypot(pathX, pathZ);
			// Straight up or down it tips over through its front.
			double swingX = across > 1.0E-4D ? pathX / across : forwardX;
			double swingZ = across > 1.0E-4D ? pathZ / across : forwardZ;
			double angle = Math.acos(clamp(pathY, -1.0D, 1.0D)) * lean;
			target = new double[] {swingX * Math.sin(angle), Math.cos(angle), swingZ * Math.sin(angle)};
		}
		swingCrownToward(target, forwardX, forwardZ);
		stream += (lean - stream) * STREAM_RATE;
		flow = (flow + speed / Math.max(height, 0.1D)) % FLOW_WRAP;
	}

	/** Turns the crown a share of the way toward the target, about the axis between them, so it swings, never snaps. */
	private void swingCrownToward(double[] target, double forwardX, double forwardZ) {
		double cos = clamp(crownX * target[0] + crownY * target[1] + crownZ * target[2], -1.0D, 1.0D);
		double apart = Math.acos(cos);
		if (apart < 1.0E-7D) return;
		double[] axis = cross(crownX, crownY, crownZ, target[0], target[1], target[2]);
		if (length(axis) < 1.0E-6D) {
			// Exactly opposite: come round through its front, or over its top if it already lies along its front.
			axis = cross(crownX, crownY, crownZ, forwardX, 0.0D, forwardZ);
			if (length(axis) < 1.0E-6D) axis = cross(crownX, crownY, crownZ, 0.0D, 1.0D, 0.0D);
		}
		double norm = length(axis);
		double kx = axis[0] / norm;
		double ky = axis[1] / norm;
		double kz = axis[2] / norm;
		double angle = apart * CROWN_RATE;
		double c = Math.cos(angle);
		double s = Math.sin(angle);
		double along = kx * crownX + ky * crownY + kz * crownZ;
		double[] turned = cross(kx, ky, kz, crownX, crownY, crownZ);
		double x = crownX * c + turned[0] * s + kx * along * (1.0D - c);
		double y = crownY * c + turned[1] * s + ky * along * (1.0D - c);
		double z = crownZ * c + turned[2] * s + kz * along * (1.0D - c);
		double size = Math.sqrt(x * x + y * y + z * z);
		crownX = x / size;
		crownY = y / size;
		crownZ = z / size;
	}

	/** The way the crown points (a unit vector, world axes), between the last tick and this one. */
	public double[] crown(float partialTick) {
		double x = crownOX + (crownX - crownOX) * partialTick;
		double y = crownOY + (crownY - crownOY) * partialTick;
		double z = crownOZ + (crownZ - crownOZ) * partialTick;
		double size = Math.sqrt(x * x + y * y + z * z);
		if (size < 1.0E-9D) return new double[] {crownX, crownY, crownZ};
		return new double[] {x / size, y / size, z / size};
	}

	/** How far its tail streams out behind: 0 hovering, 1 at full speed. */
	public double stream(float partialTick) {
		return streamO + (stream - streamO) * partialTick;
	}

	/** How far it has flowed, in its own heights. */
	public double flow(float partialTick) {
		if (flow < flowO) return flow;
		return flowO + (flow - flowO) * partialTick;
	}

	/** The smoothed velocity (blocks per tick). */
	public double[] velocity() {
		return new double[] {velocityX, velocityY, velocityZ};
	}

	private static double[] cross(double ax, double ay, double az, double bx, double by, double bz) {
		return new double[] {ay * bz - az * by, az * bx - ax * bz, ax * by - ay * bx};
	}

	private static double length(double[] v) {
		return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}
}

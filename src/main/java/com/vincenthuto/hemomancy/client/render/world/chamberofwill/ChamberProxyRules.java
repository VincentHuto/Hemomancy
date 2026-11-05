package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

/**
 * Where the camera stands in the Chamber when a viewer looks through a husk's eye: the viewer's offset from the eye,
 * turned so the husk's facing becomes south (out of the corridor), added to the gate's eye. The husk's eye is the
 * gate's eye seen from the other side: a window, so a viewer in front of the husk looks in through the gate from the
 * same distance. Pure: positions are x, y, z triples.
 */
public final class ChamberProxyRules {
	private ChamberProxyRules() {
	}

	public record Placement(double x, double y, double z, float yaw, float pitch) {
	}

	/**
	 * @param facingYRot the husk's facing as a yaw (south 0, west 90, north 180, east 270)
	 * @param huskEye the husk's eye centre; {@code gateEye} the corridor gate's
	 * @param camera the viewer's camera position, {@code yaw} and {@code pitch} its rotation
	 */
	public static Placement place(float facingYRot, double[] huskEye, double[] gateEye, double[] camera, float yaw, float pitch) {
		return place(facingYRot, huskEye, gateEye, camera, yaw, pitch, 0.0D);
	}

	/** As above, with the view carried {@code depth} blocks in through the gate: how far a walk in has gone. */
	public static Placement place(float facingYRot, double[] huskEye, double[] gateEye, double[] camera, float yaw, float pitch,
			double depth) {
		double ox = camera[0] - huskEye[0];
		double oy = camera[1] - huskEye[1];
		double oz = camera[2] - huskEye[2];
		// Turning by the facing's yaw about Y takes a vector of yaw t to yaw t - facing: the husk's front to south.
		double angle = Math.toRadians(facingYRot);
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double tx = ox * cos + oz * sin;
		double tz = -ox * sin + oz * cos;
		return new Placement(gateEye[0] + tx, gateEye[1] + oy, gateEye[2] + tz - depth, wrap(yaw - facingYRot), pitch);
	}

	/** The view out of a husk stands at least this far in front of it, whatever is behind the viewer. */
	public static final double OUTSIDE_FRONT = 0.4D;

	/**
	 * The view out of a husk: the viewer's offset from the gate's eye, turned so south (out of the corridor) becomes
	 * the husk's facing, added to the husk's eye and carried {@code depth} blocks out along that facing. The yaw turns
	 * the same way. A window onto open space may carry its camera behind the pane as the viewer backs away; this one
	 * looks out onto ground and trees, so the camera stays at least {@link #OUTSIDE_FRONT} in front of the husk.
	 */
	public static Placement placeOutside(float facingYRot, double[] gateEye, double[] huskEye, double[] camera, float yaw,
			float pitch, double depth) {
		double ox = camera[0] - gateEye[0];
		double oy = camera[1] - gateEye[1];
		double oz = camera[2] - gateEye[2];
		double angle = Math.toRadians(facingYRot);
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		// The inverse of place(): a vector of yaw t to yaw t + facing.
		double tx = ox * cos - oz * sin;
		double tz = ox * sin + oz * cos;
		// The facing as a direction: south (0, 0, 1), west (-1, 0, 0), north (0, 0, -1), east (1, 0, 0).
		double dirX = -sin;
		double dirZ = cos;
		double along = tx * dirX + tz * dirZ;
		double front = Math.max(OUTSIDE_FRONT, along) + depth;
		double lateralX = tx - along * dirX;
		double lateralZ = tz - along * dirZ;
		return new Placement(huskEye[0] + lateralX + dirX * front, huskEye[1] + oy, huskEye[2] + lateralZ + dirZ * front,
				wrap(yaw + facingYRot), pitch);
	}

	private static float wrap(float degrees) {
		float wrapped = degrees % 360.0F;
		if (wrapped >= 180.0F) wrapped -= 360.0F;
		if (wrapped < -180.0F) wrapped += 360.0F;
		return wrapped;
	}
}

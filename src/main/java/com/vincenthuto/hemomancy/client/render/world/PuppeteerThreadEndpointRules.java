package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class PuppeteerThreadEndpointRules {
	static final double SUMMON_HEIGHT_SCALE = 0.45D;
	private static final double WORLD_Y_OFFSET = 1.0D;
	private static final double HAND_SIDE_OFFSET = 0.32D;
	private static final double HAND_FORWARD_OFFSET = 0.42D;
	private static final double HAND_DOWN_OFFSET = 1.05D;
	private static final double THIRD_PERSON_HAND_EXTRA_DROP = 0.70D;
	private static final double ROOT_RADIUS = 0.016D;
	private static final double TIP_RADIUS = 0.009D;
	/** Share of the thread over which each end rounds shut: about three segments at the hand, two at the body. */
	private static final double ROOT_CLOSE = 3.0D / 28.0D;
	private static final double TIP_CLOSE = 2.0D / 28.0D;

	private PuppeteerThreadEndpointRules() {
	}

	public static Vec3 summonEndpoint(double oldX, double oldY, double oldZ,
			double x, double y, double z, double boundingBoxHeight, float partialTick) {
		return summonEndpoint(oldX, oldY, oldZ, x, y, z, boundingBoxHeight,
				SUMMON_HEIGHT_SCALE, partialTick);
	}

	public static Vec3 summonEndpoint(double oldX, double oldY, double oldZ,
			double x, double y, double z, double boundingBoxHeight,
			double heightScale, float partialTick) {
		return new Vec3(
				Mth.lerp(partialTick, oldX, x),
				Mth.lerp(partialTick, oldY, y) + boundingBoxHeight * heightScale + WORLD_Y_OFFSET,
				Mth.lerp(partialTick, oldZ, z));
	}

	static Vec3 playerHandEndpoint(Vec3 eyePosition, Vec3 viewVector, float yawRadians,
			double side, boolean firstPerson) {
		Vec3 right = new Vec3(Mth.cos(yawRadians), 0.0D, Mth.sin(yawRadians));
		double drop = HAND_DOWN_OFFSET + (firstPerson ? 0.0D : THIRD_PERSON_HAND_EXTRA_DROP);
		return eyePosition.add(right.scale(HAND_SIDE_OFFSET * side))
				.add(viewVector.normalize().scale(HAND_FORWARD_OFFSET))
				.add(0.0D, WORLD_Y_OFFSET - drop, 0.0D);
	}

	/**
	 * Tube radius at {@code t} along the thread (0 at the controller, 1 at the body). The thread is built from
	 * open hexagonal segments, so both ends round to a point rather than showing the hollow tube.
	 */
	static double threadRadius(double t) {
		double close = Math.sin(Math.min(1.0D, t / ROOT_CLOSE) * Math.PI * 0.5D)
				* Math.sin(Math.min(1.0D, (1.0D - t) / TIP_CLOSE) * Math.PI * 0.5D);
		return Mth.lerp(t, ROOT_RADIUS, TIP_RADIUS) * Math.max(0.0D, close);
	}

	/** Circus performers hold their strings where a third-person player holds a Crossbar: the right hand. */
	static Vec3 performerHandEndpoint(Vec3 eyePosition, float bodyYawRadians) {
		Vec3 forward = new Vec3(-Mth.sin(bodyYawRadians), 0.0D, Mth.cos(bodyYawRadians));
		return playerHandEndpoint(eyePosition, forward, bodyYawRadians, -1.0D, false);
	}
}

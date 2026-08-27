package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class PuppeteerThreadEndpointRules {
	static final double SUMMON_HEIGHT_SCALE = 0.45D;
	private static final double HAND_SIDE_OFFSET = 0.32D;
	private static final double HAND_FORWARD_OFFSET = 0.42D;
	private static final double HAND_DOWN_OFFSET = 1.05D;
	private static final double THIRD_PERSON_HAND_EXTRA_DROP = 0.70D;

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
				Mth.lerp(partialTick, oldY, y) + boundingBoxHeight * heightScale,
				Mth.lerp(partialTick, oldZ, z));
	}

	static Vec3 playerHandEndpoint(Vec3 eyePosition, Vec3 viewVector, float yawRadians,
			double side, boolean firstPerson) {
		Vec3 right = new Vec3(Mth.cos(yawRadians), 0.0D, Mth.sin(yawRadians));
		double drop = HAND_DOWN_OFFSET + (firstPerson ? 0.0D : THIRD_PERSON_HAND_EXTRA_DROP);
		return eyePosition.add(right.scale(HAND_SIDE_OFFSET * side))
				.add(viewVector.normalize().scale(HAND_FORWARD_OFFSET))
				.add(0.0D, -drop, 0.0D);
	}
}

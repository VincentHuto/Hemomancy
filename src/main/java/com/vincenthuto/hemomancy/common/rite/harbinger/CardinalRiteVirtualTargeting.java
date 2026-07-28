package com.vincenthuto.hemomancy.common.rite.harbinger;

import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Ray-to-marker selection for ritual interaction points that need not occupy a
 * physical block.
 */
public final class CardinalRiteVirtualTargeting {
	public static final double PROJECTION_RANGE = 5.5D;
	public static final double TARGET_RADIUS = 0.75D;

	private CardinalRiteVirtualTargeting() {
	}

	public static int closestTarget(Vec3 eye, Vec3 look, double range, double radius, List<Vec3> targets) {
		if (eye == null || look == null || targets == null || range <= 0.0D || radius < 0.0D) return -1;
		Vec3 direction = look.normalize();
		if (direction.lengthSqr() < 1.0E-9D) return -1;
		int closest = -1;
		double closestDistance = Double.MAX_VALUE;
		double bestAimError = Double.MAX_VALUE;
		double radiusSqr = radius * radius;
		for (int index = 0; index < targets.size(); index++) {
			Vec3 target = targets.get(index);
			if (target == null) continue;
			Vec3 offset = target.subtract(eye);
			double alongRay = offset.dot(direction);
			if (alongRay < 0.0D || alongRay > range) continue;
			Vec3 nearestPoint = eye.add(direction.scale(alongRay));
			double perpendicularDistanceSqr = nearestPoint.distanceToSqr(target);
			if (perpendicularDistanceSqr <= radiusSqr) {
				double aimError = perpendicularDistanceSqr
						/ Math.max(offset.lengthSqr(), 1.0E-9D);
				boolean betterAim = aimError < bestAimError - 1.0E-12D;
				boolean sameAimButCloser = Math.abs(aimError - bestAimError) <= 1.0E-12D
						&& alongRay < closestDistance;
				if (!betterAim && !sameAimButCloser) continue;
				closest = index;
				closestDistance = alongRay;
				bestAimError = aimError;
			}
		}
		return closest;
	}
}

package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class EarthenVeinDestinationTargeting {
	private EarthenVeinDestinationTargeting() {
	}

	public static Optional<EarthenVeinDestinationLayout.Marker> select(
			List<EarthenVeinDestinationLayout.Marker> markers, Vec3 eye, Vec3 look,
			double reach, double radius) {
		Vec3 direction = look.normalize();
		return markers.stream()
				.map(marker -> new Hit(marker, raySphereDistance(eye, direction, marker.center(), radius)))
				.filter(hit -> hit.distance >= 0.0D && hit.distance <= reach)
				.min(Comparator.comparingDouble(Hit::distance))
				.map(Hit::marker);
	}

	private static double raySphereDistance(Vec3 origin, Vec3 direction, Vec3 center, double radius) {
		Vec3 offset = center.subtract(origin);
		double projection = offset.dot(direction);
		if (projection < 0.0D) return -1.0D;
		double perpendicularSquared = offset.lengthSqr() - projection * projection;
		double radiusSquared = radius * radius;
		if (perpendicularSquared > radiusSquared) return -1.0D;
		return Math.max(0.0D, projection - Math.sqrt(radiusSquared - perpendicularSquared));
	}

	private record Hit(EarthenVeinDestinationLayout.Marker marker, double distance) {
	}
}

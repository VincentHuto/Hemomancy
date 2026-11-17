package com.vincenthuto.hemomancy.client.render.world.circus;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.phys.Vec3;

/** A hung wire between two anchors: a parabolic sag of {@code sagPerBlock} times the span at its middle. */
public final class Catenary {
	private Catenary() {
	}

	public static List<Vec3> points(Vec3 a, Vec3 b, double sagPerBlock, double spacing) {
		double span = a.distanceTo(b);
		int segments = Math.max(1, (int) Math.round(span / spacing));
		double sag = sagPerBlock * span;
		List<Vec3> points = new ArrayList<>(segments + 1);
		for (int i = 0; i <= segments; i++) {
			double t = i / (double) segments;
			Vec3 chord = a.lerp(b, t);
			points.add(i == 0 ? a : i == segments ? b : chord.subtract(0.0D, 4.0D * sag * t * (1.0D - t), 0.0D));
		}
		return points;
	}
}

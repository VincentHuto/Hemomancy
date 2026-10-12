package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeamTubeTest {
	private static List<Vec3> seamArc(int samples, int count, double radius) {
		List<Vec3> points = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			double a = i * Math.PI * 2 / samples;
			points.add(new Vec3(Math.cos(a) * radius, 0.15 * Math.sin(i * 1.7), Math.sin(a) * radius));
		}
		return points;
	}

	private static double[] radii(int count) {
		double[] r = new double[count];
		for (int i = 0; i < count; i++) r[i] = 0.14 + 0.26 * Math.abs(Math.sin(i * 12.9898));
		return r;
	}

	/** Matching faces of neighbouring rings stay close: the tube neither twists nor tears between rings. */
	private static void assertUntwisted(Vec3[][] rings, List<Vec3> centres, double[] radii, int from, int to) {
		Vec3 step = centres.get(to).subtract(centres.get(from));
		double allowed = step.length() + Math.abs(radii[from] - radii[to]) + 0.35 * Math.max(radii[from], radii[to]);
		for (int f = 0; f < rings[from].length; f++)
			assertTrue(rings[from][f].distanceTo(rings[to][f]) <= allowed,
					"face " + f + " of rings " + from + "-" + to + " twists by " + rings[from][f].distanceTo(rings[to][f]));
	}

	@Test
	void anOpenArcOfTheSeamIsOneUntwistedTube() {
		List<Vec3> centres = seamArc(96, 40, 13);
		double[] radii = radii(40);
		Vec3[][] rings = SeamTube.rings(centres, radii, 8, false);
		assertEquals(40, rings.length);
		for (int i = 0; i < rings.length - 1; i++) assertUntwisted(rings, centres, radii, i, i + 1);
		for (int i = 0; i < rings.length; i++)
			for (Vec3 vertex : rings[i])
				assertEquals(radii[i], vertex.distanceTo(centres.get(i)), 1e-6, "each ring sits at its own radius");
	}

	@Test
	void aWholeSeamClosesOnItselfWithoutASeam() {
		List<Vec3> centres = seamArc(96, 96, 13);
		double[] radii = radii(96);
		Vec3[][] rings = SeamTube.rings(centres, radii, 8, true);
		assertUntwisted(rings, centres, radii, 95, 0);
	}

	@Test
	void aWanderingVeinStaysWhole() {
		List<Vec3> centres = new ArrayList<>();
		for (int i = 0; i < 9; i++) centres.add(new Vec3(i * 0.35, 0.035, Math.sin(i * 0.9) * 0.22));
		double[] radii = new double[9];
		for (int i = 0; i < 9; i++) radii[i] = 0.11 * (1 - i / 9.0 * 0.8);
		Vec3[][] rings = SeamTube.rings(centres, radii, 8, false);
		for (int i = 0; i < rings.length - 1; i++) assertUntwisted(rings, centres, radii, i, i + 1);
	}
}

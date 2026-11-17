package com.vincenthuto.hemomancy.client.render.world.circus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class CatenaryTest {
	private static final Vec3 A = new Vec3(0, 10, 0);
	private static final Vec3 B = new Vec3(10, 10, 0);

	@Test
	void endsAreTheAnchors() {
		List<Vec3> points = Catenary.points(A, B, 0.08D, 0.6D);
		assertEquals(A, points.get(0));
		assertEquals(B, points.get(points.size() - 1));
	}

	@Test
	void midpointSagsBelowTheChord() {
		List<Vec3> points = Catenary.points(A, B, 0.08D, 0.6D);
		Vec3 mid = points.get(points.size() / 2);
		assertTrue(mid.y < 10.0D - 0.6D, "sagged to " + mid.y);
		for (Vec3 point : points) assertTrue(point.y <= 10.0D + 1.0E-9D);
	}

	@Test
	void spacingSetsTheCount() {
		int count = Catenary.points(A, B, 0.08D, 0.6D).size();
		assertTrue(Math.abs(count - 18) <= 1, "count " + count);
	}

	@Test
	void runsNeedTwoAnchors() {
		var one = new ShowMarks(List.of(), List.of(), List.of(new Vec3(0, 12, 0)), List.of(List.of(new Vec3(4, 6, 4))),
				ShowMarks.EMPTY.ring());
		assertTrue(StringLights.runs(one).isEmpty());
		var two = new ShowMarks(List.of(), List.of(), List.of(new Vec3(0, 12, 0), new Vec3(8, 12, 0)), List.of(),
				ShowMarks.EMPTY.ring());
		assertEquals(1, StringLights.runs(two).size());
		var far = new ShowMarks(List.of(), List.of(), List.of(new Vec3(0, 12, 0), new Vec3(60, 12, 0)), List.of(),
				ShowMarks.EMPTY.ring());
		assertTrue(StringLights.runs(far).isEmpty());
	}
}

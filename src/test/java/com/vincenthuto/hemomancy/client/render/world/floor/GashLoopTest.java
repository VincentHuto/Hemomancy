package com.vincenthuto.hemomancy.client.render.world.floor;

import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteGashRules;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The trench runs round the gash's circle and wraps round the outside of each brazier standing on it, like a moat. */
final class GashLoopTest {
	@Test
	void withNoBraziersTheTrenchIsTheGashsCircle() {
		GashLoop loop = GashLoop.circle(3, List.of());
		double radius = CardinalRiteGashRules.radius(3);
		for (float[] point : loop.centre()) {
			assertEquals(radius, Math.hypot(point[0] - 0.5F, point[1] - 0.5F), 1.0e-4D);
		}
	}

	@Test
	void theTrenchWrapsRoundTheOutsideOfASeatedBrazierAndStaysRoundElsewhere() {
		int half = 3;
		float[] seat = {-2.5F, 0.5F}; // the cell three blocks to the Focus's left, local block centre
		GashLoop loop = GashLoop.circle(half, List.<float[]>of(seat));
		double radius = CardinalRiteGashRules.radius(half);
		double nearestToSeat = Double.MAX_VALUE;
		for (float[] point : loop.centre()) {
			double fromFocus = Math.hypot(point[0] - 0.5F, point[1] - 0.5F);
			assertTrue(fromFocus >= radius - 1.0e-4D, "never cuts in toward the Focus: " + fromFocus);
			assertTrue(fromFocus <= radius + GashLoop.MOAT_OUTERMOST + 1.0e-4D, "bulges only so far: " + fromFocus);
			nearestToSeat = Math.min(nearestToSeat, Math.hypot(point[0] - seat[0], point[1] - seat[1]));
			if (point[0] > 0.5F + 1.0F) assertEquals(radius, fromFocus, 1.0e-3D, "round on the far side");
		}
		assertTrue(nearestToSeat >= GashLoop.MOAT_CLEARANCE - 1.0e-3D, "runs round the brazier's mound: " + nearestToSeat);
		double outward = 0.0D;
		for (float[] point : loop.centre()) {
			if (point[0] < 0.5F - 2.0F) outward = Math.max(outward, Math.hypot(point[0] - 0.5F, point[1] - 0.5F));
		}
		assertTrue(outward > Math.hypot(seat[0] - 0.5F, seat[1] - 0.5F), "the brazier sits inside the trench, on the membrane");
	}

	@Test
	void neighbouringBraziersShareOneSmoothBowRatherThanAddingUp() {
		int half = 3;
		float[] near = {-2.5F, 0.5F};
		float[] beside = {-2.5F, 1.5F};
		GashLoop two = GashLoop.circle(half, List.of(near, beside));
		double alone = Math.max(farthest(GashLoop.circle(half, List.<float[]>of(near))),
				farthest(GashLoop.circle(half, List.<float[]>of(beside))));
		assertEquals(alone, farthest(two), 0.05D, "a neighbour does not push the trench further out than either alone");
		float[][] centre = two.centre();
		for (int i = 1; i < centre.length - 1; i++) {
			double before = Math.atan2(centre[i][1] - centre[i - 1][1], centre[i][0] - centre[i - 1][0]);
			double after = Math.atan2(centre[i + 1][1] - centre[i][1], centre[i + 1][0] - centre[i][0]);
			double turn = Math.abs(Math.IEEEremainder(after - before, Math.PI * 2.0D));
			assertTrue(turn < 0.35D, "no crease at sample " + i + ": " + turn);
		}
	}

	private static double farthest(GashLoop loop) {
		double farthest = 0.0D;
		for (float[] point : loop.centre()) farthest = Math.max(farthest, Math.hypot(point[0] - 0.5F, point[1] - 0.5F));
		return farthest;
	}
}

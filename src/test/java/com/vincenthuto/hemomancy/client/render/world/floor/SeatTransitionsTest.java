package com.vincenthuto.hemomancy.client.render.world.floor;

import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteGashRules;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** A brazier set on or taken off the gash eases the moat and its mound in and out instead of snapping. */
final class SeatTransitionsTest {
	private static final BlockPos CELL = new BlockPos(3, 65, 0);

	@Test
	void braziersPresentWhenTheFloorIsFirstSeenAreAlreadyWhole() {
		SeatTransitions transitions = SeatTransitions.settledWith(Set.of(CELL));
		assertEquals(1.0F, transitions.weight(CELL, 0L));
		assertTrue(transitions.settled(0L));
	}

	@Test
	void aPlacedBrazierGrowsItsMoatInSmoothly() {
		SeatTransitions transitions = SeatTransitions.settledWith(Set.of());
		transitions.target(Set.of(CELL), 100L);
		assertEquals(0.0F, transitions.weight(CELL, 100L), 1.0e-6F);
		assertFalse(transitions.settled(100L));
		float previous = 0.0F;
		for (long tick = 101L; tick <= 100L + SeatTransitions.TICKS; tick++) {
			float weight = transitions.weight(CELL, tick);
			assertTrue(weight >= previous, "grows steadily");
			previous = weight;
		}
		assertEquals(1.0F, transitions.weight(CELL, 100L + SeatTransitions.TICKS), 1.0e-6F);
		assertTrue(transitions.settled(100L + SeatTransitions.TICKS));
	}

	@Test
	void aRemovedBrazierSinksAwayAndAReversalCarriesOnFromWhereItWas() {
		SeatTransitions transitions = SeatTransitions.settledWith(Set.of(CELL));
		transitions.target(Set.of(), 0L);
		assertTrue(transitions.cells().contains(CELL), "a removed brazier's mound lingers while it sinks");
		long mid = SeatTransitions.TICKS / 2;
		float halfway = transitions.weight(CELL, mid);
		assertTrue(halfway > 0.0F && halfway < 1.0F);
		transitions.target(Set.of(CELL), mid);
		assertEquals(halfway, transitions.weight(CELL, mid), 1.0e-6F, "no jump when it is put back");
		assertEquals(1.0F, transitions.weight(CELL, mid + SeatTransitions.TICKS), 1.0e-6F);
		transitions.target(Set.of(), 100L);
		transitions.weight(CELL, 100L + SeatTransitions.TICKS);
		transitions.prune(100L + SeatTransitions.TICKS);
		assertFalse(transitions.cells().contains(CELL), "gone once it has sunk");
	}

	@Test
	void aHalfGrownMoatBulgesPartWay() {
		float[] seat = {-2.5F, 0.5F};
		double radius = CardinalRiteGashRules.radius(3);
		double none = farthest(GashLoop.circle(3, List.<float[]>of(seat), new float[] {0.0F}));
		double half = farthest(GashLoop.circle(3, List.<float[]>of(seat), new float[] {0.5F}));
		double whole = farthest(GashLoop.circle(3, List.<float[]>of(seat), new float[] {1.0F}));
		assertEquals(radius, none, 1.0e-3D, "no bulge yet");
		assertTrue(half > none + 0.1D && half < whole - 0.1D, "part way: " + none + " " + half + " " + whole);
	}

	private static double farthest(GashLoop loop) {
		double farthest = 0.0D;
		for (float[] point : loop.centre()) farthest = Math.max(farthest, Math.hypot(point[0] - 0.5F, point[1] - 0.5F));
		return farthest;
	}
}

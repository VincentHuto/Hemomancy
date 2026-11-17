package com.vincenthuto.hemomancy.common.manipulation.verb;

import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargeting.Hit;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Track 1: which of the things on the look ray the one verb pick chooses. */
class VerbTargetingTest {
	private static final Set<VerbTargetKind> ALL = Set.of(VerbTargetKind.values());

	@Test
	void theNearestWantedThingWins() {
		List<Hit<String>> hits = List.of(
				new Hit<>(VerbTargetKind.BLOCK, 4.0D, "log"),
				new Hit<>(VerbTargetKind.CREATURE, 2.5D, "pig"),
				new Hit<>(VerbTargetKind.ITEMS, 3.0D, "seeds"));
		assertEquals(Optional.of("pig"), VerbTargeting.nearestWanted(hits, ALL, 4.0D));
	}

	@Test
	void kindsTheSculptingDoesNotWantAreSkipped() {
		List<Hit<String>> hits = List.of(
				new Hit<>(VerbTargetKind.BLOCK, 4.0D, "log"),
				new Hit<>(VerbTargetKind.CREATURE, 2.5D, "pig"));
		assertEquals(Optional.of("log"), VerbTargeting.nearestWanted(hits, Set.of(VerbTargetKind.BLOCK), 4.0D));
		assertEquals(Optional.empty(), VerbTargeting.nearestWanted(hits, Set.of(VerbTargetKind.ITEMS), 4.0D));
	}

	@Test
	void nothingBehindTheOccludingBlockIsTaken() {
		List<Hit<String>> hits = List.of(new Hit<>(VerbTargetKind.CREATURE, 5.0D, "pig behind the wall"));
		assertEquals(Optional.empty(), VerbTargeting.nearestWanted(hits, ALL, 3.0D));
	}

	@Test
	void onATieWhatStandsInFrontOfTheSurfaceWins() {
		List<Hit<String>> hits = List.of(
				new Hit<>(VerbTargetKind.BLOCK, 3.0D, "block"),
				new Hit<>(VerbTargetKind.FLUID, 3.0D, "water"),
				new Hit<>(VerbTargetKind.ITEMS, 3.0D, "items"),
				new Hit<>(VerbTargetKind.CREATURE, 3.0D, "creature"));
		assertEquals(Optional.of("creature"), VerbTargeting.nearestWanted(hits, ALL, 3.0D));
		assertEquals(Optional.of("items"), VerbTargeting.nearestWanted(hits,
				Set.of(VerbTargetKind.ITEMS, VerbTargetKind.FLUID, VerbTargetKind.BLOCK), 3.0D));
		assertEquals(Optional.of("water"), VerbTargeting.nearestWanted(hits,
				Set.of(VerbTargetKind.FLUID, VerbTargetKind.BLOCK), 3.0D));
	}

	@Test
	void thePlainPickWantsOnlyBlocks() {
		assertEquals(Set.of(VerbTargetKind.BLOCK), VerbTargetKind.PLAIN);
		assertTrue(VerbTargetKind.of(VerbTargetKind.AIR, VerbTargetKind.FLUID)
				.containsAll(Set.of(VerbTargetKind.AIR, VerbTargetKind.FLUID)));
	}
}

package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.*;
import static org.junit.jupiter.api.Assertions.*;

final class CardinalRiteProblemsTest {
	private static Facts calm(CardinalRitePhase phase) {
		return new Facts(phase, List.of(), 0, 0, true, 0, 0, false);
	}

	private static List<String> keys(List<RiteProblem> problems) {
		return problems.stream().map(RiteProblem::key).toList();
	}

	@Test
	void aReadyRiteHasNoProblems() {
		for (CardinalRitePhase phase : CardinalRitePhase.values()) {
			assertTrue(of(calm(phase)).isEmpty(), phase.name());
		}
	}

	@Test
	void inscriptionNamesEachSleepingSigilThenAlliesThenMedium() {
		List<RiteProblem> problems = of(new Facts(CardinalRitePhase.INSCRIPTION,
				List.of("Bastion", "Hematic Lattice"), 3, 1, false, 0, 0, false));

		assertEquals(List.of(SIGIL_REQUIRED, SIGIL_REQUIRED, ALLIES, MEDIUM), keys(problems));
		assertEquals(List.of("Bastion"), problems.get(0).args());
		assertEquals(List.of("3", "1"), problems.get(2).args());
	}

	@Test
	void sigilsAlliesAndMediumOnlyMatterDuringInscription() {
		assertTrue(of(new Facts(CardinalRitePhase.ORDEAL, List.of("Bastion"), 3, 0, false, 0, 0, false)).isEmpty());
	}

	@Test
	void damagedAnchorsMatterWhileAnchorsCanBeFed() {
		for (CardinalRitePhase phase : List.of(CardinalRitePhase.CONSECRATION, CardinalRitePhase.ORDEAL,
				CardinalRitePhase.STILL_INTERVAL)) {
			assertEquals(List.of(ANCHOR_DAMAGED),
					keys(of(new Facts(phase, List.of(), 0, 0, true, 2, 0, false))), phase.name());
		}
		assertTrue(of(new Facts(CardinalRitePhase.CULMINATION, List.of(), 0, 0, true, 2, 0, false)).isEmpty());
	}

	@Test
	void anOwedSacrificeLeadsUntilTheSeal() {
		List<RiteProblem> problems = of(new Facts(CardinalRitePhase.INSCRIPTION, List.of(), 0, 0, false, 0, 120, false));

		assertEquals(List.of(SACRIFICE, MEDIUM), keys(problems));
		assertEquals(List.of("120"), problems.getFirst().args());
		assertTrue(of(new Facts(CardinalRitePhase.ORDEAL, List.of(), 0, 0, true, 0, 120, false)).isEmpty());
	}

	@Test
	void aHeldFinaleIsReported() {
		assertEquals(List.of(FINALE_HELD),
				keys(of(new Facts(CardinalRitePhase.STILL_INTERVAL, List.of(), 0, 0, true, 0, 0, true))));
	}

	@Test
	void problemsAreCappedAtSix() {
		List<RiteProblem> problems = of(new Facts(CardinalRitePhase.INSCRIPTION,
				List.of("a", "b", "c", "d", "e", "f", "g"), 0, 0, true, 0, 0, false));

		assertEquals(6, problems.size());
	}

	@Test
	void sigilIdsReadAsTitleCase() {
		assertEquals("Hematic Lattice", readable("hematic_lattice"));
		assertEquals("Bastion", readable("bastion"));
		assertEquals("", readable(""));
	}
	@Test
	void aHungryWokenRingIsNamedOutsideConsecration() {
		var ordeal = new CardinalRiteProblems.Facts(CardinalRitePhase.ORDEAL, List.of(), 0, 0, true, 0, 0, false, 3);
		assertEquals(List.of(new CardinalRiteProblems.RiteProblem(CardinalRiteProblems.RING_HUNGERS, List.of("3"))),
				CardinalRiteProblems.of(ordeal));

		var consecration = new CardinalRiteProblems.Facts(CardinalRitePhase.CONSECRATION, List.of(), 0, 0, true, 0, 0,
				false, 3);
		assertTrue(CardinalRiteProblems.of(consecration).isEmpty(), "ring 0 is the phase's own work, not a problem");
		assertEquals(0, new CardinalRiteProblems.Facts(CardinalRitePhase.ORDEAL, List.of(), 0, 0, true, 0, 0, false)
				.hungryRingAnchors(), "the old shape still builds");
	}
}

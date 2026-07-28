package com.vincenthuto.hemomancy.common.rite.sigil;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class CardinalRiteSigilProgressTest {
	private static final List<IchorianSigilDefinition.Node> DIAMOND = List.of(
			new IchorianSigilDefinition.Node(-1.0D, 0.0D),
			new IchorianSigilDefinition.Node(0.0D, -1.0D),
			new IchorianSigilDefinition.Node(1.0D, 0.0D),
			new IchorianSigilDefinition.Node(0.0D, 1.0D));

	@Test
	void oneCompletedNodeDoesNotInventAConnection() {
		assertEquals(List.of(), CardinalRiteSigilProgress.completedConnections(DIAMOND, 1));
	}

	@Test
	void eachAdditionalCorrectNodeAddsOnlyItsCompletedLine() {
		assertEquals(List.of(
				new CardinalRiteSigilProgress.Connection(DIAMOND.get(0), DIAMOND.get(1)),
				new CardinalRiteSigilProgress.Connection(DIAMOND.get(1), DIAMOND.get(2))),
				CardinalRiteSigilProgress.completedConnections(DIAMOND, 3));
	}

	@Test
	void progressBeyondTheShapeIsClampedToAuthoredLines() {
		assertEquals(3, CardinalRiteSigilProgress.completedConnections(DIAMOND, 99).size());
	}
}

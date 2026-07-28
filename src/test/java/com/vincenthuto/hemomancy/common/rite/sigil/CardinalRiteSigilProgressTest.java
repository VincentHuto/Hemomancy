package com.vincenthuto.hemomancy.common.rite.sigil;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

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

	@Test
	void explicitGroundTopologyAppearsOnlyAfterBothEndpointsAreCompleted() {
		IchorianSigilDefinition definition = new IchorianSigilDefinition(
				ResourceLocation.parse("hemomancy:branch"),
				IchorianSigilDefinition.Kind.RESPONSE, 1, 0, "Branch", "Test", 0, 0,
				DIAMOND,
				List.of(
						new IchorianSigilDefinition.Connection(0, 1),
						new IchorianSigilDefinition.Connection(0, 2),
						new IchorianSigilDefinition.Connection(2, 3)),
				Optional.empty());

		assertEquals(0, CardinalRiteSigilProgress.completedConnections(definition, 1).size());
		assertEquals(1, CardinalRiteSigilProgress.completedConnections(definition, 2).size());
		assertEquals(2, CardinalRiteSigilProgress.completedConnections(definition, 3).size());
		assertEquals(3, CardinalRiteSigilProgress.completedConnections(definition, 4).size());
	}

	@Test
	void definitionWithoutExplicitTopologyKeepsSequentialProgress() {
		IchorianSigilDefinition definition = new IchorianSigilDefinition(
				ResourceLocation.parse("hemomancy:legacy"),
				IchorianSigilDefinition.Kind.SUPPORT, 1, 0, "Legacy", "Test", 0, 0, DIAMOND);

		assertEquals(CardinalRiteSigilProgress.completedConnections(DIAMOND, 3),
				CardinalRiteSigilProgress.completedConnections(definition, 3));
	}
}

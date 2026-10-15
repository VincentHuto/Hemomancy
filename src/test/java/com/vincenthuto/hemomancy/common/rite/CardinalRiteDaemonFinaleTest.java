package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRiteDaemonFinaleTest {
	@Test
	void worldRitesSlamTheGroundUnlessTheyReachForTheSky() {
		assertEquals(CardinalRiteDaemonFinale.GROUND, CardinalRiteDaemonFinale.resolve(CardinalRitePurpose.WORLD, null));
		assertEquals(CardinalRiteDaemonFinale.GROUND, CardinalRiteDaemonFinale.resolve(CardinalRitePurpose.WORLD, "ground"));
		assertEquals(CardinalRiteDaemonFinale.SKY, CardinalRiteDaemonFinale.resolve(CardinalRitePurpose.WORLD, "Sky"));
	}

	@Test
	void otherRitesKeepMergingIntoTheCaster() {
		for (CardinalRitePurpose purpose : new CardinalRitePurpose[] {CardinalRitePurpose.SELF,
				CardinalRitePurpose.MAKING, CardinalRitePurpose.BOND, null}) {
			assertEquals(CardinalRiteDaemonFinale.MERGE, CardinalRiteDaemonFinale.resolve(purpose, null));
		}
	}

	@Test
	void onlyWorldRitesMayNameAFinale() {
		assertNull(CardinalRiteDaemonFinale.violation(CardinalRitePurpose.WORLD, "sky"));
		assertNull(CardinalRiteDaemonFinale.violation(CardinalRitePurpose.SELF, null));
		assertNotNull(CardinalRiteDaemonFinale.violation(CardinalRitePurpose.SELF, "sky"));
		assertNotNull(CardinalRiteDaemonFinale.violation(CardinalRitePurpose.WORLD, "underground"));
	}
}

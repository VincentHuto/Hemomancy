package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRiteRingStateTest {
	static final ResourceLocation PLAIN = ResourceLocation.parse("hemomancy:cardinal_rite/ring_test");
	static final int[] TWO_RINGS = {0, 0, 0, 0, 1, 1, 1, 1};

	static ActiveCardinalRite rite(int anchors, int waves) {
		return ActiveCardinalRite.interactive(UUID.randomUUID(), BlockPos.ZERO, PLAIN, 1200, 5, 5, false, waves, anchors);
	}

	static ActiveCardinalRite twoRings(int waves) {
		ActiveCardinalRite rite = rite(8, waves);
		rite.configureRings(TWO_RINGS, false);
		return rite;
	}

	@Test
	void anUnconfiguredRiteKeepsEveryRingOpenAtTheFlatCost() {
		ActiveCardinalRite rite = rite(8, 1);

		assertFalse(rite.ringsGated());
		assertEquals(2, rite.getOpenRings());
		assertTrue(rite.isAnchorOpen(5));
		assertEquals(50, rite.anchorRequirementMl(5));
		assertTrue(rite.fillAnchor(5, 50));
	}

	@Test
	void laterRingsSleepUntilWoken() {
		ActiveCardinalRite rite = twoRings(1);

		assertTrue(rite.ringsGated());
		assertEquals(1, rite.getOpenRings());
		assertFalse(rite.isAnchorOpen(4));
		assertFalse(rite.fillAnchor(4, 63), "a sleeping anchor takes no blood");
		assertEquals(0, rite.bloodNeededForAnchor(4));
		assertEquals(0, rite.drainAnchor(4, 5));
		for (int i = 0; i < 4; i++) rite.fillAnchor(i, 50);
		assertTrue(rite.areAnchorsConsecrated(), "only open anchors count");
		assertEquals(1, rite.completedRings());
		assertArrayEquals(new int[] {50, 50, 50, 50, 0, 0, 0, 0}, rite.anchorRequirements());
		assertEquals(63, rite.anchorRequirementMl(4), "the cost is the rank's, asleep or not");
		assertEquals(200, rite.openRequirementMl());
		assertEquals(452, rite.fullRequirementMl());
		assertEquals(200, rite.committedAnchorBloodMl());
	}

	@Test
	void ringsFollowTheAuthoredValueNotTheIndex() {
		ActiveCardinalRite rite = rite(4, 0);
		rite.configureRings(new int[] {1, 0, 1, 0}, false);

		assertTrue(rite.isAnchorOpen(1));
		assertFalse(rite.isAnchorOpen(0));
		assertEquals(2, rite.totalAnchorRings());
		rite.fillAnchor(1, 50);
		rite.fillAnchor(3, 50);
		assertTrue(rite.areAnchorsConsecrated());
	}

	@Test
	void sleepingAnchorsAddNoDeficitAndNeverBreak() {
		ActiveCardinalRite rite = twoRings(1);
		for (int i = 0; i < 4; i++) rite.fillAnchor(i, 50);
		assertEquals(0, rite.anchorDeficitInstability());

		rite.setInstabilityDamagePriority(new int[] {7, 6, 5, 4, 3, 2, 1, 0});
		rite.addInstability(90);
		for (int anchor = 4; anchor < 8; anchor++) {
			assertFalse(rite.isInstabilityDamagedAnchor(anchor), "sleeping anchor " + anchor + " broke");
		}
		assertTrue(rite.getBrokenInstabilityAnchors().stream().allMatch(index -> index < 4));
	}

	@Test
	void deficitPressureReadsEachAnchorsOwnRequirement() {
		assertEquals(0, CardinalRiteCeremonyRules.anchorDeficitInstability(new int[] {63, 0}, new int[] {63, 0}));
		assertEquals(2, CardinalRiteCeremonyRules.anchorDeficitInstability(new int[] {50}, new int[] {63}));
		assertEquals(CardinalRiteCeremonyRules.anchorDeficitInstability(new int[] {20}),
				CardinalRiteCeremonyRules.anchorDeficitInstability(new int[] {20}, null), "flat 50 without requirements");
	}

	@Test
	void ringStateSurvivesASave() {
		ActiveCardinalRite rite = twoRings(1);
		for (int i = 0; i < 3; i++) rite.fillAnchor(i, 50);

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize());

		assertTrue(copy.ringsGated());
		assertEquals(1, copy.getOpenRings());
		assertFalse(copy.isAnchorOpen(4));
		assertEquals(63, copy.anchorRequirementMl(4));
		assertEquals(-1, copy.nextRingToAnnounce(), "ring 0 is never announced");
	}

	@Test
	void anOlderSaveOpensEveryRingAtTheFlatCost() {
		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite(8, 1).serialize());

		assertFalse(copy.ringsGated());
		assertEquals(2, copy.getOpenRings());
		assertEquals(50, copy.anchorRequirementMl(7));
	}

	@Test
	void balancingNeverPoursBloodIntoASleepingRing() {
		ActiveCardinalRite rite = twoRings(1);
		rite.fillAnchor(0, 50);
		rite.fillAnchor(1, 10);
		rite.balanceAnchors();

		int[] blood = rite.getAnchorBloodMl();
		assertEquals(60, blood[0] + blood[1] + blood[2] + blood[3]);
		for (int anchor = 4; anchor < 8; anchor++) assertEquals(0, blood[anchor]);
	}
}

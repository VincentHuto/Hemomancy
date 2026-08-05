package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteProgressionPolicyTest {
	@Test
	void initiationAllowsOnlyThePrebuiltConsentLesson() {
		assertTrue(CardinalRiteProgressionPolicy.violations(
				"cardinal_rite/sanguine_initiation", 0,
				ceremony(0, 0, 0, "temple_medium", "none", false, false,
						0, "safe_retry"), 0).isEmpty());
		assertFalse(CardinalRiteProgressionPolicy.violations(
				"cardinal_rite/sanguine_initiation", 0,
				ceremony(4, 1, 1, "living_staff", "faint", true, false,
						1, "offering_loss"), 1).isEmpty());
	}

	@Test
	void firstSelfBuiltRitesTeachOnlyMediumAndFourAnchors() {
		assertTrue(CardinalRiteProgressionPolicy.violations(
				"cardinal_rite/votary_rite", 1,
				ceremony(4, 0, 0, "hematic_medium", "none", false, false,
						0, "safe_retry"), 0).isEmpty());
		assertFalse(CardinalRiteProgressionPolicy.violations(
				"cardinal_rite/votary_rite", 1,
				ceremony(4, 1, 0, "hematic_medium", "none", false, false,
						0, "safe_retry"), 0).isEmpty());
	}

	@Test
	void ordealsLightningAndHelpersCannotAppearBeforeDegreeFive() {
		assertFalse(CardinalRiteProgressionPolicy.violations(
				"cardinal_rite/illuminatus_rite", 4,
				ceremony(4, 1, 1, "living_staff", "dense", true, false,
						1, "offering_loss"), 2).isEmpty());
		assertTrue(CardinalRiteProgressionPolicy.violations(
				"cardinal_rite/sanctified_rite", 5,
				ceremony(8, 3, 1, "living_staff", "storm", true, false,
						0, "fragile_damage"), 3).isEmpty());
	}

	@Test
	void grandTierAllowsDomeRequiredAidAndTwelveAnchors() {
		assertTrue(CardinalRiteProgressionPolicy.violations(
				"cardinal_rite/archon_rite", 6,
				ceremony(12, 5, 3, "living_staff", "storm", true, true,
						1, "collapse"), 5).isEmpty());
	}

	private static CardinalRiteCeremonyDefinition ceremony(int anchors, int sockets, int waves,
			String focus, String fog, boolean lightning, boolean dome,
			int helpers, String failure) {
		List<CardinalRiteCeremonyDefinition.Anchor> anchorList =
				java.util.stream.IntStream.range(0, anchors)
						.mapToObj(i -> new CardinalRiteCeremonyDefinition.Anchor(i, 1, 0, i / 4, i % 4))
						.toList();
		List<CardinalRiteCeremonyDefinition.SupportSocket> socketList =
				java.util.stream.IntStream.range(0, sockets)
						.mapToObj(i -> new CardinalRiteCeremonyDefinition.SupportSocket(
								i, 0, 1, "sigil_" + i, i == 0))
						.toList();
		List<String> waveList = java.util.stream.IntStream.range(0, waves)
				.mapToObj(i -> "wave_" + i).toList();
		return new CardinalRiteCeremonyDefinition(
				CardinalRiteCeremonyProfile.STANDARD, anchorList, socketList, waveList, List.of(),
				List.of(BlockPos.ZERO), 200, focus, helpers, List.of(),
				waves == 0 ? 0 : 100,
				new CardinalRiteCeremonyDefinition.Atmosphere(fog, lightning, dome), failure);
	}
}

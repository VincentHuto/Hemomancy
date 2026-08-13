package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

final class ChamberProgressionRulesTest {
	@Test
	void mapsEveryProgressionTierAndRadius() {
		assertState(facts(0, false, false, false), 0, "will_default", 4);
		assertState(facts(4, true, false, false), 0, "mnemonic_lowtide", 4);
		assertState(facts(7, false, false, false), 1, "archon_revelation", 6);
		assertState(facts(6, false, true, false), 2, "qliphoth_communion", 8);
		assertState(facts(7, false, false, true), 2, "silent_archon", 8);
		assertState(facts(8, false, false, false), 3, "apotheos", 10);
	}

	@Test
	void refreshReportsTierRadiusAndThemeChangesIndependently() {
		var oldState = new ChamberProgressionRules.State(0, "will_default");
		var tierChange = ChamberProgressionRules.compare(oldState,
				new ChamberProgressionRules.State(1, "mnemonic_lowtide"));
		assertTrue(tierChange.tierChanged());
		assertTrue(tierChange.radiusIncreased());
		assertTrue(tierChange.themeChanged());

		var themeOnly = ChamberProgressionRules.compare(
				new ChamberProgressionRules.State(2, "qliphoth_communion"),
				new ChamberProgressionRules.State(2, "silent_archon"));
		assertFalse(themeOnly.tierChanged());
		assertFalse(themeOnly.radiusIncreased());
		assertTrue(themeOnly.themeChanged());
	}

	@Test
	void availableThemesAreStableFilteredAndExcludeEncounters() {
		assertEquals(List.of("will_default"),
				ChamberProgressionRules.availableThemes(facts(0, false, false, false)));
		assertEquals(List.of("will_default", "mnemonic_lowtide", "archon_revelation",
					"qliphoth_communion", "silent_archon", "apotheos"),
				ChamberProgressionRules.availableThemes(facts(8, true, true, true)));
		assertFalse(ChamberProgressionRules.availableThemes(facts(8, true, true, true))
				.contains("vesper_fight"));
		assertFalse(ChamberProgressionRules.availableThemes(facts(8, true, true, true))
				.contains("mycophant_nursery"));
	}

	@Test
	void lowtideUnlocksFromLearningFirstScarRatherThanDegreeSix() {
		assertEquals(List.of("will_default", "mnemonic_lowtide"),
				ChamberProgressionRules.availableThemes(facts(4, true, false, false)));
		assertEquals(List.of("will_default"),
				ChamberProgressionRules.availableThemes(facts(6, false, false, false)));
	}

	private static ChamberProgressionRules.Facts facts(int degree, boolean veinMason,
			boolean qliphothStarted, boolean silentArchon) {
		return new ChamberProgressionRules.Facts(degree, veinMason, qliphothStarted, silentArchon);
	}

	private static void assertState(ChamberProgressionRules.Facts facts, int tier,
			String theme, int radius) {
		var state = ChamberProgressionRules.stateFor(facts);
		assertEquals(tier, state.tier());
		assertEquals(theme, state.theme());
		assertEquals(radius, ChamberProgressionRules.radiusForTier(state.tier()));
	}
}

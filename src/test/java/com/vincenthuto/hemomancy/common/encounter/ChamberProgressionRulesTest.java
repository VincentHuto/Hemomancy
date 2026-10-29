package com.vincenthuto.hemomancy.common.encounter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class ChamberProgressionRulesTest {
	@Test
	void mapsEveryProgressionTierAndRadius() {
		assertState(facts(0, false, false, false, false), 0, "will_default", 4);
		assertState(facts(4, true, false, false, false), 0, "mnemonic_lowtide", 4);
		assertState(facts(7, false, false, false, false), 1, "archon_revelation", 6);
		assertState(facts(6, false, true, false, false), 2, "qliphoth_communion", 8);
		assertState(facts(8, false, true, true, false), 3, "silent_archon", 10);
		assertState(facts(8, false, true, false, true), 3, "apotheos", 10);
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
				ChamberProgressionRules.availableThemes(facts(0, false, false, false, false)));
		assertEquals(List.of("will_default", "mnemonic_lowtide", "archon_revelation",
					"qliphoth_communion", "silent_archon"),
				ChamberProgressionRules.availableThemes(facts(8, true, true, true, false)));
		assertEquals(List.of("will_default", "mnemonic_lowtide", "archon_revelation",
					"qliphoth_communion", "apotheos"),
				ChamberProgressionRules.availableThemes(facts(8, true, true, false, true)));
		assertFalse(ChamberProgressionRules.availableThemes(facts(8, true, true, false, true))
				.contains("vesper_fight"));
		assertFalse(ChamberProgressionRules.availableThemes(facts(8, true, true, false, true))
				.contains("mycophant_nursery"));
	}

	@Test
	void lowtideUnlocksFromLearningFirstScarRatherThanDegreeSix() {
		assertEquals(List.of("will_default", "mnemonic_lowtide"),
				ChamberProgressionRules.availableThemes(facts(4, true, false, false, false)));
		assertEquals(List.of("will_default"),
				ChamberProgressionRules.availableThemes(facts(6, false, false, false, false)));
	}

	private static ChamberProgressionRules.Facts facts(int degree, boolean veinMason,
			boolean qliphothStarted, boolean silentium, boolean apotheos) {
		return new ChamberProgressionRules.Facts(degree, veinMason, qliphothStarted, silentium, apotheos);
	}

	private static void assertState(ChamberProgressionRules.Facts facts, int tier,
			String theme, int radius) {
		var state = ChamberProgressionRules.stateFor(facts);
		assertEquals(tier, state.tier());
		assertEquals(theme, state.theme());
		assertEquals(radius, ChamberProgressionRules.radiusForTier(state.tier()));
	}
}

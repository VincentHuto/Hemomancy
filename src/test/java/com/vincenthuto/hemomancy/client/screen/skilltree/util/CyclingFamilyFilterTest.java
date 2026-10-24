package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import com.vincenthuto.hemomancy.support.EnglishText;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class CyclingFamilyFilterTest {
	@Test
	void selectedFamiliesUseTheNicknameWithoutTheLongFamilyPrefix() {
		assertEquals("Family: All", EnglishText.render(FamilyFilterLabels.label(null)));
		assertEquals("Animus", FamilyFilterLabels.display("Animus"));
	}

	@Test
	void recipeAndSkillFamiliesHaveCompactAuthoredNicknames() {
		assertEquals("Staff", EnglishText.render(FamilyFilterLabels.nickname("living_staff")));
		assertEquals("Bloodline", EnglishText.render(FamilyFilterLabels.nickname("Bloodline/Fane")));
		assertEquals("Qliphoth", EnglishText.render(FamilyFilterLabels.nickname("Qliphoth/Forbidden")));
		assertEquals("Floors", EnglishText.render(FamilyFilterLabels.nickname("Ritual Floors")));
		assertEquals("Ritual", EnglishText.render(FamilyFilterLabels.nickname("Ritual Infrastructure")));
		assertEquals("Constructs", EnglishText.render(FamilyFilterLabels.nickname("Constructs/Effigies")));
		assertEquals("Order", EnglishText.render(FamilyFilterLabels.nickname("Order")));
	}

	@Test
	void cyclesFromAllThroughEveryFamilyAndBackToAll() {
		CyclingFamilyFilter<String> filter = new CyclingFamilyFilter<>(List.of("Fervent", "Umbral"));

		assertNull(filter.selected());
		filter.cycle(1);
		assertEquals("Fervent", filter.selected());
		filter.cycle(1);
		assertEquals("Umbral", filter.selected());
		filter.cycle(1);
		assertNull(filter.selected());
		filter.cycle(-1);
		assertEquals("Umbral", filter.selected());
	}

	@Test
	void includesEveryEntryForAllAndOnlyTheSelectedFamilyOtherwise() {
		CyclingFamilyFilter<String> filter = new CyclingFamilyFilter<>(List.of("Blocks", "Reagents"));
		assertTrue(filter.includes("Blocks"));
		assertTrue(filter.includes("Reagents"));

		filter.cycle(1);
		assertTrue(filter.includes("Blocks"));
		assertFalse(filter.includes("Reagents"));
	}

	@Test
	void resettingOptionsDropsASelectionThatNoLongerExists() {
		CyclingFamilyFilter<String> filter = new CyclingFamilyFilter<>(List.of("Blocks", "Reagents"));
		filter.cycle(1);

		filter.setOptions(List.of("Equipment"));

		assertNull(filter.selected());
	}
}

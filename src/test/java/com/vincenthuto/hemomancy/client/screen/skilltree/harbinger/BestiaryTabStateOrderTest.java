package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class BestiaryTabStateOrderTest {

	private static BestiaryTabState.Entry entry(BestiaryTabState.Kind kind, String key, boolean discovered) {
		return new BestiaryTabState.Entry(kind, key, key + ".title", key + ".description", key + ".source",
				"", discovered, false);
	}

	@Test
	void specimensComeFirstThenMorphlingsEachAlphabeticalByTitle() {
		Map<String, String> titles = Map.of(
				"hemomancy:verdigris_moth.title", "Verdigris Moth",
				"hemomancy:barbed_urchin.title", "Barbed Urchin",
				"hemomancy:chiton.title", "chiton",
				"bat.title", "Witch's Ear Strain",
				"mole.title", "Irontooth Strain");
		List<BestiaryTabState.Entry> entries = new ArrayList<>(List.of(
				entry(BestiaryTabState.Kind.MORPHLING, "bat", true),
				entry(BestiaryTabState.Kind.SPECIMEN, "hemomancy:verdigris_moth", true),
				entry(BestiaryTabState.Kind.SPECIMEN, "hemomancy:barbed_urchin", false),
				entry(BestiaryTabState.Kind.MORPHLING, "mole", false),
				entry(BestiaryTabState.Kind.SPECIMEN, "hemomancy:chiton", true)));

		entries.sort(BestiaryTabState.displayOrder(titles::get));

		assertEquals(List.of("hemomancy:barbed_urchin", "hemomancy:chiton", "hemomancy:verdigris_moth", "mole", "bat"),
				entries.stream().map(BestiaryTabState.Entry::key).toList());
	}

	@Test
	void dossierScrollStopsAtTheLastLine() {
		BestiaryTabState state = new BestiaryTabState();
		state.infoContentH = 500;
		state.infoVisibleH = 190;
		state.infoScroll = 900;
		state.clampInfoScroll();
		assertEquals(310, state.infoScroll);

		state.infoScroll = -40;
		state.clampInfoScroll();
		assertEquals(0, state.infoScroll);

		state.infoContentH = 120;
		state.infoScroll = 30;
		state.clampInfoScroll();
		assertEquals(0, state.infoScroll, "short dossiers do not scroll");
	}
}

package com.vincenthuto.hemomancy.common.world;

import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GardensNetworkRespondersTagTest {
	@Test
	void theNetworkCallsItsOwnMonstersAndNeverAPlayersPuppets() {
		String tag = ResourceReader.text("data/hemomancy/tags/entity_type/gardens_network_responders.json");
		for (String wild : List.of("abhorrent_thought", "fargone", "facsimile_spider", "erythromycelium_eruptus")) {
			assertTrue(tag.contains("hemomancy:" + wild), wild);
		}
		for (String bound : List.of("marrow_spitter", "veinwing_vulture", "will", "gorebound_hulk", "antibody")) {
			assertFalse(tag.contains("\"hemomancy:" + bound + "\""), bound);
		}
	}
}

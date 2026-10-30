package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Rite of Possession is a Sanguine Quintessence on a Qliphoth floor, tuned by whatever its braziers hold. */
final class PossessionRiteDataTest {
	private static final JsonObject RITE = ResourceReader.data("recipe/cardinal_rite/possession_rite.json");

	@Test
	void theRiteIsAQuintessenceOnTheQliphothFloor() {
		assertEquals("hemomancy:qliphoth_grand", RITE.get("floor").getAsString());
		assertEquals("hemomancy:sanguine_quintessence", RITE.getAsJsonObject("medium").get("item").getAsString());
		assertTrue(RITE.get("special").getAsBoolean());
		assertEquals("bond", RITE.get("purpose").getAsString());
		assertFalse(RITE.has("brazier_signature"), "its braziers are read for optional offerings, not matched");
	}

	@Test
	void everyOptionalOfferingNamesAKnownEffectOnce() {
		Set<String> effects = new HashSet<>();
		for (JsonElement element : RITE.getAsJsonArray("optional_offerings")) {
			String effect = element.getAsJsonObject().get("effect").getAsString();
			assertTrue(PossessionRules.OFFERING_EFFECTS.contains(effect), effect);
			assertTrue(effects.add(effect), effect + " is offered twice");
		}
		assertEquals(PossessionRules.OFFERING_EFFECTS, effects);
	}
}

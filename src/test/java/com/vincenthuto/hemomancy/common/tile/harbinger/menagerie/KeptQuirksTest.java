package com.vincenthuto.hemomancy.common.tile.harbinger.menagerie;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Core-loop round two P6: a studied kind's quirks are notes read from rules that already hold. */
class KeptQuirksTest {
	@Test
	void aBlazeNeedsTheSyringeRattlesForcesTheGlassAndBreeds() {
		var facts = new KeptQuirks.Facts(true, 20.0F, true, false, true, null);
		assertEquals(List.of("syringe", "upkeep", "vat", "rattles", "breaks_out", "breeds"), KeptQuirks.ids(facts));
		var upkeep = KeptQuirks.of(facts).get(1);
		assertEquals(VatVigorRules.mlPerVigor(20.0F), upkeep.value(), 1.0E-9D);
		assertEquals(BroodRules.broodCost(20.0F), KeptQuirks.of(facts).get(5).value(), 1.0E-9D);
	}

	@Test
	void aWeakHostileRattlesButCannotForceTheGlass() {
		var facts = new KeptQuirks.Facts(true, 12.0F, false, true, true, null);
		assertEquals(List.of("upkeep", "jar", "rattles", "breeds"), KeptQuirks.ids(facts));
	}

	@Test
	void aCalfWithAnInheritedSchoolSaysSoAndTheDeadDoNotBreed() {
		var calf = new KeptQuirks.Facts(false, 10.0F, false, false, true, EnumBloodTemper.LUX);
		assertEquals(List.of("upkeep", "vat", "breeds", "inherited"), KeptQuirks.ids(calf));
		assertEquals(EnumBloodTemper.LUX, KeptQuirks.of(calf).get(3).school());
		var husk = new KeptQuirks.Facts(true, 20.0F, false, false, false, null);
		assertEquals("no_brood", KeptQuirks.ids(husk).getLast());
		assertEquals("message.hemomancy.menagerie.quirk.no_brood", KeptQuirks.of(husk).getLast().key());
	}
}

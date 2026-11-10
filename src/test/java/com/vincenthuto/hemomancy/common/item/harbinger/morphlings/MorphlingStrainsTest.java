package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayer;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** The Brined Court strain table (Track 1): eight rows, settled ids, tempers, Cradle set and Last Rites. */
final class MorphlingStrainsTest {
	@Test
	void eightRowsWithTheSettledIdsShallowToDeep() {
		assertEquals(List.of("sunburst", "holdfast", "mermaids_purse", "dead_mans_rope", "ink_cap", "hushbell",
				"marrowplume", "kinbud"), MorphlingStrains.ALL.stream().map(MorphlingStrain::id).toList());
		assertEquals(8, new HashSet<>(MorphlingStrains.itemPaths()).size(), "item ids must be unique");
		for (int i = 0; i < MorphlingStrains.ALL.size(); i++) {
			MorphlingStrain strain = MorphlingStrains.ALL.get(i);
			assertEquals(i, strain.creativeOrder(), strain.id() + " creative order follows the table");
			assertEquals(CourtHouse.values()[i], strain.house(), strain.id() + " keeps its house depth slot");
			assertEquals("morphling_" + strain.id(), strain.itemPath());
		}
	}

	@Test
	void everyTemperIsPreferredExactlyOnceAndSecondariesFollowTheGradient() {
		Set<EnumBloodTemper> preferred = MorphlingStrains.ALL.stream().map(MorphlingStrain::preferred)
				.collect(Collectors.toCollection(() -> EnumSet.noneOf(EnumBloodTemper.class)));
		assertEquals(EnumSet.allOf(EnumBloodTemper.class), preferred);
		assertTempers(MorphlingStrains.SUNBURST, EnumBloodTemper.LUX, EnumBloodTemper.FERRIC);
		assertTempers(MorphlingStrains.HOLDFAST, EnumBloodTemper.FERRIC, EnumBloodTemper.ANIMUS);
		assertTempers(MorphlingStrains.MERMAIDS_PURSE, EnumBloodTemper.ANIMUS, EnumBloodTemper.DUCTILIS);
		assertTempers(MorphlingStrains.DEAD_MANS_ROPE, EnumBloodTemper.DUCTILIS, EnumBloodTemper.TENEBRIS);
		assertTempers(MorphlingStrains.INK_CAP, EnumBloodTemper.TENEBRIS, EnumBloodTemper.DUCTILIS);
		assertTempers(MorphlingStrains.HUSHBELL, EnumBloodTemper.CONGEATIO, EnumBloodTemper.TENEBRIS);
		assertTempers(MorphlingStrains.MARROWPLUME, EnumBloodTemper.MORTEM, EnumBloodTemper.CONGEATIO);
		assertTempers(MorphlingStrains.KINBUD, EnumBloodTemper.FLAMMEUS, EnumBloodTemper.MORTEM);
	}

	@Test
	void cradleHostsTheFiveSupportStrainsAtPrimal() {
		assertEquals(Set.of("sunburst", "holdfast", "mermaids_purse", "hushbell", "marrowplume"),
				MorphlingStrains.ALL.stream().filter(MorphlingStrain::cradlePrimal).map(MorphlingStrain::id)
						.collect(Collectors.toSet()));
	}

	@Test
	void lastRitesArmOnlyOnInkCapApexAndHushbellPrimal() {
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			if (strain == MorphlingStrains.INK_CAP) {
				assertEquals(LastRiteHelper.INK_MANTLE_ID, strain.lastRiteId());
				assertEquals(PrimalMorphlingRules.APEX_LEVEL, strain.lastRiteStage());
			} else if (strain == MorphlingStrains.HUSHBELL) {
				assertEquals(LastRiteHelper.CRYPTOBIOSIS_ID, strain.lastRiteId());
				assertEquals(PrimalMorphlingRules.PRIMAL_LEVEL, strain.lastRiteStage());
			} else {
				assertFalse(strain.hasLastRite(), strain.id() + " must not arm a Last Rite");
			}
		}
	}

	@Test
	void eachStrainOwnsOnePartnerFamilyAndAccent() {
		assertEquals(MorphlingPolypLayer.values().length, MorphlingStrains.ALL.size());
		for (MorphlingPolypLayer family : MorphlingPolypLayer.values()) {
			assertTrue(MorphlingStrains.byPolypFamily(family).isPresent(), family + " has no strain");
		}
		assertEquals(8, MorphlingStrains.ALL.stream().map(MorphlingStrain::accentColor).distinct().count(),
				"HUD accents must tell the strains apart");
	}

	private static void assertTempers(MorphlingStrain strain, EnumBloodTemper preferred,
			EnumBloodTemper secondary) {
		assertEquals(preferred, strain.preferred(), strain.id() + " preferred");
		assertEquals(secondary, strain.secondary(), strain.id() + " secondary");
	}
}

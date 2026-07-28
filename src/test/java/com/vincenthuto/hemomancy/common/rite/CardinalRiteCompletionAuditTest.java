package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteThreatRules;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;

import static org.junit.jupiter.api.Assertions.*;

class CardinalRiteCompletionAuditTest {
	private static final List<String> HARBINGER_RITES = List.of(
			"ancestral_communion", "apotheos_rite", "archon_rite", "ashen_vessel_rite",
			"bloodline_founding", "bloodline_recall", "bloom_of_qliphoth", "chamber_of_will",
			"crimson_beacon", "crimson_vessel_rite", "eternal_covenant", "exsanguination",
			"founding_fane", "hematic_fortification", "hematic_unbinding",
			"horn_of_culmination_rite", "hungering_earth", "illuminatus_rite", "initiate_rite",
			"pallid_shadow", "pallid_vessel_rite", "pruning_of_qliphoth", "sanctified_rite",
			"sanguine_attunement", "sanguine_brotherhood", "sanguine_dominion",
			"sanguine_eclipse", "sanguine_fervor", "sanguine_initiation", "scarlet_summons",
			"vascular_mending", "votary_rite");

	@Test
	void everyHarbingerRiteHasAnExplicitAuthoredCeremonySpec() {
		for (String path : HARBINGER_RITES) {
			assertTrue(CardinalRiteCeremonyCatalog.hasAuthoredSpec(path), path);
		}
	}

	@Test
	void authoredDiagonalAnchorsRemainInsideTheirCompletedRing() {
		CardinalRiteCeremonyDefinition ceremony = CardinalRiteCeremonyDefinition.convertedDefault(
				ResourceLocation.parse("hemomancy:cardinal_rite/sanctified_rite"),
				CardinalRiteType.GREATER, 6);
		double farthest = ceremony.anchors().stream()
				.mapToDouble(anchor -> Math.hypot(anchor.x(), anchor.z())).max().orElseThrow();
		assertTrue(farthest <= 8.5D, "farthest=" + farthest);
	}

	@Test
	void completedSupportSigilsProvideRealReservoirAndLatticeState() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), BlockPos.ZERO,
				ResourceLocation.parse("hemomancy:cardinal_rite/test"), 200, 5, 2, false, 2);
		rite.setSigilProgress("hemomancy:reservoir", 3);
		assertFalse(rite.isSigilComplete("hemomancy:reservoir", 4));
		rite.setSigilProgress("hemomancy:reservoir", 4);
		assertTrue(rite.isSigilComplete("hemomancy:reservoir", 4));
		assertEquals(200, rite.storeReservoirBlood(250, 200));
		assertEquals(50, rite.drawReservoirBlood(50));

		rite.fillAnchor(0, 50);
		rite.fillAnchor(1, 10);
		rite.balanceAnchors();
		assertArrayEquals(new int[] { 15, 15, 15, 15, 0, 0, 0, 0 }, rite.getAnchorBloodMl());
	}

	@Test
	void attendantCanCatchOnlyOneMissPerWave() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), BlockPos.ZERO,
				ResourceLocation.parse("hemomancy:cardinal_rite/test"), 200, 9, 7, false, 6);
		UUID attendant = UUID.randomUUID();
		for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i, 50);
		assertTrue(rite.enterInscription());
		assertTrue(rite.sealAltar());
		assertTrue(rite.tryUseAttendantCatch(attendant));
		assertFalse(rite.tryUseAttendantCatch(attendant));
		rite.completeWave();
		assertTrue(rite.tryUseAttendantCatch(attendant));
	}

	@Test
	void threatPressureRequiresPhysicalContactRange() {
		assertTrue(CardinalRiteThreatRules.canSiphonAnchor(2.24D));
		assertFalse(CardinalRiteThreatRules.canSiphonAnchor(2.26D));
		assertTrue(CardinalRiteThreatRules.canDrainCaster(8.99D));
		assertFalse(CardinalRiteThreatRules.canDrainCaster(9.01D));
	}

	@Test
	void depletedAnchorsGenerateInstabilityUntilRepaired() {
		assertEquals(6, CardinalRiteCeremonyRules.anchorDeficitInstability(
				new int[] { 50, 49, 0, 50 }));
		assertEquals(0, CardinalRiteCeremonyRules.anchorDeficitInstability(
				new int[] { 50, 50, 50, 50 }));
	}

	@Test
	void repeatedResponseSigilsHaveWaveScopedProgress() {
		assertEquals("wave:2:hemomancy:suture",
				CardinalRiteSigilRules.responseProgressKey(2,
						ResourceLocation.parse("hemomancy:suture")));
	}
}

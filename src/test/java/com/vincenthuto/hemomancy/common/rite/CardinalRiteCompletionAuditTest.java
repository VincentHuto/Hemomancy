package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteThreatRules;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CardinalRiteCompletionAuditTest {
	@Test
	void authoredDiagonalAnchorsRemainInsideTheirCompletedRing() {
		double farthest = CardinalRiteCeremonyDefinition.anchorsForLayout(
						3, 0, CardinalRiteCeremonyCatalog.Layout.DIAGONAL).stream()
				.mapToDouble(anchor -> Math.hypot(anchor.x(), anchor.z())).max().orElseThrow();
		assertTrue(farthest <= 8.5D, "farthest=" + farthest);
	}

	@Test
	void completedSupportSigilsProvideRealReservoirAndLatticeState() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), BlockPos.ZERO,
				net.minecraft.resources.ResourceLocation.parse("hemomancy:cardinal_rite/test"), 200, 5, 2, false, 2);
		rite.setSigilProgress("hemomancy:reservoir", 3);
		assertFalse(rite.isSigilComplete("hemomancy:reservoir", 4));
		rite.setSigilProgress("hemomancy:reservoir", 4);
		assertTrue(rite.isSigilComplete("hemomancy:reservoir", 4));
		assertEquals(200, rite.storeReservoirBlood(250, 200));
		assertEquals(50, rite.drawReservoirBlood(50));

		rite.fillAnchor(0, 50);
		rite.fillAnchor(1, 10);
		rite.balanceAnchors();
		assertArrayEquals(new int[] { 15, 15, 15, 15 }, rite.getAnchorBloodMl());
	}

	@Test
	void attendantCanCatchOnlyOneMissPerWave() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), BlockPos.ZERO,
				net.minecraft.resources.ResourceLocation.parse("hemomancy:cardinal_rite/test"), 200, 9, 7, false, 6);
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
						net.minecraft.resources.ResourceLocation.parse("hemomancy:suture")));
	}
}

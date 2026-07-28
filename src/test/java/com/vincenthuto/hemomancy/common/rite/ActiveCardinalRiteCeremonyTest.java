package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class ActiveCardinalRiteCeremonyTest {
	@Test
	void consecrationRequiresEveryAnchorBeforeInscription() {
		ActiveCardinalRite rite = rite(2, false, 3);
		assertEquals(CardinalRitePhase.CONSECRATION, rite.getPhase());
		assertFalse(rite.enterInscription());

		for (int i = 0; i < 8; i++) {
			assertTrue(rite.fillAnchor(i, 50));
		}

		assertEquals(2, rite.completedRings());
		assertEquals(400, rite.getCommittedBloodMl());
		assertTrue(rite.enterInscription());
		assertTrue(rite.sealAltar());
		assertEquals(CardinalRitePhase.ORDEAL, rite.getPhase());
	}

	@Test
	void ordealFlowsThroughStillIntervalAndCulmination() {
		ActiveCardinalRite rite = rite(1, true, 1);
		rite.fillAnchor(0, 50);
		rite.fillAnchor(1, 50);
		rite.fillAnchor(2, 50);
		rite.fillAnchor(3, 50);
		rite.enterInscription();
		rite.sealAltar();
		rite.completeWave();
		assertEquals(CardinalRitePhase.STILL_INTERVAL, rite.getPhase());
		rite.finishStillInterval(false);
		assertEquals(CardinalRitePhase.CULMINATION, rite.getPhase());
		rite.markComplete();
		assertTrue(rite.isComplete());
	}

	@Test
	void timedWaitingPhasesAdvanceOverallProgressContinuously() {
		ActiveCardinalRite rite = rite(1, true, 1);
		for (int anchor = 0; anchor < 4; anchor++) rite.fillAnchor(anchor, 50);
		assertEquals(0.25D, rite.getProgress(100), 0.0001D, "consecration occupies first quarter");
		rite.enterInscription();
		rite.sealAltar();
		rite.completeWave();

		assertEquals(0.75D, rite.getProgress(100), 0.0001D, "still interval begins after ordeal");
		for (int tick = 0; tick < 50; tick++) rite.tick();
		assertEquals(0.80D, rite.getProgress(100), 0.0001D, "still interval midpoint");
		for (int tick = 0; tick < 50; tick++) rite.tick();
		assertEquals(0.85D, rite.getProgress(100), 0.0001D, "still interval end");

		rite.finishStillInterval(false);
		assertEquals(0.95D, rite.getProgress(100), 0.0001D, "culmination begins");
		for (int tick = 0; tick < 20; tick++) rite.tick();
		assertEquals(0.975D, rite.getProgress(100), 0.0001D, "culmination midpoint");
	}

	@Test
	void ceremonyStateRoundTripsAndOldStateRemainsLegacy() {
		ActiveCardinalRite rite = rite(5, false, 6);
		UUID ally = UUID.randomUUID();
		rite.fillAnchor(0, 25);
		rite.addInstability(43);
		rite.setSigilProgress("hemomancy:reservoir", 3);
		rite.assignAlly(ally, CardinalRiteAllyRole.WARDEN);
		rite.setSharedPoolOptIn(ally, true);
		rite.carryIchor(30);

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize());
		assertEquals(CardinalRitePhase.CONSECRATION, copy.getPhase());
		assertEquals(43, copy.getInstability());
		assertEquals(rite.getBrokenInstabilityAnchors(), copy.getBrokenInstabilityAnchors());
		assertEquals(CardinalRiteInstability.STRAINED, copy.getInstabilityBand());
		assertEquals(25, copy.getAnchorBloodMl()[0]);
		assertEquals(3, copy.getSigilProgress().get("hemomancy:reservoir"));
		assertEquals(CardinalRiteAllyRole.WARDEN, copy.getAllyRoles().get(ally));
		assertTrue(copy.getSharedPoolOptIns().contains(ally));
		assertEquals(30, copy.getCarriedIchorMl());

		CompoundTag old = new CompoundTag();
		old.putUUID("PlayerUUID", UUID.randomUUID());
		old.putLong("CenterPos", BlockPos.ZERO.asLong());
		old.putString("RecipeId", "hemomancy:cardinal_rite/old");
		old.putInt("TotalTicks", 100);
		old.putInt("RemainingTicks", 40);
		old.putInt("RiteSize", 3);
		assertEquals(CardinalRitePhase.LEGACY, ActiveCardinalRite.deserialize(old).getPhase());
	}

	@Test
	void repairingABrokenAnchorPreservesWhichBoundarySectionWasRestored() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(
				UUID.randomUUID(), BlockPos.ZERO, ResourceLocation.parse("hemomancy:test"),
				200, 5, 2, false, 1, 8);
		rite.addInstability(26);

		assertTrue(rite.getBrokenInstabilityAnchors().contains(0));
		assertTrue(rite.getBrokenInstabilityAnchors().contains(1));
		assertTrue(rite.offerInstabilityRepair(0, 50));

		assertEquals(13, rite.getInstability(), "tier-scaled repair");
		assertFalse(rite.getBrokenInstabilityAnchors().contains(0), "clicked anchor restored");
		assertTrue(rite.getBrokenInstabilityAnchors().contains(1));
	}

	@Test
	void instabilityUsesTheConfiguredOuterToInnerDamagePriority() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(
				UUID.randomUUID(), BlockPos.ZERO, ResourceLocation.parse("hemomancy:test"),
				200, 5, 2, false, 1, 8);
		rite.setInstabilityDamagePriority(new int[] {4, 5, 6, 7, 0, 1, 2, 3});

		rite.addInstability(26);

		assertEquals(java.util.Set.of(4, 5), rite.getBrokenInstabilityAnchors());
		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize());
		copy.addInstability(13);
		assertEquals(java.util.Set.of(4, 5, 6), copy.getBrokenInstabilityAnchors(),
				"priority survives reload");
	}

	@Test
	void loadingAnOlderActiveRiteCanRehomeDamageToTheAuthoredOuterRing() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(
				UUID.randomUUID(), BlockPos.ZERO, ResourceLocation.parse("hemomancy:test"),
				200, 5, 2, false, 1, 8);
		rite.addInstability(26);
		assertEquals(java.util.Set.of(0, 1), rite.getBrokenInstabilityAnchors());

		rite.setInstabilityDamagePriority(new int[] {4, 5, 6, 7, 0, 1, 2, 3});

		assertEquals(java.util.Set.of(4, 5), rite.getBrokenInstabilityAnchors());
	}

	@Test
	void awakeningASigilIsIdempotentAndPersistsAcrossReloads() {
		ActiveCardinalRite rite = rite(1, false, 2);

		assertTrue(rite.awakenSigil("hemomancy:reservoir"), "first completion spawns an entity");
		assertFalse(rite.awakenSigil("hemomancy:reservoir"), "repeat completion cannot spawn a duplicate");
		assertTrue(rite.isSigilAwakened("hemomancy:reservoir"));

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize());
		assertTrue(copy.isSigilAwakened("hemomancy:reservoir"), "awakened state survives world reload");
		assertEquals(1, copy.getAwakenedSigils().size());
	}

	private static ActiveCardinalRite rite(int degree, boolean abbreviated, int waves) {
		return ActiveCardinalRite.interactive(UUID.randomUUID(), BlockPos.ZERO,
				ResourceLocation.fromNamespaceAndPath("hemomancy", "cardinal_rite/test"),
				100, 3, degree, abbreviated, waves);
	}
}

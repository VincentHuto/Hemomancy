package com.vincenthuto.hemomancy.common.tile;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Lazy settlement: nothing ticks, every read clots what the time since feeding says, however long the gap. */
class BlockBloodVolumeTest {
	private static BlockBloodVolume tank(double capacity, double fresh, long fedAt) {
		BlockBloodVolume volume = new BlockBloodVolume();
		volume.setMaxBloodVolume(capacity);
		volume.settle(fedAt, false);
		volume.fill(fresh);
		return volume;
	}

	@Test
	void theFirstReadStampsAndNothingClotsInsideTheWindow() {
		BlockBloodVolume volume = tank(1_000.0D, 600.0D, 100L);
		volume.settle(100L + CoagulationRules.FRESH_WINDOW_TICKS, false);
		assertEquals(600.0D, volume.getBloodVolume(), 1e-9);
		assertEquals(0.0D, volume.clotted(), 1e-9);
	}

	@Test
	void aLongUnloadedGapSettlesInOneRead() {
		BlockBloodVolume volume = tank(1_000.0D, 600.0D, 0L);
		volume.settle(10_000_000L, false);
		assertEquals(0.0D, volume.getBloodVolume(), 1e-9);
		assertEquals(600.0D, volume.clotted(), 1e-9);
	}

	@Test
	void manySmallReadsMatchOneLateRead() {
		BlockBloodVolume stepped = tank(1_000.0D, 800.0D, 0L);
		for (long t = 20L; t <= 30_000L; t += 20L) stepped.settle(t, false);
		BlockBloodVolume once = tank(1_000.0D, 800.0D, 0L);
		once.settle(30_000L, false);
		assertEquals(once.getBloodVolume(), stepped.getBloodVolume(), 1e-6);
		assertEquals(once.clotted(), stepped.clotted(), 1e-6);
		assertEquals(800.0D, stepped.getBloodVolume() + stepped.clotted(), 1e-6);
	}

	@Test
	void aTopUpBlendsTheAgeInsteadOfMakingTheTankNew() {
		// NQ-01-3: 1 mL into 8,000 changes nothing; an equal pour halves the age.
		BlockBloodVolume big = tank(10_000.0D, 8_000.0D, 0L);
		big.settle(CoagulationRules.FRESH_WINDOW_TICKS - 1L, false);
		big.fill(1.0D);
		assertEquals(CoagulationRules.FRESH_WINDOW_TICKS - 1L, big.age(), 1.0D);
		BlockBloodVolume half = tank(1_000.0D, 300.0D, 0L);
		half.settle(4_000L, false);
		half.fill(300.0D);
		assertEquals(2_000L, half.age());
		assertEquals(2_000L, half.lastFed());
	}

	@Test
	void clotStaysUnderAPlainFill() {
		BlockBloodVolume volume = tank(1_000.0D, 500.0D, 0L);
		volume.settle(18_000L, false);
		double clot = volume.clotted();
		assertTrue(clot > 0.0D);
		volume.fill(100.0D);
		assertEquals(clot, volume.clotted(), 1e-9, "a flask or gutter pour woke clot");
	}

	@Test
	void aRevivingPourWakesClotAndBlendsTheAge() {
		BlockBloodVolume volume = tank(8_000.0D, 6_000.0D, 0L);
		volume.settle(CoagulationRules.FULL_CLOT_TICKS, false);
		volume.scrapeClot(volume.clotted() - 1_000.0D);
		volume.setBloodVolume(5_000.0D);
		CoagulationRules.Pour pour = volume.revive(1_000.0D, 0.5D, 2.0D);
		assertEquals(1_000.0D, pour.taken(), 1e-9);
		assertEquals(6_500.0D, volume.getBloodVolume(), 1e-9);
		assertEquals(0.0D, volume.clotted(), 1e-9);
		// 5,000 old blood and 1,500 new blend to about three quarters of the old age.
		assertEquals(Math.round(CoagulationRules.FULL_CLOT_TICKS * 5_000.0D / 6_500.0D), volume.age());
	}

	@Test
	void aFullClottedTankStillRevives() {
		BlockBloodVolume volume = tank(600.0D, 600.0D, 0L);
		volume.settle(CoagulationRules.FULL_CLOT_TICKS, false);
		assertEquals(0.0D, volume.getMaxBloodVolume(), 1e-9);
		CoagulationRules.Pour pour = volume.revive(100.0D, 0.5D, 4.0D);
		assertEquals(100.0D, pour.spent(), 1e-9);
		assertEquals(400.0D, volume.getBloodVolume(), 1e-9);
		assertEquals(200.0D, volume.clotted(), 1e-9);
	}

	@Test
	void aCourierDeliveryKeepsItsAgeAndWakesNothing() {
		BlockBloodVolume volume = tank(1_000.0D, 0.0D, 0L);
		volume.settle(100L, false);
		volume.addClot(200.0D);
		assertEquals(300.0D, volume.fillAged(300.0D, 9_000L), 1e-9);
		assertEquals(9_000L, volume.age());
		assertEquals(200.0D, volume.clotted(), 1e-9);
		assertEquals(500.0D, volume.fillAged(900.0D, 9_000L), 1e-9, "a delivery overfilled the tank");
	}

	@Test
	void aPausedClockKeepsTheBloodAsOldAsItWentIn() {
		BlockBloodVolume volume = tank(1_000.0D, 600.0D, 0L);
		volume.settle(CoagulationRules.FRESH_WINDOW_TICKS, false);
		volume.pause(CoagulationRules.FULL_CLOT_TICKS * 2);
		assertEquals(CoagulationRules.FRESH_WINDOW_TICKS, volume.age());
		assertEquals(600.0D, volume.getBloodVolume(), 1e-9);
		volume.settle(CoagulationRules.FULL_CLOT_TICKS * 2 + CoagulationRules.MINUTE_TICKS, false);
		assertEquals(540.0D, volume.getBloodVolume(), 1e-6, "a paused tank came out younger or older than it went in");
	}

	@Test
	void clotTakesRoomUntilItIsScraped() {
		BlockBloodVolume volume = tank(1_000.0D, 1_000.0D, 0L);
		volume.settle(CoagulationRules.FULL_CLOT_TICKS, false);
		assertEquals(0.0D, volume.getMaxBloodVolume(), 1e-9);
		assertTrue(volume.isFull());
		assertFalse(volume.fill(1.0D));
		assertEquals(1_000.0D, volume.capacity(), 1e-9);
		assertEquals(400.0D, volume.scrapeClot(400.0D), 1e-9);
		assertEquals(400.0D, volume.getMaxBloodVolume(), 1e-9);
	}

	@Test
	void aHeldVesselNeverClots() {
		BlockBloodVolume volume = tank(2_000.0D, 1_500.0D, 0L);
		volume.settle(CoagulationRules.FULL_CLOT_TICKS * 3, true);
		assertEquals(1_500.0D, volume.getBloodVolume(), 1e-9);
		assertEquals(0.0D, volume.clotted(), 1e-9);
		// And it reads as just fed when it lets go.
		volume.settle(CoagulationRules.FULL_CLOT_TICKS * 3 + CoagulationRules.FRESH_WINDOW_TICKS, false);
		assertEquals(1_500.0D, volume.getBloodVolume(), 1e-9);
	}

	@Test
	void pouredClotFitsOnlyInFreeRoom() {
		BlockBloodVolume volume = tank(1_000.0D, 700.0D, 0L);
		assertEquals(300.0D, volume.addClot(500.0D), 1e-9);
		assertEquals(0.0D, volume.getMaxBloodVolume() - volume.getBloodVolume(), 1e-9);
	}

	@Test
	void shrinkingTheTankKeepsClotInside() {
		BlockBloodVolume volume = tank(5_000.0D, 3_000.0D, 0L);
		volume.settle(CoagulationRules.FULL_CLOT_TICKS, false);
		volume.setMaxBloodVolume(600.0D);
		assertEquals(600.0D, volume.clotted(), 1e-9);
	}

	@Test
	void ownerIsKept() {
		BlockBloodVolume volume = new BlockBloodVolume();
		UUID owner = UUID.randomUUID();
		volume.setOwner(owner);
		assertEquals(owner, volume.owner());
	}

	@Test
	void ownPouredBloodIsTakenFirstAndNeverOutlastsTheFreshBlood() {
		BlockBloodVolume volume = tank(1_000.0D, 300.0D, 0L);
		UUID pourer = UUID.randomUUID();
		UUID other = UUID.randomUUID();
		volume.fill(200.0D);
		volume.recordPoured(pourer, 200.0D);
		assertEquals(0.0D, volume.takePoured(other, 100.0D), 1e-9, "another player's draw takes none of the share");
		assertEquals(150.0D, volume.takePoured(pourer, 150.0D), 1e-9, "the pourer's draw is their own blood first");
		assertEquals(50.0D, volume.poured(pourer), 1e-9);
		volume.setBloodVolume(20.0D);
		volume.takePoured(other, 0.0D);
		assertEquals(20.0D, volume.poured(pourer), 1e-9, "a share never outlasts the fresh blood left");
	}
}

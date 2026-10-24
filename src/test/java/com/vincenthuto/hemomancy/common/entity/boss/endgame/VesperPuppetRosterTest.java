package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class VesperPuppetRosterTest {
	@Test
	void rosterHoldsSevenShapesAndExcludesTheRingmaster() {
		assertEquals(7, VesperPuppetRoster.SHAPES.size());
		assertFalse(VesperPuppetRoster.SHAPES.contains(PuppeteerSummonDefinitions.RINGMASTER_PATTERN));
		for (String shape : VesperPuppetRoster.SHAPES) {
			assertTrue(PuppeteerSummonDefinitions.byName(shape).isPresent(), shape);
		}
	}

	@Test
	void musterNeverExceedsThreeOut() {
		VesperPuppetRoster roster = new VesperPuppetRoster();
		RandomSource random = RandomSource.create(4L);
		List<String> first = roster.nextMuster(random);
		assertEquals(3, first.size());
		first.forEach(roster::markOut);
		assertTrue(roster.nextMuster(random).isEmpty());
		roster.recall(first.getFirst());
		assertEquals(1, roster.nextMuster(random).size());
	}

	@Test
	void everyShapeFieldedBeforeAnyRepeat() {
		VesperPuppetRoster roster = new VesperPuppetRoster();
		RandomSource random = RandomSource.create(9L);
		Set<String> fielded = new HashSet<>();
		while (fielded.size() < VesperPuppetRoster.SHAPES.size()) {
			List<String> muster = roster.nextMuster(random);
			assertFalse(muster.isEmpty());
			for (String shape : muster) {
				roster.markOut(shape);
				// The muster that fields the last unseen shape may fill its other slots with repeats.
				if (fielded.size() == VesperPuppetRoster.SHAPES.size()) continue;
				assertTrue(fielded.add(shape), shape + " was fielded twice before every shape had a turn");
			}
			for (String shape : muster) roster.recall(shape);
		}
	}

	@Test
	void struckShapesNeverReturn() {
		VesperPuppetRoster roster = new VesperPuppetRoster();
		RandomSource random = RandomSource.create(2L);
		String victim = roster.nextMuster(random).getFirst();
		roster.markOut(victim);
		roster.strike(victim);
		assertEquals(VesperPuppetRoster.Slot.STRUCK, roster.slot(victim));
		for (int i = 0; i < 40; i++) {
			List<String> muster = roster.nextMuster(random);
			assertFalse(muster.contains(victim));
			muster.forEach(roster::markOut);
			muster.forEach(roster::recall);
		}
	}

	@Test
	void survivorsIncludeNeverCalledAndRecalled() {
		VesperPuppetRoster roster = new VesperPuppetRoster();
		roster.markOut(PuppeteerSummonDefinitions.GOREBOUND_HULK);
		roster.strike(PuppeteerSummonDefinitions.GOREBOUND_HULK);
		roster.markOut(PuppeteerSummonDefinitions.SANGUINE_HOUND);
		roster.recall(PuppeteerSummonDefinitions.SANGUINE_HOUND);
		roster.markOut(PuppeteerSummonDefinitions.CINDER_BELLOWS);
		List<String> survivors = roster.survivors();
		assertEquals(6, survivors.size());
		assertFalse(survivors.contains(PuppeteerSummonDefinitions.GOREBOUND_HULK));
		assertTrue(survivors.contains(PuppeteerSummonDefinitions.SANGUINE_HOUND));
		assertTrue(survivors.contains(PuppeteerSummonDefinitions.CINDER_BELLOWS), "still out at the end counts");
		assertTrue(survivors.contains(PuppeteerSummonDefinitions.MNEMONIST_PUPPET), "never called counts");
	}

	@Test
	void recallWaitsUntilAPuppetHasHadItsTurn() {
		assertFalse(VesperPuppetRoster.mayRecall(399));
		assertTrue(VesperPuppetRoster.mayRecall(400));
	}

	@Test
	void roundTripsThroughNbt() {
		VesperPuppetRoster roster = new VesperPuppetRoster();
		roster.markOut(PuppeteerSummonDefinitions.MARROW_SPITTER);
		roster.markOut(PuppeteerSummonDefinitions.VEINWING_VULTURE);
		roster.strike(PuppeteerSummonDefinitions.VEINWING_VULTURE);
		CompoundTag tag = roster.save();
		VesperPuppetRoster loaded = VesperPuppetRoster.load(tag);
		assertEquals(VesperPuppetRoster.Slot.OUT, loaded.slot(PuppeteerSummonDefinitions.MARROW_SPITTER));
		assertEquals(VesperPuppetRoster.Slot.STRUCK, loaded.slot(PuppeteerSummonDefinitions.VEINWING_VULTURE));
		assertTrue(loaded.hasFielded(PuppeteerSummonDefinitions.MARROW_SPITTER));
		assertFalse(loaded.hasFielded(PuppeteerSummonDefinitions.MNEMONIST_PUPPET));
		assertEquals(roster.survivors(), loaded.survivors());
	}

	@Test
	void encounterResetReturnsOutPuppetsButKeepsStruck() {
		VesperPuppetRoster roster = new VesperPuppetRoster();
		roster.markOut(PuppeteerSummonDefinitions.SCARLET_MUMMER);
		roster.markOut(PuppeteerSummonDefinitions.SANGUINE_HOUND);
		roster.strike(PuppeteerSummonDefinitions.SANGUINE_HOUND);
		roster.onEncounterReset();
		assertEquals(0, roster.out());
		assertEquals(VesperPuppetRoster.Slot.AVAILABLE, roster.slot(PuppeteerSummonDefinitions.SCARLET_MUMMER));
		assertEquals(VesperPuppetRoster.Slot.STRUCK, roster.slot(PuppeteerSummonDefinitions.SANGUINE_HOUND));
	}
}

package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HemorathWellRulesTest {
	@Test
	void absorbAndProjectAreCappedByContentRoomAndTheItemsRate() {
		assertEquals(30.0F, HemorathWellRules.absorb(300.0F, 100.0D, 1000.0D));
		assertEquals(12.0F, HemorathWellRules.absorb(12.0F, 100.0D, 1000.0D));
		assertEquals(5.0F, HemorathWellRules.absorb(300.0F, 100.0D, 5.0D));
		assertEquals(10.0F, HemorathWellRules.absorb(300.0F, 10.0D, 1000.0D));
		assertEquals(25.0F, HemorathWellRules.project(300.0F, 100.0D, 1000.0D));
		assertEquals(4.0F, HemorathWellRules.project(996.0F, 100.0D, 1000.0D));
		assertEquals(7.0F, HemorathWellRules.project(300.0F, 100.0D, 7.0D));
	}

	@Test
	void aDrawEmptiesAWellOverFortyTicksAndHealsOnePerFifty() {
		HemorathWells wells = new HemorathWells();
		wells.count(4);
		wells.startDraw(HemorathWells.pairMask(0), 0);
		float drawn = 0.0F;
		for (int tick = 0; tick < HemorathWellRules.DRAW_TICKS; tick++) {
			for (int i = 0; i < 4; i++) drawn += wells.take(i, wells.flow(i));
			wells.tickDraw();
		}
		assertEquals(600.0F, drawn, 0.01F);
		assertEquals(0.0F, wells.content(0), 0.01F);
		assertEquals(300.0F, wells.content(2));
		assertFalse(wells.drawing());
		assertEquals(12.0F, HemorathWellRules.heal(600.0F), 1.0E-4F);
	}

	@Test
	void denialTakesTheFlowFirst() {
		assertEquals(0.0F, HemorathWellRules.afterDenial(7.5F, 30.0F));
		assertEquals(2.5F, HemorathWellRules.afterDenial(7.5F, 5.0F));
	}

	@Test
	void theLitanyRefillsAndDrawsAllFour() {
		HemorathWells wells = new HemorathWells();
		wells.count(4);
		wells.take(0, 300.0F);
		wells.refill();
		assertEquals(2.0F, wells.content(0));
		assertEquals(0b1111, wells.allMask());
		for (int i = 0; i < 500; i++) wells.refill();
		assertEquals(HemorathWellRules.CAPACITY, wells.content(1));
	}

	@Test
	void pairsAreDrawnByContentAndRestAfterADraw() {
		HemorathWells wells = new HemorathWells();
		wells.count(4);
		wells.credit(2, 400.0F);
		assertEquals(1, wells.bestPair(10_000));
		wells.startDraw(HemorathWells.pairMask(1), 10_000);
		wells.stopDraw();
		assertEquals(0, wells.bestPair(10_000 + 10));
		assertEquals(-1, new HemorathWells().bestPair(0), "a chamber with no wells offers no pair");
	}

	@Test
	void aDrainedTideSendsHalfBackAQuarterEach() {
		assertEquals(50.0F, HemorathWellRules.tideReturnEach(400.0F));
	}

	@Test
	void theGuttersShowTheWellsAndHisMeter() {
		assertEquals(0, HemorathWellRules.spokeFill(199.0F));
		assertEquals(1, HemorathWellRules.spokeFill(200.0F));
		assertEquals(2, HemorathWellRules.spokeFill(500.0F));
		assertEquals(3, HemorathWellRules.spokeFill(800.0F));
		assertEquals(0, HemorathWellRules.pitFill(0.34F));
		assertEquals(1, HemorathWellRules.pitFill(0.35F));
		assertEquals(2, HemorathWellRules.pitFill(0.7F));
		assertEquals(3, HemorathWellRules.pitFill(0.9F));
	}

	@Test
	void thePackedWordAndTheSaveRoundTrip() {
		float[] levels = {0.0F, 300.0F, 1000.0F, 640.0F};
		int word = HemorathWellRules.pack(levels);
		for (int i = 0; i < 4; i++) assertEquals(levels[i], HemorathWellRules.unpack(word, i), 4.0F);
		HemorathWells wells = new HemorathWells();
		wells.count(4);
		wells.credit(3, 123.0F);
		CompoundTag tag = new CompoundTag();
		wells.save(tag);
		HemorathWells loaded = new HemorathWells();
		loaded.load(tag);
		assertEquals(4, loaded.count());
		assertEquals(423.0F, loaded.content(3));
	}
}

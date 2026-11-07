package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HemorathIronRulesTest {
	@Test
	void onePatchPerThousandLeakedUpToNine() {
		HemorathPatches patches = new HemorathPatches();
		assertEquals(0, patches.leak(999.0F, 1));
		assertEquals(1, patches.leak(1.0F, 2));
		assertEquals(2, patches.changedAt());
		assertEquals(8, patches.leak(20_000.0F, 3));
		assertEquals(9, patches.count());
		assertEquals(0, patches.leak(5_000.0F, 4), "the iron stops at nine");
		assertEquals(9, HemorathIronRules.patchesFor(1.0E6F));
	}

	@Test
	void eachPatchHardensHimAndShapesHisIronMoves() {
		assertEquals(1.0F, HemorathIronRules.reduction(0));
		assertEquals(0.64F, HemorathIronRules.reduction(9), 1.0E-5F);
		assertEquals(1, HemorathIronRules.rakeSideLanes(3));
		assertEquals(2, HemorathIronRules.rakeSideLanes(4));
		assertEquals(0.55D, HemorathIronRules.rakeHalfWidth(8));
		assertEquals(8.0F, HemorathIronRules.haloDamage(2));
		assertEquals(11.0F, HemorathIronRules.haloDamage(9));
		assertEquals(0, HemorathIronRules.thicketSpend(2));
		assertEquals(3, HemorathIronRules.thicketSpend(7));
		assertEquals(6, HemorathIronRules.thicketDiscs(3));
	}

	@Test
	void patchesTearOutNewestFirstAndSave() {
		HemorathPatches patches = new HemorathPatches();
		patches.leak(5000.0F, 0);
		assertEquals(2, patches.tear(2, 10));
		assertEquals(3, patches.count());
		assertEquals(3, patches.burstAll(20));
		assertEquals(0, patches.count());
		patches.leak(2500.0F, 30);
		CompoundTag tag = new CompoundTag();
		patches.save(tag);
		HemorathPatches loaded = new HemorathPatches();
		loaded.load(tag);
		assertEquals(2, loaded.count());
		assertEquals(1, loaded.leak(500.0F, 40), "the half-leaked patch is kept across a save");
		assertEquals(9, HemorathIronRules.SITES.length);
		assertEquals("right_forearm", HemorathIronRules.SITES[0]);
		assertEquals("head", HemorathIronRules.SITES[8]);
	}

	@Test
	void theHaloPathGoesOutTenAndComesHomeInFrontOfHim() {
		double[] start = HemorathHaloPath.at(0);
		assertEquals(0.0D, start[0], 1.0E-6D);
		double[] turn = HemorathHaloPath.at(HemorathHaloPath.OUT / HemorathHaloPath.speed());
		assertEquals(HemorathHaloPath.OUT, turn[0], 1.0E-6D);
		assertEquals(0.0D, turn[1], 1.0E-6D);
		double[] end = HemorathHaloPath.at(HemorathHaloPath.FLIGHT_TICKS);
		assertEquals(HemorathHaloPath.CATCH, end[0], 1.0E-6D);
		assertEquals(0.0D, end[1], 1.0E-6D);
		assertFalse(HemorathHaloPath.returning(1));
		assertTrue(HemorathHaloPath.returning(HemorathHaloPath.FLIGHT_TICKS - 1));
	}
}

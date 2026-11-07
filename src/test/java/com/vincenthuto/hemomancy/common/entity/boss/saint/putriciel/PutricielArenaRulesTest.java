package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielChamberLightingRules.Light;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielChamberLightingRules.Phase;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The chamber's part in the fight (design section 9): scorch caps, the Bloom's safe cells and reclaim, the lights. */
final class PutricielArenaRulesTest {
	@Test
	void scorchCapsAreFortyEightAnIntervalAndOneSixtyStanding() {
		assertEquals(48, PutricielArenaRules.SCORCH_PER_INTERVAL);
		assertEquals(160, PutricielArenaRules.SCORCH_RECORDED);
		assertTrue(PutricielArenaRules.SWEEP_SCORCH + PutricielArenaRules.CAST_SCORCH + PutricielArenaRules.LANE_SCORCH
				+ PutricielArenaRules.LINES_SCORCH <= PutricielArenaRules.SCORCH_PER_INTERVAL);
	}

	@Test
	void theBloomSparesAshPyreCirclesAndTheBody() {
		LongSet ash = new LongOpenHashSet();
		ash.add(BossFootprint.cell(5, 0));
		Vec3 pyre = new Vec3(-6.5, 0, 0.5);
		LongSet safe = PutricielArenaRules.safeCells(ash, List.of(pyre), Vec3.ZERO, Vec3.ZERO, 12.0D);
		assertTrue(safe.contains(BossFootprint.cell(5, 0)), "ash is safe ground");
		assertTrue(safe.contains(BossFootprint.cell(-7, 0)), "a pyre's circle is safe");
		assertTrue(safe.contains(BossFootprint.cell(0, 0)), "the body's own cell is clear");
		assertFalse(safe.contains(BossFootprint.cell(3, 3)), "open moss is not");
	}

	@Test
	void theRotReclaimsOnlyEmptyAsh() {
		LongList ash = new LongArrayList(new long[]{BossFootprint.cell(1, 1), BossFootprint.cell(2, 2)});
		LongSet occupied = new LongOpenHashSet(new long[]{BossFootprint.cell(2, 2)});
		assertEquals(List.of(BossFootprint.cell(1, 1)), PutricielArenaRules.reclaim(ash, occupied));
	}

	@Test
	void lineAndDiscCellsRespectTheirCaps() {
		LongList line = PutricielArenaRules.lineCells(new Vec3(0, 0, 0), new Vec3(10, 0, 0), 1.0D, 8);
		assertEquals(8, line.size());
		LongList disc = PutricielArenaRules.discCells(new Vec3(0.5, 0, 0.5), 2.5D, 12);
		assertTrue(disc.size() <= 12 && disc.getLong(0) == BossFootprint.cell(0, 0), "nearest the centre first");
	}

	@Test
	void faithfulCaps() {
		assertTrue(PutricielArenaRules.faithfulMayRise(1, 7));
		assertFalse(PutricielArenaRules.faithfulMayRise(2, 3));
		assertFalse(PutricielArenaRules.faithfulMayRise(0, 8));
	}

	@Test
	void theLightingTable() {
		assertFalse(PutricielChamberLightingRules.on(Light.PYRE, Phase.ROT));
		assertTrue(PutricielChamberLightingRules.on(Light.SHELF, Phase.ROT));
		assertTrue(PutricielChamberLightingRules.on(Light.PYRE, Phase.CLEANSING));
		assertFalse(PutricielChamberLightingRules.on(Light.SHELF, Phase.CLEANSING));
		assertTrue(PutricielChamberLightingRules.on(Light.PYRE, Phase.REST));
		assertFalse(PutricielChamberLightingRules.on(Light.SHELF, Phase.REST));
		assertEquals(Phase.ROT, Phase.of(PutricielStage.ROT));
		assertEquals(Phase.ROT, Phase.of(PutricielStage.EMBERING));
		assertEquals(Phase.CLEANSING, Phase.of(PutricielStage.BURNING));
	}

	@Test
	void theSludgeAshHasItsOwnBudget() {
		assertEquals("putriciel_sludge_ash", PutricielArenaRules.SLUDGE_ASH_BUDGET);
		assertEquals(8, PutricielArenaRules.SLUDGE_ASH_PER_POOL);
		assertEquals(24, PutricielArenaRules.SLUDGE_ASH_PER_WINDOW);
		assertEquals(600, PutricielArenaRules.SLUDGE_ASH_WINDOW);
		assertEquals(64, PutricielArenaRules.SLUDGE_ASH_RECORDED);
		assertFalse(PutricielArenaRules.SLUDGE_ASH_BUDGET.equals(PutricielArenaRules.SCORCH_BUDGET), "a second budget");
	}

	@Test
	void cellsUnderAPoolAreNeverSafeButItsAshIs() {
		LongSet ash = new LongOpenHashSet();
		ash.add(BossFootprint.cell(5, 0));
		LongSet pooled = new LongOpenHashSet(new long[]{BossFootprint.cell(0, 0), BossFootprint.cell(5, 0)});
		LongSet safe = PutricielArenaRules.safeCells(ash, List.of(), Vec3.ZERO, Vec3.ZERO, 12.0D, pooled);
		assertFalse(safe.contains(BossFootprint.cell(0, 0)), "a pool is part of the rot, even over the body's cell");
		assertTrue(safe.contains(BossFootprint.cell(5, 0)), "ash from burned sludge is ordinary fight ash");
	}
}

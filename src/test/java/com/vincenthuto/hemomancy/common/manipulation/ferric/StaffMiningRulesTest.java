package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingDigRules;
import com.vincenthuto.hemomancy.common.manipulation.ferric.StaffMiningRules.HewShape;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Core loop Track 8: Lode Draw's pace and price, and Hematic Hewing's shapes. */
class StaffMiningRulesTest {
	@Test
	void aLodeDrawsOneBlockAPulseAndNeverFasterOnTheStaff() {
		assertEquals(1.0D, StaffMiningRules.lodeBlocksPerPulse(1.0D), 1e-9);
		assertEquals(1.0D, StaffMiningRules.lodeBlocksPerPulse(3.2D), 1e-9, "the Staff's rate does not cheapen a block");
		assertEquals(0.75D, StaffMiningRules.lodeBlocksPerPulse(0.75D), 1e-9, "a dry pulse draws less");
	}

	@Test
	void theSculptingsCostMoreABlockThanThePickaxe() {
		double drawPerBlock = StaffMiningRules.LODE_BLOOD_PER_PULSE / StaffMiningRules.lodeBlocksPerPulse(1.0D);
		assertTrue(drawPerBlock > LivingDigRules.bloodPerBlock(4.5F), "dearer than a deepslate ore by pickaxe");
		double tunnelPerBlock = StaffMiningRules.hewPrice(new float[] { 3.0F, 3.0F }) / 2.0D;
		assertTrue(tunnelPerBlock > LivingDigRules.bloodPerBlock(3.0F), "a tunnel is dearer than picking deepslate");
	}

	@Test
	void aCutCostsTwoMlAHardnessPointWithA36MlFloor() {
		assertEquals(36.0D, StaffMiningRules.HEW_MIN_BLOOD_PER_CUT, 1e-9);
		assertEquals(36.0D, StaffMiningRules.hewPrice(new float[] { 1.5F, 1.5F }), 1e-9, "a two-block stone cut pays the floor");
		assertEquals(36.0D, StaffMiningRules.hewPrice(new float[0]), 1e-9, "an empty cut still pays the floor");
		float[] deepslateFace = new float[9];
		Arrays.fill(deepslateFace, 3.0F);
		assertEquals(54.0D, StaffMiningRules.hewPrice(deepslateFace), 1e-9, "nine deepslate blocks cost 6 mL each");
		assertEquals(80.0D, StaffMiningRules.hewPrice(new float[] { 50.0F, 50.0F }), 1e-9, "each block keeps the pickaxe's 40 mL cap");
	}

	@Test
	void sneakingCutsAFaceAndASteepAimCutsAStair() {
		assertEquals(HewShape.TUNNEL, StaffMiningRules.shapeFor(false, 10.0F, true));
		assertEquals(HewShape.FACE, StaffMiningRules.shapeFor(true, 60.0F, true));
		assertEquals(HewShape.STAIR, StaffMiningRules.shapeFor(false, 50.0F, true));
		assertEquals(HewShape.STAIR, StaffMiningRules.shapeFor(false, -40.0F, true));
		assertEquals(HewShape.TUNNEL, StaffMiningRules.shapeFor(false, 80.0F, false), "a floor is shafted, not stepped");
	}

	@Test
	void aTunnelIsTwoTallTowardTheBody() {
		assertEquals(set(new int[] { 0, 0, 0 }, new int[] { 0, -1, 0 }),
				set(StaffMiningRules.cells(HewShape.TUNNEL, 0, 0, 1, true)));
		assertEquals(set(new int[] { 0, 0, 0 }, new int[] { 0, 1, 0 }),
				set(StaffMiningRules.cells(HewShape.TUNNEL, 0, 0, 1, false)));
		assertEquals(set(new int[] { 0, 0, 0 }, new int[] { 0, -1, 0 }),
				set(StaffMiningRules.cells(HewShape.TUNNEL, 0, 1, 0, false)), "a floor is cut two deep");
	}

	@Test
	void aFaceIsNineAcrossTheStruckFace() {
		Set<String> wall = set(StaffMiningRules.cells(HewShape.FACE, 1, 0, 0, false));
		assertEquals(9, wall.size());
		assertTrue(wall.stream().allMatch(cell -> cell.startsWith("0,")), "a face on the x axis stays in its plane");
		Set<String> floor = set(StaffMiningRules.cells(HewShape.FACE, 0, 1, 0, false));
		assertEquals(9, floor.size());
		assertTrue(floor.stream().allMatch(cell -> cell.split(",")[1].equals("0")));
	}

	@Test
	void aStairIsAColumnThreeTall() {
		assertEquals(set(new int[] { 0, 0, 0 }, new int[] { 0, 1, 0 }, new int[] { 0, -1, 0 }),
				set(StaffMiningRules.cells(HewShape.STAIR, 0, 0, -1, true)));
	}

	private static Set<String> set(List<int[]> cells) {
		Set<String> out = new HashSet<>();
		for (int[] c : cells) out.add(c[0] + "," + c[1] + "," + c[2]);
		return out;
	}

	private static Set<String> set(int[]... cells) {
		return set(Arrays.asList(cells));
	}
}

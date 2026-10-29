package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MausoleumWardRulesTest {
	/** The threshold hall's slab: x=7 of a hall running east, floor top at y=1, corridor z 3..7. */
	private static final AABB EAST_SLAB = new AABB(7, 1, 3, 8, 6, 8);

	@Test
	void onlyTheEighthDegreeCrosses() {
		assertEquals(8, MausoleumWardRules.WARD_DEGREE);
	}

	@Test
	void aVisitorTouchingTheLineIsSetDownABlockBeforeIt() {
		Vec3 back = MausoleumWardRules.pushBack(new Vec3(6.9, 1.0, 5.2), EAST_SLAB, Direction.EAST);
		assertEquals(new Vec3(6.0, 1.0, 5.2), back);
	}

	@Test
	void aVisitorFoundDeepPastTheLineIsSetDownBeforeItToo() {
		Vec3 back = MausoleumWardRules.pushBack(new Vec3(30.0, 4.0, 5.5), EAST_SLAB, Direction.EAST);
		assertEquals(new Vec3(6.0, 1.0, 5.5), back);
	}

	@Test
	void theWayBackFollowsTheHallsTurn() {
		AABB westSlab = new AABB(-8, 1, -8, -7, 6, -3);
		assertEquals(new Vec3(-6.0, 1.0, -5.0),
				MausoleumWardRules.pushBack(new Vec3(-7.5, 1.0, -5.0), westSlab, Direction.WEST));
		AABB southSlab = new AABB(3, 1, 7, 8, 6, 8);
		assertEquals(new Vec3(5.5, 1.0, 6.0),
				MausoleumWardRules.pushBack(new Vec3(5.5, 1.0, 7.4), southSlab, Direction.SOUTH));
		AABB northSlab = new AABB(3, 1, 7, 8, 6, 8);
		assertEquals(new Vec3(5.5, 1.0, 9.0),
				MausoleumWardRules.pushBack(new Vec3(5.5, 1.0, 7.4), northSlab, Direction.NORTH));
	}

	@Test
	void theWayBackKeepsTheBodyOffTheSideWalls() {
		Vec3 back = MausoleumWardRules.pushBack(new Vec3(7.5, 1.0, 2.0), EAST_SLAB, Direction.EAST);
		assertEquals(3.0 + MausoleumWardRules.SIDE_MARGIN, back.z, 1.0E-9);
		back = MausoleumWardRules.pushBack(new Vec3(7.5, 1.0, 12.0), EAST_SLAB, Direction.EAST);
		assertEquals(8.0 - MausoleumWardRules.SIDE_MARGIN, back.z, 1.0E-9);
	}

	@Test
	void quickBlowsBrightenTheFlashUpToACap() {
		assertEquals(1.0f, MausoleumWardRules.flashStrength(0));
		assertEquals(1.0f, MausoleumWardRules.flashStrength(1));
		float previous = 1.0f;
		for (int strikes = 2; strikes <= 5; strikes++) {
			float flash = MausoleumWardRules.flashStrength(strikes);
			assertTrue(flash > previous, "strike " + strikes + " must flash brighter");
			previous = flash;
		}
		assertEquals(MausoleumWardRules.flashStrength(40), MausoleumWardRules.flashStrength(400));
		assertTrue(MausoleumWardRules.flashStrength(400) <= 2.5f);
	}

	@Test
	void everySaintHasItsOwnWardColour() {
		Set<Integer> colours = new HashSet<>();
		for (EnumSaintType saint : EnumSaintType.values()) {
			int colour = MausoleumWardRules.wardColor(saint);
			assertEquals(0, colour >>> 24, saint + " colour is RGB only");
			colours.add(colour);
		}
		assertEquals(EnumSaintType.values().length, colours.size());
	}
}

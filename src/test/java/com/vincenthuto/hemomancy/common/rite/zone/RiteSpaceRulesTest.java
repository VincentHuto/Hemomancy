package com.vincenthuto.hemomancy.common.rite.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A rite space is the cube above a consecrated floor, and it lasts only as long as that floor. */
final class RiteSpaceRulesTest {
	private static final BlockPos FOCUS = new BlockPos(10, 64, -20);

	@Test
	void coversTheCubeAboveTheFloor() {
		assertTrue(RiteSpaceRules.covers(FOCUS, 2, FOCUS.above()));
		assertTrue(RiteSpaceRules.covers(FOCUS, 2, FOCUS.offset(2, 4, -2)), "far top corner");
		assertTrue(RiteSpaceRules.covers(FOCUS, 2, FOCUS.offset(-2, 1, 2)), "near bottom corner");
	}

	@Test
	void excludesTheFloorItselfAndEverythingOutsideTheCube() {
		assertFalse(RiteSpaceRules.covers(FOCUS, 2, FOCUS), "the floor layer is not in the space");
		assertFalse(RiteSpaceRules.covers(FOCUS, 2, FOCUS.offset(0, 5, 0)), "one above the cube");
		assertFalse(RiteSpaceRules.covers(FOCUS, 2, FOCUS.offset(3, 1, 0)), "one past the floor's edge");
		assertFalse(RiteSpaceRules.covers(FOCUS, 2, FOCUS.offset(0, 1, -3)));
		assertFalse(RiteSpaceRules.covers(FOCUS, 2, FOCUS.below()));
	}

	@Test
	void keepsTheSpaceWhileTheSameFloorStaysFormed() {
		ResourceLocation floor = ResourceLocation.fromNamespaceAndPath("hemomancy", "working_lesser");
		assertFalse(RiteSpaceRules.shouldRelease(floor, ResourceLocation.fromNamespaceAndPath("hemomancy", "working_lesser")));
	}

	@Test
	void releasesWhenFloorIdChanges() {
		ResourceLocation floor = ResourceLocation.fromNamespaceAndPath("hemomancy", "working_lesser");
		assertTrue(RiteSpaceRules.shouldRelease(floor, null), "a broken floor releases its space");
		assertTrue(RiteSpaceRules.shouldRelease(floor, ResourceLocation.fromNamespaceAndPath("hemomancy", "working_greater")),
				"a floor grown to a higher tier is a different floor: the smaller cube it consecrated is released");
		assertTrue(RiteSpaceRules.shouldRelease(floor, ResourceLocation.fromNamespaceAndPath("hemomancy", "dominion_lesser")));
	}
}

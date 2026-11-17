package com.vincenthuto.hemomancy.common.capability.player.harbinger.cling;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClingRulesTest {
	@Test
	void wallsAndCeilingsHoldButFloorsDoNot() {
		assertEquals(ClingRules.Surface.NONE, ClingRules.Surface.forFace(Direction.UP));
		assertEquals(ClingRules.Surface.CEILING, ClingRules.Surface.forFace(Direction.DOWN));
		for (Direction side : Direction.Plane.HORIZONTAL) assertEquals(ClingRules.Surface.WALL, ClingRules.Surface.forFace(side));
	}

	@Test
	void cleatsHalveTheUpkeepAndQuickenTheWall() {
		assertEquals(4.0D, ClingRules.upkeepPerSecond(ClingRules.Surface.WALL, false));
		assertEquals(2.0D, ClingRules.upkeepPerSecond(ClingRules.Surface.WALL, true));
		assertEquals(6.0D, ClingRules.upkeepPerSecond(ClingRules.Surface.CEILING, false));
		assertEquals(3.0D, ClingRules.upkeepPerSecond(ClingRules.Surface.CEILING, true));
		assertEquals(0.0D, ClingRules.upkeepPerSecond(ClingRules.Surface.NONE, true));
		assertTrue(ClingRules.speed(ClingRules.Surface.WALL, true) > 2.5D * ClingRules.speed(ClingRules.Surface.WALL, false),
				"cleats bring a wall near walking pace");
		assertTrue(ClingRules.speed(ClingRules.Surface.WALL, true) < 0.22D, "but never past a walk");
	}

	@Test
	void walkingIntoAWallClimbsAndAwayDescends() {
		// Facing north (yaw 180) into a wall whose face points south (+Z).
		Vec3 forward = ClingRules.intent(1.0F, 0.0F, 180.0F);
		assertEquals(-1.0D, forward.z, 1.0E-6D);
		Vec3 climb = ClingRules.crawl(Direction.SOUTH, forward, 0.1D);
		assertEquals(0.1D, climb.y, 1.0E-6D, "toward the wall is up");
		assertEquals(-ClingRules.PRESS, climb.z, 1.0E-6D, "pressed into the face");
		Vec3 back = ClingRules.crawl(Direction.SOUTH, ClingRules.intent(-1.0F, 0.0F, 180.0F), 0.1D);
		assertEquals(-0.1D, back.y, 1.0E-6D, "away from the wall is down");
		Vec3 side = ClingRules.crawl(Direction.SOUTH, ClingRules.intent(0.0F, 1.0F, 180.0F), 0.1D);
		assertEquals(0.0D, side.y, 1.0E-6D);
		assertEquals(0.1D, Math.abs(side.x), 1.0E-6D, "sideways stays sideways");
		Vec3 diagonal = ClingRules.crawl(Direction.SOUTH, ClingRules.intent(1.0F, 1.0F, 180.0F), 0.1D);
		assertEquals(0.1D, new Vec3(diagonal.x, diagonal.y, 0.0D).length(), 1.0E-6D, "no faster on a diagonal");
	}

	@Test
	void underACeilingWalkingShufflesAndTheBodyPressesUp() {
		Vec3 crawl = ClingRules.crawl(Direction.DOWN, ClingRules.intent(1.0F, 0.0F, 0.0F), 0.05D);
		assertEquals(0.05D, crawl.z, 1.0E-6D);
		assertEquals(ClingRules.PRESS, crawl.y, 1.0E-6D);
		Vec3 still = ClingRules.crawl(Direction.DOWN, Vec3.ZERO, 0.05D);
		assertEquals(0.0D, still.horizontalDistance(), 1.0E-9D, "a still hang does not drift");
	}

	@Test
	void jumpingPushesOffAndHoldfastNeverSlides() {
		Vec3 off = ClingRules.jumpOff(Direction.SOUTH);
		assertTrue(off.z > 0.0D && off.y > 0.0D, "out from the wall and up");
		assertTrue(ClingRules.jumpOff(Direction.DOWN).y < 0.0D, "a drop from a ceiling");
		assertEquals(0.0D, ClingRules.holdfastVertical(-0.3D));
		assertEquals(0.2D, ClingRules.holdfastVertical(0.2D));
	}
}

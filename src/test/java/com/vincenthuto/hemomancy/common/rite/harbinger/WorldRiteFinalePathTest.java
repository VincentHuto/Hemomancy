package com.vincenthuto.hemomancy.common.rite.harbinger;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class WorldRiteFinalePathTest {
	private static final Vec3 HOVER = new Vec3(0.5D, 66.5D, 0.5D);
	private static final Vec3 GROUND = new Vec3(0.5D, 65.0D, 0.5D);
	private static final int GATHER = CardinalRiteFinaleTiming.GROWTH_TICKS;
	private static final int IMPACT = CardinalRiteFinaleTiming.TOTAL_TICKS;

	@Test
	void theDaemonGathersOverTheStaffFirst() {
		assertEquals(HOVER, WorldRiteFinalePath.skyPosition(GATHER, HOVER, 300.0D));
		assertEquals(HOVER, WorldRiteFinalePath.groundPosition(GATHER, HOVER, GROUND));
	}

	@Test
	void aSkyRiteShootsTheDaemonIntoTheSkyAndSpeedsUp() {
		Vec3 top = WorldRiteFinalePath.skyPosition(IMPACT, HOVER, 300.0D);
		assertEquals(HOVER.y + WorldRiteFinalePath.SKY_HEIGHT, top.y, 1.0E-6, "it strikes the sky at the impact");
		double half = (GATHER + IMPACT) / 2.0D;
		double firstHalf = WorldRiteFinalePath.skyPosition(half, HOVER, 300.0D).y - HOVER.y;
		assertTrue(firstHalf < (top.y - HOVER.y) * 0.35D, "accelerates upward rather than drifting");
		assertEquals(120.0D, WorldRiteFinalePath.skyPosition(IMPACT, HOVER, 120.0D).y, 1.0E-6,
				"never flies past the world's ceiling");
	}

	@Test
	void aGroundRiteClimbsThenSlamsIntoTheFloor() {
		double apex = 0.0D;
		for (int tick = GATHER; tick <= IMPACT; tick++) {
			apex = Math.max(apex, WorldRiteFinalePath.groundPosition(tick, HOVER, GROUND).y);
		}
		assertTrue(apex >= HOVER.y + 6.0D, "climbs well above the staff before the slam: " + apex);
		assertTrue(WorldRiteFinalePath.groundPosition(IMPACT, HOVER, GROUND).distanceTo(GROUND) < 1.0E-6, "lands on the floor");
		double lastStep = WorldRiteFinalePath.groundPosition(IMPACT - 1, HOVER, GROUND).y
				- WorldRiteFinalePath.groundPosition(IMPACT, HOVER, GROUND).y;
		double earlierStep = WorldRiteFinalePath.groundPosition(IMPACT - 6, HOVER, GROUND).y
				- WorldRiteFinalePath.groundPosition(IMPACT - 5, HOVER, GROUND).y;
		assertTrue(lastStep > earlierStep * 2.0D, "the slam accelerates into the ground");
	}
}

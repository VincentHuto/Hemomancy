package com.vincenthuto.hemomancy.common.manipulation.verb;

import com.vincenthuto.hemomancy.common.event.SanguineProjectionTargeting;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffFocusProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Track 1: one reach for both verbs, grown by the Staff and by Sanguine Reach, scaled but never replaced by a Sculpting. */
class VerbReachTest {
	private static final double EPSILON = 1.0E-9D;

	@Test
	void theBareHandReachesTheOldProjectionReach() {
		assertEquals(SanguineProjectionTargeting.PROJECTION_REACH, VerbReach.compute(false, null, 1.0D), EPSILON);
		assertEquals(5.5D, VerbReach.BASE, EPSILON);
	}

	@Test
	void theStaffReachesItsFocusRange() {
		assertEquals(6.0D, VerbReach.compute(true, LivingStaffFocusProfile.NONE, 1.0D), EPSILON);
		LivingStaffFocusProfile skilled = new LivingStaffFocusProfile(3, 0, 0, 2, 0, false);
		assertEquals(6.0D + 3 * 1.5D + 2 * 0.75D, VerbReach.compute(true, skilled, 1.0D), EPSILON);
	}

	@Test
	void sanguineReachMultipliesEitherTool() {
		assertEquals(5.5D * 1.45D, VerbReach.compute(false, null, 1.45D), EPSILON);
		assertEquals(6.0D * 1.15D, VerbReach.compute(true, LivingStaffFocusProfile.NONE, 1.15D), EPSILON);
		assertEquals(5.5D, VerbReach.compute(false, null, 0.5D), EPSILON, "a multiplier never shortens reach");
		assertEquals(5.5D, VerbReach.compute(false, null, Double.NaN), EPSILON);
	}

	@Test
	void aSculptingScalesReachWithinBounds() {
		assertEquals(11.0D, VerbReach.scaled(5.5D, 2.0D), EPSILON);
		assertEquals(5.5D * VerbReach.MAX_SCALE, VerbReach.scaled(5.5D, 100.0D), EPSILON);
		assertEquals(5.5D * VerbReach.MIN_SCALE, VerbReach.scaled(5.5D, 0.0D), EPSILON);
		assertEquals(5.5D, VerbReach.scaled(5.5D, Double.POSITIVE_INFINITY), EPSILON);
		assertTrue(VerbReach.scaled(5.5D, 0.5D) < 5.5D, "a touch may reach less than the verb");
	}
}

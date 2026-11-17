package com.vincenthuto.hemomancy.common.entity.summon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

/** FX-28: following bodies stand in a row behind the owner's back, the Hulk in a row of its own further back. */
class FollowSlotTest {
	private static void assertVec(Vec3 expected, Vec3 actual) {
		assertEquals(expected.x, actual.x, 1.0E-9, "x of " + actual);
		assertEquals(expected.y, actual.y, 1.0E-9, "y of " + actual);
		assertEquals(expected.z, actual.z, 1.0E-9, "z of " + actual);
	}

	@Test
	void aLoneBodyStandsStraightBehindTheOwner() {
		// Yaw 0 faces +Z, so behind is -Z.
		assertVec(new Vec3(0, 0, -2.5D), BoundSummonBehavior.slotOffset(0, 1, false, 0.0F));
		assertVec(new Vec3(0, 0, -4.5D), BoundSummonBehavior.slotOffset(0, 1, true, 0.0F));
	}

	@Test
	void aRowFansOutSideToSideAtTheSpacing() {
		Vec3 left = BoundSummonBehavior.slotOffset(0, 3, false, 0.0F);
		Vec3 middle = BoundSummonBehavior.slotOffset(1, 3, false, 0.0F);
		Vec3 right = BoundSummonBehavior.slotOffset(2, 3, false, 0.0F);
		assertVec(new Vec3(-1.6D, 0, -2.5D), left);
		assertVec(new Vec3(0, 0, -2.5D), middle);
		assertVec(new Vec3(1.6D, 0, -2.5D), right);
		assertVec(new Vec3(-0.8D, 0, -2.5D), BoundSummonBehavior.slotOffset(0, 2, false, 0.0F));
	}

	@Test
	void theRowTurnsWithTheOwner() {
		// Yaw 90 faces -X, so behind is +X and the row runs along Z.
		assertVec(new Vec3(2.5D, 0, 0), BoundSummonBehavior.slotOffset(0, 1, false, 90.0F));
		assertVec(new Vec3(2.5D, 0, -1.6D), BoundSummonBehavior.slotOffset(0, 3, false, 90.0F));
	}
}

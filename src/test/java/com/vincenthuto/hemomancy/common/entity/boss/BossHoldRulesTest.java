package com.vincenthuto.hemomancy.common.entity.boss;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The boss hold's pure rules: the pin point from offset and yaw, the slack, release reasons and refusals. */
final class BossHoldRulesTest {
	@Test
	void thePointIsForwardUpAndRightOfTheBody() {
		Vec3 feet = new Vec3(10, 64, 10);
		Vec3 south = BossHold.point(feet, 0.0F, new Vec3(2, 1, 0));
		assertEquals(10.0D, south.x, 1.0E-9);
		assertEquals(65.0D, south.y, 1.0E-9);
		assertEquals(12.0D, south.z, 1.0E-9, "yaw 0 faces south");
		Vec3 right = BossHold.point(feet, 0.0F, new Vec3(0, 0, 1.5));
		assertEquals(8.5D, right.x, 1.0E-9, "facing south, the right hand is west");
		Vec3 west = BossHold.point(feet, 90.0F, new Vec3(2, 0, 0));
		assertEquals(8.0D, west.x, 1.0E-9);
		assertEquals(10.0D, west.z, 1.0E-9);
	}

	@Test
	void theSlackDecidesThePin() {
		assertFalse(BossHold.pinned(new Vec3(0, 0, 0.4), Vec3.ZERO, 0.5D));
		assertTrue(BossHold.pinned(new Vec3(0, 0, 0.6), Vec3.ZERO, 0.5D));
	}

	@Test
	void releaseReasons() {
		assertEquals(BossHold.Reason.LOST, BossHold.ends(false, false, 3, 40));
		assertEquals(BossHold.Reason.BROKEN, BossHold.ends(true, true, 3, 40));
		assertEquals(BossHold.Reason.DONE, BossHold.ends(true, false, 40, 40));
		assertNull(BossHold.ends(true, false, 39, 40));
	}

	@Test
	void refusals() {
		assertFalse(BossHold.refuses(false, false, false, false));
		assertTrue(BossHold.refuses(true, false, false, false), "a spectator");
		assertTrue(BossHold.refuses(false, true, false, false), "a creative player");
		assertTrue(BossHold.refuses(false, false, true, false), "a player already held");
		assertTrue(BossHold.refuses(false, false, false, true), "a HeldTraveller");
		UUID player = new UUID(3, 4);
		assertFalse(BossHold.isHeld(player));
		BossHold.logout(player);
		BossHold.clearSessionState();
		assertFalse(BossHold.isHeld(player));
	}
}

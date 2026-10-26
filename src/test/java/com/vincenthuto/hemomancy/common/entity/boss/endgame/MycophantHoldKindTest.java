package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantHoldKindTest {
	private static void assertPoint(Vec3 expected, Vec3 actual) {
		assertEquals(expected.x, actual.x, 1.0E-5D, "x");
		assertEquals(expected.y, actual.y, 1.0E-5D, "y");
		assertEquals(expected.z, actual.z, 1.0E-5D, "z");
	}

	@Test
	void onlyTheSacIsMeteredAndBreakable() {
		assertTrue(MycophantHoldKind.SAC.metered() && MycophantHoldKind.SAC.breakable());
		assertFalse(MycophantHoldKind.PIN.metered() || MycophantHoldKind.PIN.breakable());
		assertFalse(MycophantHoldKind.COCOON.metered() || MycophantHoldKind.COCOON.breakable());
	}

	@Test
	void durations() {
		assertEquals(-1, MycophantHoldKind.SAC.durationTicks());
		assertEquals(20, MycophantHoldKind.PIN.durationTicks());
		assertEquals(60, MycophantHoldKind.COCOON.durationTicks());
	}

	@Test
	void eachKindIsHeldThroughItsOwnMoves() {
		assertTrue(MycophantHoldKind.SAC.heldThrough(MycophantAttack.RUSHING_EMBRACE));
		assertTrue(MycophantHoldKind.SAC.heldThrough(MycophantAttack.FUSION_EMBRACE));
		assertFalse(MycophantHoldKind.SAC.heldThrough(MycophantAttack.CLAW_PIN));
		assertTrue(MycophantHoldKind.PIN.heldThrough(MycophantAttack.CLAW_PIN));
		assertFalse(MycophantHoldKind.PIN.heldThrough(MycophantAttack.PLUCK));
		assertTrue(MycophantHoldKind.COCOON.heldThrough(MycophantAttack.LAST_BROOD));
		assertFalse(MycophantHoldKind.COCOON.heldThrough(MycophantAttack.RUSHING_EMBRACE));
	}

	@Test
	void pinPointIsUnderTheClawOnTheFloor() {
		Vec3 feet = new Vec3(10.0D, 64.0D, -5.0D);
		// MycophantEntity.holdPoint(feet, yaw, kind) is kind.point(feet, yaw); the entity class cannot load in a plain JVM.
		// yaw 0 faces +z and her right is -x: the pin sits under her right front claw, 1.6 along -x
		assertPoint(feet.add(-1.6D, 0.0D, 3.6D), MycophantHoldKind.PIN.point(feet, 0.0F));
		// yaw 90 faces -x, and her right is then -z
		assertPoint(feet.add(-3.6D, 0.0D, -1.6D), MycophantHoldKind.PIN.point(feet, 90.0F));
	}

	@Test
	void sacAndCocoonKeepTheSacPoint() {
		Vec3 feet = new Vec3(3.0D, 70.0D, 8.0D);
		for (float yaw = -180.0F; yaw <= 180.0F; yaw += 22.5F) {
			// the sac point as MycophantEntity.holdPoint(feet, yaw) placed it before hold kinds
			float rad = yaw * Mth.DEG_TO_RAD;
			Vec3 sac = feet.add(-Mth.sin(rad) * 2.7D, 0.55D, Mth.cos(rad) * 2.7D);
			assertPoint(sac, MycophantHoldKind.SAC.point(feet, yaw));
			assertPoint(sac, MycophantHoldKind.COCOON.point(feet, yaw));
		}
	}

	@Test
	void holdKindsReleaseWhenTheHeldPlayerIsGone() {
		for (MycophantHoldKind kind : MycophantHoldKind.values()) {
			assertTrue(MycophantHoldKind.shouldRelease(false, 0, kind), kind.name());
			assertTrue(MycophantHoldKind.shouldRelease(false, 5, kind), kind.name());
			assertFalse(MycophantHoldKind.shouldRelease(true, 0, kind), kind.name());
		}
		// the sac runs until the meter fills or it breaks, never on a clock
		assertFalse(MycophantHoldKind.shouldRelease(true, 10_000, MycophantHoldKind.SAC));
		assertFalse(MycophantHoldKind.shouldRelease(true, 19, MycophantHoldKind.PIN));
		assertTrue(MycophantHoldKind.shouldRelease(true, 20, MycophantHoldKind.PIN));
		assertFalse(MycophantHoldKind.shouldRelease(true, 59, MycophantHoldKind.COCOON));
		assertTrue(MycophantHoldKind.shouldRelease(true, 60, MycophantHoldKind.COCOON));
	}

	@Test
	void byOrdinalFallsBackToTheSac() {
		assertEquals(MycophantHoldKind.PIN, MycophantHoldKind.byOrdinal(1));
		assertEquals(MycophantHoldKind.SAC, MycophantHoldKind.byOrdinal(-1));
		assertEquals(MycophantHoldKind.SAC, MycophantHoldKind.byOrdinal(99));
	}
}

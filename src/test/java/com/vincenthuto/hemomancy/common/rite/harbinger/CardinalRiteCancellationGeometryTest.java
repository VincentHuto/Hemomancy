package com.vincenthuto.hemomancy.common.rite.harbinger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCancellationRules;

import net.minecraft.world.phys.Vec3;

class CardinalRiteCancellationGeometryTest {

	@Test
	void daemonContractsFromItsStartingPoseIntoTheStaff() {
		Vec3 start = new Vec3(8.0D, 75.0D, -3.0D);
		Vec3 staff = new Vec3(4.5D, 71.0D, -8.5D);

		assertEquals(start, CardinalRiteCancellationGeometry.daemonPosition(start, staff, 0));
		assertEquals(staff, CardinalRiteCancellationGeometry.daemonPosition(
				start, staff, CardinalRiteCancellationRules.DAEMON_ABSORPTION_TICKS));
		assertEquals(2.0F, CardinalRiteCancellationGeometry.daemonScale(2.0F, 0), 0.0001F);
		assertEquals(HumanitySpriteEntity.MIN_SCALE,
				CardinalRiteCancellationGeometry.daemonScale(
						2.0F, CardinalRiteCancellationRules.DAEMON_ABSORPTION_TICKS),
				0.0001F);
	}

	@Test
	void staffAndRiteFadeOnlyAfterDaemonIsAbsorbed() {
		int transition = CardinalRiteCancellationRules.DAEMON_ABSORPTION_TICKS;
		assertEquals(1.0F, CardinalRiteCancellationGeometry.staffScale(transition), 0.0001F);
		assertEquals(1.0F, CardinalRiteCancellationGeometry.riteOpacity(transition), 0.0001F);
		assertEquals(0.0F, CardinalRiteCancellationGeometry.staffScale(
				CardinalRiteCancellationRules.TOTAL_TICKS), 0.0001F);
		assertEquals(0.0F, CardinalRiteCancellationGeometry.riteOpacity(
				CardinalRiteCancellationRules.TOTAL_TICKS), 0.0001F);
	}

	@Test
	void interruptedDaemonRestoresToItsPreAbsorptionPoseOverTime() {
		Vec3 targetPosition = new Vec3(8.0D, 75.0D, -3.0D);
		Vec3 position = new Vec3(4.5D, 71.0D, -8.5D);
		float targetScale = 2.0F;
		float scale = HumanitySpriteEntity.MIN_SCALE;

		Vec3 firstPosition = CardinalRiteCancellationGeometry.recoveryPosition(
				position, targetPosition, CardinalRiteCancellationGeometry.RECOVERY_TICKS);
		float firstScale = CardinalRiteCancellationGeometry.recoveryScale(
				scale, targetScale, CardinalRiteCancellationGeometry.RECOVERY_TICKS);
		assertTrue(firstPosition.distanceTo(targetPosition) < position.distanceTo(targetPosition));
		assertTrue(firstPosition.distanceTo(targetPosition) > 0.0D,
				"the daemon must not snap back in one frame");
		assertTrue(firstScale > scale);
		assertTrue(firstScale < targetScale);

		position = firstPosition;
		scale = firstScale;
		for (int remaining = CardinalRiteCancellationGeometry.RECOVERY_TICKS - 1;
				remaining >= 1; remaining--) {
			position = CardinalRiteCancellationGeometry.recoveryPosition(
					position, targetPosition, remaining);
			scale = CardinalRiteCancellationGeometry.recoveryScale(
					scale, targetScale, remaining);
		}
		assertEquals(targetPosition, position);
		assertEquals(targetScale, scale, 0.0001F);
	}
}

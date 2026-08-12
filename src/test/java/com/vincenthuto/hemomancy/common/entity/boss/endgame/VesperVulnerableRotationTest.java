package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class VesperVulnerableRotationTest {
	@Test
	void captureRetainsEveryCardinalYawAndClearDisablesTheLock() {
		for (float yaw : new float[] { 0.0F, 90.0F, 180.0F, -90.0F }) {
			VesperVulnerableRotation lock = VesperVulnerableRotation.capture(yaw);
			assertTrue(lock.active());
			assertEquals(yaw, lock.yaw(), 0.001F);
			assertFalse(lock.clear().active());
		}
	}

	@Test
	void activeLockRoundTripsThroughPersistentData() {
		CompoundTag tag = new CompoundTag();
		VesperVulnerableRotation.capture(-137.5F).save(tag);

		VesperVulnerableRotation restored = VesperVulnerableRotation.load(tag, true, 22.0F);
		assertTrue(restored.active());
		assertEquals(-137.5F, restored.yaw(), 0.001F);
	}

	@Test
	void inactiveLoadClearsStaleYawAndLegacyActiveLoadCapturesCurrentHeading() {
		CompoundTag stale = new CompoundTag();
		VesperVulnerableRotation.capture(45.0F).save(stale);
		assertFalse(VesperVulnerableRotation.load(stale, false, 90.0F).active());

		VesperVulnerableRotation legacy = VesperVulnerableRotation.load(new CompoundTag(), true, 135.0F);
		assertTrue(legacy.active());
		assertEquals(135.0F, legacy.yaw(), 0.001F);
	}
}

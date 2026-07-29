package com.vincenthuto.hemomancy.common.event;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SanguineProjectionTargetingTest {
	@Test
	void currentLookRayPreservesEveryCardinalDirectionIncludingNegativeX() {
		Vec3 eye = new Vec3(10.0D, 65.0D, 10.0D);

		assertEquals(new Vec3(5.0D, 65.0D, 10.0D),
				SanguineProjectionTargeting.rayEnd(eye, new Vec3(-1, 0, 0), 5.0D));
		assertEquals(new Vec3(15.0D, 65.0D, 10.0D),
				SanguineProjectionTargeting.rayEnd(eye, new Vec3(1, 0, 0), 5.0D));
		assertEquals(new Vec3(10.0D, 65.0D, 5.0D),
				SanguineProjectionTargeting.rayEnd(eye, new Vec3(0, 0, -1), 5.0D));
		assertEquals(new Vec3(10.0D, 65.0D, 15.0D),
				SanguineProjectionTargeting.rayEnd(eye, new Vec3(0, 0, 1), 5.0D));
	}

	@Test
	void sharedProjectionReachCoversTheGameplayMaximum() {
		Vec3 eye = new Vec3(2.0D, 64.0D, -3.0D);

		assertEquals(new Vec3(2.0D, 69.5D, -3.0D),
				SanguineProjectionTargeting.rayEnd(eye, new Vec3(0.0D, 1.0D, 0.0D),
						SanguineProjectionTargeting.PROJECTION_REACH));
		assertEquals(new Vec3(2.0D, 58.5D, -3.0D),
				SanguineProjectionTargeting.rayEnd(eye, new Vec3(0.0D, -1.0D, 0.0D),
						SanguineProjectionTargeting.PROJECTION_REACH));
	}

	@Test
	void invalidDirectionAndNegativeReachCannotCreateBackwardRays() {
		Vec3 eye = new Vec3(2.0D, 64.0D, -3.0D);

		assertEquals(eye, SanguineProjectionTargeting.rayEnd(eye, Vec3.ZERO, 5.5D));
		assertEquals(eye, SanguineProjectionTargeting.rayEnd(eye, new Vec3(1.0D, 0.0D, 0.0D), -1.0D));
	}
}

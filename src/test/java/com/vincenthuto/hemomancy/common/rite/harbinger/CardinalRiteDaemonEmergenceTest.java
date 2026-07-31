package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

final class CardinalRiteDaemonEmergenceTest {
	@Test
	void daemonEmergenceBuildsFromAFullStaffSpiral() {
		assertEquals(0.0D, CardinalRiteDaemonEmergence.progress(0), 0.0001D);
		assertEquals(0.5D, CardinalRiteDaemonEmergence.progress(12), 0.0001D);
		assertEquals(1.0D, CardinalRiteDaemonEmergence.progress(24), 0.0001D);

		CardinalRiteDaemonEmergence.SpiralPoint lower =
				CardinalRiteDaemonEmergence.spiralPoint(4.5D, 8.5D, 3, 0, 0);
		CardinalRiteDaemonEmergence.SpiralPoint upper =
				CardinalRiteDaemonEmergence.spiralPoint(4.5D, 8.5D, 3, 7, 0);

		assertNotEquals(4.5D, lower.x(), 0.0001D);
		assertNotEquals(8.5D, lower.z(), 0.0001D);
		assertEquals(upper.y() - lower.y(), 1.75D, 0.0001D,
				"the spiral rises along the planted living staff");
	}
}

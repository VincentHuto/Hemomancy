package com.vincenthuto.hemomancy.common.entity.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HumanitySpriteEmissionScheduleTest {
	@Test
	void distributesOneCloudRefreshAcrossEveryTick() {
		int pointCount = 83;
		int interval = 8;
		boolean[] emitted = new boolean[pointCount];
		int totalEmissions = 0;

		for (int tick = 0; tick < interval; tick++) {
			int emissionsThisTick = 0;
			for (int point = 0; point < pointCount; point++) {
				if (!HumanitySpriteEmissionSchedule.isDue(point, tick, interval)) continue;
				assertTrue(!emitted[point], "a point must not be refreshed twice in one cadence cycle");
				emitted[point] = true;
				emissionsThisTick++;
				totalEmissions++;
			}
			assertTrue(emissionsThisTick >= 10 && emissionsThisTick <= 11,
					"every tick should receive a balanced visible slice");
		}

		assertEquals(pointCount, totalEmissions,
				"staggering must smooth the cloud without increasing its particle load");
		for (boolean wasEmitted : emitted) assertTrue(wasEmitted);
	}
}

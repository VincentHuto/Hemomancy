package com.vincenthuto.hemomancy.common.mission.shared;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BloodBeneathProgressTest {
	@Test
	void theBarFillsOnlyWhenAPathIsFinished() {
		assertEquals(13, BloodBeneathProgress.TOTAL);
		assertEquals(12, BloodBeneathProgress.movements(9, true, EnumArchonPath.SILENT_PENDING, false, true));
		assertEquals(12, BloodBeneathProgress.movements(9, true, EnumArchonPath.SILENT_PENDING, true, false));
		assertEquals(13, BloodBeneathProgress.movements(9, true, EnumArchonPath.SILENTIUM, false, true));
		assertEquals(13, BloodBeneathProgress.movements(9, true, EnumArchonPath.APOTHEOS, false, false));
		assertEquals(11, BloodBeneathProgress.movements(9, true, EnumArchonPath.APOTHEOS_PENDING, false, false));
	}
}

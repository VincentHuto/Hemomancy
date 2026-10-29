package com.vincenthuto.hemomancy.common.capability;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PathMutualExclusionEndgameTest {
	@Test
	void aResetLetsTheSilentPathBeEarnedAgain() {
		InitiatoryDegree degree = new InitiatoryDegree();
		degree.setArchonPath(EnumArchonPath.SILENTIUM);
		LivingStaffProgress staff = new LivingStaffProgress();
		staff.setVesperMemoryAwakened(true);

		PathMutualExclusionHelper.clearEndgame(degree, staff);

		assertEquals(EnumArchonPath.NONE, degree.getArchonPath());
		assertFalse(staff.isVesperMemoryAwakened());
	}
}

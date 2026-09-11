package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HarbingerPathPermissionsTest {
	@Test
	void completedEndingsRestoreSidePowersWithoutReopeningTheOtherFinale() {
		for (EnumArchonPath path : EnumArchonPath.values()) {
			boolean complete = path == EnumArchonPath.APOTHEOS || path == EnumArchonPath.SILENT_ARCHON;
			assertEquals(complete, HarbingerPathPermissions.canUsePower(9, path, false), path.name());
			assertEquals(complete, HarbingerPathPermissions.canPerformRite(9, path, false), path.name());
			assertTrue(HarbingerPathPermissions.canUsePower(9, path, true), "Staff recovery: " + path);
			assertEquals(path == EnumArchonPath.APOTHEOS_PENDING,
					HarbingerPathPermissions.canPerformRite(9, path, true), path.name());
		}
	}

	@Test
	void ordinaryProgressionDoesNotNeedAnEndingChoice() {
		for (int pomes = 0; pomes < 9; pomes++) {
			assertTrue(HarbingerPathPermissions.canUsePower(pomes, EnumArchonPath.NONE, false));
			assertTrue(HarbingerPathPermissions.canPerformRite(pomes, EnumArchonPath.NONE, false));
			assertFalse(HarbingerPathPermissions.canPerformRite(pomes, EnumArchonPath.NONE, true));
		}
	}
}

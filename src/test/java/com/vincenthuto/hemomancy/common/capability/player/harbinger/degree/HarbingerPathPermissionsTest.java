package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HarbingerPathPermissionsTest {
	@Test
	void completedEndingsRestoreSidePowersWithoutReopeningTheOtherFinale() {
		for (EnumArchonPath path : EnumArchonPath.values()) {
			boolean complete = path == EnumArchonPath.APOTHEOS || path == EnumArchonPath.SILENTIUM;
			assertEquals(complete, HarbingerPathPermissions.canUsePower(9, path, false), path.name());
			assertEquals(complete, HarbingerPathPermissions.canPerformRite(9, path, false), path.name());
			assertTrue(HarbingerPathPermissions.canUsePower(9, path, true), "Staff recovery: " + path);
			assertEquals(path == EnumArchonPath.APOTHEOS_PENDING,
					HarbingerPathPermissions.canPerformRite(9, path, true), path.name());
		}
	}

	@Test
	void eachEighthDegreePathIsNamedOnlyByItsOwnFlag() {
		assertTrue(HarbingerPathPermissions.isApotheos(8, EnumArchonPath.APOTHEOS));
		assertFalse(HarbingerPathPermissions.isApotheos(7, EnumArchonPath.APOTHEOS));
		assertFalse(HarbingerPathPermissions.isApotheos(8, EnumArchonPath.SILENTIUM));
	}

	@Test
	void teachersGreetASilentiumWithTheArchonTreeNotTheApotheosOne() {
		assertEquals(7, HarbingerPathPermissions.greetingDegree(8, EnumArchonPath.SILENTIUM));
		assertEquals(8, HarbingerPathPermissions.greetingDegree(8, EnumArchonPath.APOTHEOS));
		assertEquals(7, HarbingerPathPermissions.greetingDegree(7, EnumArchonPath.SILENT_PENDING));
		assertEquals(3, HarbingerPathPermissions.greetingDegree(3, EnumArchonPath.NONE));
	}

	@Test
	void onlyAFinishedSilentiumStopsHearingTheHive() {
		assertFalse(HarbingerPathPermissions.hearsTheHive(8, EnumArchonPath.SILENTIUM));
		assertTrue(HarbingerPathPermissions.hearsTheHive(8, EnumArchonPath.APOTHEOS));
		assertTrue(HarbingerPathPermissions.hearsTheHive(7, EnumArchonPath.SILENT_PENDING),
				"A Silence still pending has not cut the antenna yet");
		assertTrue(HarbingerPathPermissions.hearsTheHive(7, EnumArchonPath.SILENTIUM),
				"Silentium is an eighth-degree state");
		for (int degree = 0; degree <= 7; degree++) {
			assertTrue(HarbingerPathPermissions.hearsTheHive(degree, EnumArchonPath.NONE), "Degree " + degree);
		}
	}

	@Test
	void theHiveAddressesAnApotheosByTheirOwnTitle() {
		assertEquals(".apotheos", HarbingerPathPermissions.hiveAddressSuffix(8, EnumArchonPath.APOTHEOS));
		assertEquals("", HarbingerPathPermissions.hiveAddressSuffix(7, EnumArchonPath.APOTHEOS_PENDING));
		assertEquals("", HarbingerPathPermissions.hiveAddressSuffix(7, EnumArchonPath.NONE));
		assertEquals("", HarbingerPathPermissions.hiveAddressSuffix(8, EnumArchonPath.SILENTIUM));
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

package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DegreeTitlesTest {
	@Test
	void theEighthDegreeIsTitledByItsPath() {
		assertEquals("hemomancy.degree.8.apotheos", DegreeTitles.titleKey(8, EnumArchonPath.APOTHEOS));
		assertEquals("hemomancy.degree.8.silentium", DegreeTitles.titleKey(8, EnumArchonPath.SILENTIUM));
		assertEquals("hemomancy.degree.8", DegreeTitles.titleKey(8, EnumArchonPath.NONE));
	}

	@Test
	void lowerDegreesKeepTheirSharedTitle() {
		assertEquals("hemomancy.degree.7", DegreeTitles.titleKey(7, EnumArchonPath.SILENT_PENDING));
		assertEquals("hemomancy.degree.0", DegreeTitles.titleKey(-3, EnumArchonPath.NONE));
		assertEquals("hemomancy.degree.8", DegreeTitles.titleKey(12, EnumArchonPath.NONE));
	}
}

package com.vincenthuto.hemomancy.testing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegacyMainTestSuiteContractTest {
	@Test
	void discoversEveryExistingLegacyMainTest() throws Exception {
		assertEquals(351, LegacyMainTestAdapterTest.discoverLegacyMainClasses().size());
	}
}

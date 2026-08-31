package com.vincenthuto.hemomancy.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LegacyMainTestSuiteContractTest {
	@Test
	void discoversEveryExistingLegacyMainTest() throws Exception {
		assertEquals(351, LegacyMainTestAdapterTest.discoverLegacyMainClasses().size());
	}
}

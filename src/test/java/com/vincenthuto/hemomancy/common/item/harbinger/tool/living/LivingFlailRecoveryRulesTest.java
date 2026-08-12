package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LivingFlailRecoveryRulesTest {
	@Test
	void staleMissingMismatchedAndRestoredDeploymentsRecoverTheHead() throws Exception {
		Method recover = Class.forName("com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingFlailRecoveryRules")
				.getMethod("shouldRecover", boolean.class, boolean.class, boolean.class, boolean.class);
		assertFalse((boolean) recover.invoke(null, false, false, false, false));
		assertFalse((boolean) recover.invoke(null, true, true, true, false));
		assertTrue((boolean) recover.invoke(null, true, false, true, false));
		assertTrue((boolean) recover.invoke(null, true, true, false, false));
		assertTrue((boolean) recover.invoke(null, true, true, true, true));
	}
}

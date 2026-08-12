package com.vincenthuto.hemomancy.common.entity.projectile;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LivingFlailImpactRulesTest {
	private static Class<?> rules() throws ClassNotFoundException {
		return Class.forName("com.vincenthuto.hemomancy.common.entity.projectile.LivingFlailImpactRules");
	}

	@Test
	void collisionCanOnlyImpactOnce() throws Exception {
		Method mayImpact = rules().getMethod("mayImpact", boolean.class);
		assertTrue((boolean) mayImpact.invoke(null, false));
		assertFalse((boolean) mayImpact.invoke(null, true));
	}

	@Test
	void ownerAlliesAndInvalidTargetsAreFiltered() throws Exception {
		Method valid = rules().getMethod("isValidTarget", boolean.class, boolean.class, boolean.class, boolean.class);
		assertTrue((boolean) valid.invoke(null, false, false, true, true));
		assertFalse((boolean) valid.invoke(null, true, false, true, true));
		assertFalse((boolean) valid.invoke(null, false, true, true, true));
		assertFalse((boolean) valid.invoke(null, false, false, false, true));
		assertFalse((boolean) valid.invoke(null, false, false, true, false));
	}

	@Test
	void timeoutUsesReducedGroundImpact() throws Exception {
		Method scale = rules().getMethod("timeoutImpactScale", boolean.class);
		assertEquals(1.0F, ((Number) scale.invoke(null, false)).floatValue(), 0.0001F);
		assertEquals(0.65F, ((Number) scale.invoke(null, true)).floatValue(), 0.0001F);
	}
}

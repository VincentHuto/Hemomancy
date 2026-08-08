package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

final class VesperPhaseTransitionRulesTest {
	@Test
	void dismountFinishesBeforeMountAbsorptionAndPhaseTwoWaitsForBoth() throws Exception {
		Class<?> rules;
		try {
			rules = Class.forName("com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseTransitionRules");
		} catch (ClassNotFoundException missing) {
			fail("Vesper phase transition rules are missing");
			return;
		}

		Method dismount = rules.getMethod("dismountProgress", float.class);
		Method absorption = rules.getMethod("absorptionProgress", float.class);
		Method complete = rules.getMethod("isComplete", int.class);

		assertEquals(0.0F, (float) dismount.invoke(null, 0.0F), 0.001F);
		assertEquals(0.5F, (float) dismount.invoke(null, 18.0F), 0.001F);
		assertEquals(1.0F, (float) dismount.invoke(null, 36.0F), 0.001F);
		assertEquals(0.0F, (float) absorption.invoke(null, 36.0F), 0.001F);
		assertEquals(0.5F, (float) absorption.invoke(null, 78.0F), 0.001F);
		assertEquals(1.0F, (float) absorption.invoke(null, 120.0F), 0.001F);
		assertFalse((boolean) complete.invoke(null, 119));
		assertTrue((boolean) complete.invoke(null, 120));
	}
}

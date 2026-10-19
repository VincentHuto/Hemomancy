package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

final class VesperRageCombatRulesTest {
	@Test
	void rageStartsAtTheLastThirdOfHealth() throws Exception {
		Method threshold = rules().getMethod("isRageThreshold", float.class, float.class);
		assertFalse((boolean) threshold.invoke(null, 213.4F, 640.0F));
		assertTrue((boolean) threshold.invoke(null, 213.3F, 640.0F));
		assertFalse((boolean) threshold.invoke(null, 1.0F, 0.0F));
	}

	@Test
	void everyRageAttackKeepsAReadableWindupAndShortRecovery() {
		for (String name : new String[] {
				"SICKLE_CYCLONE", "SICKLE_POUNCE", "SICKLE_CROSS_REND", "SICKLE_HOOK" }) {
			VesperWeaponAction action = action(name);
			assertTrue(action.impactTick() >= 10, name);
			assertTrue(action.durationTicks() - action.lastImpactTick() >= 8, name);
		}
	}

	private static Class<?> rules() {
		try {
			return Class.forName("com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperRageCombatRules");
		} catch (ClassNotFoundException missing) {
			return fail("Vesper rage combat rules are missing");
		}
	}

	private static VesperWeaponAction action(String name) {
		try {
			return VesperWeaponAction.valueOf(name);
		} catch (IllegalArgumentException missing) {
			return fail("Missing rage action " + name);
		}
	}
}

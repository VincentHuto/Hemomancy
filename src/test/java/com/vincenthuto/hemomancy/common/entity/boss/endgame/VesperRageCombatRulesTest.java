package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

final class VesperRageCombatRulesTest {
	@Test
	void rageStartsOnlyBelowThirtyPercentHealth() throws Exception {
		Method threshold = rules().getMethod("isRageThreshold", float.class, float.class);
		assertFalse((boolean) threshold.invoke(null, 192.0F, 640.0F));
		assertTrue((boolean) threshold.invoke(null, 191.9F, 640.0F));
		assertFalse((boolean) threshold.invoke(null, 1.0F, 0.0F));
	}

	@Test
	void rageAlternatesCloseAndLongRangeSickleAttacksWithoutRepeating() throws Exception {
		Method select = rules().getMethod("selectAction", VesperWeaponAction.class, double.class, int.class);
		VesperWeaponAction cyclone = action("SICKLE_CYCLONE");
		VesperWeaponAction crossRend = action("SICKLE_CROSS_REND");
		VesperWeaponAction pounce = action("SICKLE_POUNCE");
		VesperWeaponAction hook = action("SICKLE_HOOK");

		assertEquals(cyclone, select.invoke(null, VesperWeaponAction.NONE, 3.0D, 0));
		assertEquals(crossRend, select.invoke(null, cyclone, 3.0D, 0));
		assertEquals(hook, select.invoke(null, VesperWeaponAction.NONE, 14.0D, 0));
		assertEquals(pounce, select.invoke(null, hook, 14.0D, 0));

		VesperWeaponAction previous = crossRend;
		VesperWeaponAction selected = (VesperWeaponAction) select.invoke(null, previous, 7.0D, 1);
		assertNotEquals(previous, selected);
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

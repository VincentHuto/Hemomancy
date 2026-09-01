package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

final class VesperWeaponEffectRulesTest {
	@Test
	void everyAnimatedWeaponActionSelectsItsOwnVisualLanguage() throws Exception {
		Method style = rules().getMethod("styleName", VesperWeaponAction.class);
		assertStyle(style, "blood_blade", VesperWeaponAction.ICHIMONJI, VesperWeaponAction.CROSSCUT);
		assertStyle(style, "living_axe", VesperWeaponAction.LEAPING_CLEAVE, VesperWeaponAction.REAPER_SWEEP);
		assertStyle(style, "living_spear", VesperWeaponAction.SKY_LANCE, VesperWeaponAction.LANCE_FLURRY);
		assertStyle(style, "gloam_claw", VesperWeaponAction.TWIN_REND, VesperWeaponAction.PREDATOR_POUNCE);
		assertStyle(style, "crimson_torch", VesperWeaponAction.BRANDING_THRUSTS,
				VesperWeaponAction.UPDRAFT_IMPALEMENT);
		assertStyle(style, "glacial_flail", VesperWeaponAction.CHAIN_SWEEP, VesperWeaponAction.HOOK_AND_CRUSH);
		assertStyle(style, "none", VesperWeaponAction.CONDUCTIVE_VOLLEY, VesperWeaponAction.STORM_LOCK,
				VesperWeaponAction.MAGNETIC_AXIS, VesperWeaponAction.IRON_RETORT);
	}

	@Test
	void effectsOccurDuringVisibleWeaponContactAndSpearFlight() throws Exception {
		Method emits = rules().getMethod("shouldEmit", VesperWeaponAction.class, int.class);
		assertFalse((boolean) emits.invoke(null, VesperWeaponAction.ICHIMONJI, 17));
		assertTrue((boolean) emits.invoke(null, VesperWeaponAction.ICHIMONJI, 18));
		assertTrue((boolean) emits.invoke(null, VesperWeaponAction.SKY_LANCE, 17));
		assertTrue((boolean) emits.invoke(null, VesperWeaponAction.SKY_LANCE, 23));
		assertTrue((boolean) emits.invoke(null, VesperWeaponAction.BRANDING_THRUSTS, 12));
		assertTrue((boolean) emits.invoke(null, VesperWeaponAction.CHAIN_SWEEP, 14));
		assertTrue((boolean) emits.invoke(null, VesperWeaponAction.CHAIN_SWEEP, 18));
		assertFalse((boolean) emits.invoke(null, VesperWeaponAction.CONDUCTIVE_VOLLEY, 20));
	}

	@Test
	void flailSweepUsesABossScaleWideArc() throws Exception {
		Method arc = rules().getMethod("arcDegrees", VesperWeaponAction.class);
		assertEquals(360.0D, (double) arc.invoke(null, VesperWeaponAction.CHAIN_SWEEP), 0.001D);
		assertEquals(210.0D, (double) arc.invoke(null, VesperWeaponAction.HOOK_AND_CRUSH), 0.001D);
	}

	private static void assertStyle(Method method, String expected, VesperWeaponAction... actions) throws Exception {
		for (VesperWeaponAction action : actions) assertEquals(expected, method.invoke(null, action), action.name());
	}

	private static Class<?> rules() {
		try {
			return Class.forName("com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponEffectRules");
		} catch (ClassNotFoundException missing) {
			return fail("Vesper weapon effect rules are missing");
		}
	}
}

package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;

public final class WillEquipmentRulesTest {
	private WillEquipmentRulesTest() {
	}

	public static void main(String[] args) {
		tierOneBrokenWillsUseLivingStaff();
		tierTwoBrokenWillsUseSchoolWeapons();
		sentWillsUseSchoolWeaponsAtEveryTier();
	}

	private static void tierOneBrokenWillsUseLivingStaff() {
		for (EnumBloodTendency school : EnumBloodTendency.values()) {
			assertEquals("living_staff", WillEquipmentRules.weaponId(WillOrigin.BROKEN, school, 1),
					"tier one broken " + school + " weapon");
		}
	}

	private static void tierTwoBrokenWillsUseSchoolWeapons() {
		assertSchoolWeapons(WillOrigin.BROKEN, 2);
		assertSchoolWeapons(WillOrigin.BROKEN, 4);
	}

	private static void sentWillsUseSchoolWeaponsAtEveryTier() {
		assertSchoolWeapons(WillOrigin.SENT, 1);
		assertSchoolWeapons(WillOrigin.SENT, 4);
	}

	private static void assertSchoolWeapons(WillOrigin origin, int tier) {
		assertEquals("living_blade", WillEquipmentRules.weaponId(origin, EnumBloodTendency.ANIMUS, tier),
				origin + " animus");
		assertEquals("living_torch", WillEquipmentRules.weaponId(origin, EnumBloodTendency.FLAMMEUS, tier),
				origin + " flammeus");
		assertEquals("living_crossbow", WillEquipmentRules.weaponId(origin, EnumBloodTendency.DUCTILIS, tier),
				origin + " ductilis");
		assertEquals("living_spear", WillEquipmentRules.weaponId(origin, EnumBloodTendency.LUX, tier),
				origin + " lux");
		assertEquals("living_axe", WillEquipmentRules.weaponId(origin, EnumBloodTendency.MORTEM, tier),
				origin + " mortem");
		assertEquals("living_flail", WillEquipmentRules.weaponId(origin, EnumBloodTendency.CONGEATIO, tier),
				origin + " congeatio");
		assertEquals("living_staff", WillEquipmentRules.weaponId(origin, EnumBloodTendency.FERRIC, tier),
				origin + " ferric");
		assertEquals("living_baghnakh", WillEquipmentRules.weaponId(origin, EnumBloodTendency.TENEBRIS, tier),
				origin + " tenebris");
	}

	private static void assertEquals(String expected, String actual, String label) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected `" + expected + "` but got `" + actual + "`");
		}
	}
}

package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;

public final class WillEquipmentRulesTest {
	private WillEquipmentRulesTest() {
	}

	public static void main(String[] args) {
		tierOneBrokenWillsUseLivingStaff();
		tierTwoBrokenWillsUseSchoolWeapons();
		sentWillsUseSchoolWeaponsAtEveryTier();
	}

	private static void tierOneBrokenWillsUseLivingStaff() {
		for (EnumBloodTemper school : EnumBloodTemper.values()) {
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
		assertEquals("living_blade", WillEquipmentRules.weaponId(origin, EnumBloodTemper.ANIMUS, tier),
				origin + " animus");
		assertEquals("living_torch", WillEquipmentRules.weaponId(origin, EnumBloodTemper.FLAMMEUS, tier),
				origin + " flammeus");
		assertEquals("living_crossbow", WillEquipmentRules.weaponId(origin, EnumBloodTemper.DUCTILIS, tier),
				origin + " ductilis");
		assertEquals("living_spear", WillEquipmentRules.weaponId(origin, EnumBloodTemper.LUX, tier),
				origin + " lux");
		assertEquals("living_sickle", WillEquipmentRules.weaponId(origin, EnumBloodTemper.MORTEM, tier),
				origin + " mortem");
		assertEquals("living_flail", WillEquipmentRules.weaponId(origin, EnumBloodTemper.CONGEATIO, tier),
				origin + " congeatio");
		assertEquals("living_axe", WillEquipmentRules.weaponId(origin, EnumBloodTemper.FERRIC, tier),
				origin + " ferric");
		assertEquals("living_baghnakh", WillEquipmentRules.weaponId(origin, EnumBloodTemper.TENEBRIS, tier),
				origin + " tenebris");
	}

	private static void assertEquals(String expected, String actual, String label) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected `" + expected + "` but got `" + actual + "`");
		}
	}
}

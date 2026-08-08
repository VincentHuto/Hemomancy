package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

final class VesperVisualRulesTest {
	@Test
	void everyVesperSceneUsesAuthoredParticleFamilies() throws Exception {
		Method families = rules().getMethod("families", String.class);
		assertFamilies(families, "crowned_ambient", "dark_glow", "sporitic_spore", "blood_cell");
		assertFamilies(families, "crowned_telegraph", "dark_glow", "blood_cell");
		assertFamilies(families, "throne_wound", "dark_glow", "blood_cell", "lightning");
		assertFamilies(families, "throne_break", "blood_cell", "dark_glow", "tendril", "lightning");
		assertFamilies(families, "transformation", "absorbed_blood_cell", "dark_glow", "tendril", "lightning");
		assertFamilies(families, "evening_ambient", "glow", "dark_glow", "blood_cell");
		assertFamilies(families, "stance_shift", "glow", "dark_glow", "blood_cell");
		assertFamilies(families, "rage", "blood_cell", "dark_glow", "ember", "tendril", "lightning");
		assertFamilies(families, "death", "dark_glow", "blood_cell", "ember", "lightning");
	}

	@Test
	void weaponMotifsSelectHemomancyEffectsInsteadOfGenericVanillaClouds() throws Exception {
		Method weaponScene = rules().getMethod("weaponScene", VesperWeaponAction.class);
		Method families = rules().getMethod("families", String.class);
		assertEquals("blood_blade", weaponScene.invoke(null, VesperWeaponAction.ICHIMONJI));
		assertEquals("living_spear", weaponScene.invoke(null, VesperWeaponAction.SKY_LANCE));
		assertEquals("gloam_claw", weaponScene.invoke(null, VesperWeaponAction.PREDATOR_POUNCE));
		assertEquals("crimson_torch", weaponScene.invoke(null, VesperWeaponAction.BRANDING_THRUSTS));
		assertEquals("glacial_flail", weaponScene.invoke(null, VesperWeaponAction.CHAIN_SWEEP));

		for (String scene : List.of("blood_blade", "living_spear", "gloam_claw", "crimson_torch", "glacial_flail")) {
			@SuppressWarnings("unchecked") List<String> sceneFamilies = (List<String>) families.invoke(null, scene);
			assertFalse(sceneFamilies.stream().anyMatch(family -> family.startsWith("vanilla_")), scene);
		}
	}

	@Test
	void blockDebrisIsReservedForPhysicalAxeImpacts() throws Exception {
		Method families = rules().getMethod("families", String.class);
		@SuppressWarnings("unchecked") List<String> axe = (List<String>) families.invoke(null, "living_axe");
		assertTrue(axe.contains("contextual_block_debris"));
		assertTrue(axe.contains("dark_glow"));
		assertTrue(axe.contains("ember"));
	}

	@Test
	void stanceColorsRemainDistinctAcrossAllEightTendencies() throws Exception {
		Method color = rules().getMethod("tendencyColorRgb", EnumBloodTendency.class);
		assertEquals(0xE00018, color.invoke(null, EnumBloodTendency.ANIMUS));
		assertEquals(0xFF6508, color.invoke(null, EnumBloodTendency.FLAMMEUS));
		assertEquals(0xF2E85C, color.invoke(null, EnumBloodTendency.DUCTILIS));
		assertEquals(0xF4F7FF, color.invoke(null, EnumBloodTendency.LUX));
		assertEquals(0x15522A, color.invoke(null, EnumBloodTendency.MORTEM));
		assertEquals(0x52BFE8, color.invoke(null, EnumBloodTendency.CONGEATIO));
		assertEquals(0x68646A, color.invoke(null, EnumBloodTendency.FERRIC));
		assertEquals(0x63108A, color.invoke(null, EnumBloodTendency.TENEBRIS));
	}

	private static void assertFamilies(Method method, String scene, String... expected) throws Exception {
		@SuppressWarnings("unchecked") List<String> actual = (List<String>) method.invoke(null, scene);
		assertEquals(List.of(expected), actual, scene);
	}

	private static Class<?> rules() {
		try {
			return Class.forName("com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperVisualRules");
		} catch (ClassNotFoundException missing) {
			return fail("Vesper authored visual rules are missing");
		}
	}
}

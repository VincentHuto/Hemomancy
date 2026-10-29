package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SilentArchonArmatureDegreeTest {
	@Test
	void silentArchonVestmentsShowTheEighthDegreeTheirGateRequires() {
		for (String slot : new String[] {"helm", "chestplate", "leggings", "boots"}) {
			assertEquals(8, ResourceReader.data("recipe/armature_upgrade/blood_lust_to_silent_archon_" + slot + ".json")
					.get("required_degree").getAsInt(), slot);
		}
	}
}

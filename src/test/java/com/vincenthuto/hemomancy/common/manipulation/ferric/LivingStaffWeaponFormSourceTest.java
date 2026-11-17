package com.vincenthuto.hemomancy.common.manipulation.ferric;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class LivingStaffWeaponFormSourceTest {

	private LivingStaffWeaponFormSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String manipInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java");
		String itemInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
		String changePacket = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/ChangeSelectedManipPacket.java");
		String updatePacket = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/UpdateCurrentManipPacket.java");
		String usePacket = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/UseManipKeyPacket.java");
		String formHelper = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffWeaponFormHelper.java");
		String tool = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingToolItem.java");
		String blade = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingBladeItem.java");
		String axe = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingAxeItem.java");
		String crossbow = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingCrossbowItem.java");

		assertContains("blade form uses staff weapon form manipulation", manipInit,
				"new StaffWeaponFormManip(\"conjure_blade\"");
		assertContains("axe form registered", manipInit, "conjure_axe");
		assertContains("spear form registered", manipInit, "conjure_spear");
		assertContains("claws form registered", manipInit, "conjure_claws");
		assertContains("crossbow form registered", manipInit, "conjure_crossbow");
		assertContains("selection cycling applies staff weapon form", changePacket,
				"LivingStaffWeaponFormHelper.applySelection");
		assertContains("radial selection applies staff weapon form", updatePacket,
				"LivingStaffWeaponFormHelper.applySelection");
		assertContains("unified use skips staff weapon forms", usePacket,
				"LivingStaffWeaponFormHelper.toggleSelectedForm");
		assertContains("form helper exposes R-key toggle", formHelper,
				"toggleSelectedForm(Player player, BloodManipulation selectedManip)");
		assertContains("form helper restores matching selected form", formHelper,
				"shouldToggleOffSelectedForm(selectedName, currentFormName");
		assertContains("base living tool restore hook", tool,
				"LivingStaffWeaponFormHelper.restoreStaffAfterBloodFailure");
		assertContains("blade restore hook", blade,
				"LivingStaffWeaponFormHelper.restoreStaffAfterBloodFailure");
		assertContains("axe restore hook", axe,
				"LivingStaffWeaponFormHelper.restoreStaffAfterBloodFailure");
		assertContains("crossbow restore hook", crossbow,
				"LivingStaffWeaponFormHelper.restoreStaffAfterBloodFailure");
	}
}

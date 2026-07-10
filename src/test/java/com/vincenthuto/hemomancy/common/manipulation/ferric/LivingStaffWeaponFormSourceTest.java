package com.vincenthuto.hemomancy.common.manipulation.ferric;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LivingStaffWeaponFormSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private LivingStaffWeaponFormSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String manipInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java");
		String itemInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
		String changePacket = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/ChangeSelectedManipPacket.java");
		String updatePacket = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/UpdateCurrentManipPacket.java");
		String usePacket = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/UseManipKeyPacket.java");
		String quickPacket = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/UseQuickManipKeyPacket.java");
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
		assertContains("axe memory registered", itemInit, "memory_living_axe");
		assertContains("spear memory registered", itemInit, "memory_living_spear");
		assertContains("claws memory registered", itemInit, "memory_living_claws");
		assertContains("crossbow memory registered", itemInit, "memory_living_crossbow");
		assertContains("selection cycling applies staff weapon form", changePacket,
				"LivingStaffWeaponFormHelper.applySelection");
		assertContains("radial selection applies staff weapon form", updatePacket,
				"LivingStaffWeaponFormHelper.applySelection");
		assertContains("unified use skips staff weapon forms", usePacket,
				"LivingStaffWeaponFormHelper.toggleSelectedForm");
		assertContains("quick use toggles staff weapon forms", quickPacket,
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

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}

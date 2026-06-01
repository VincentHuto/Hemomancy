package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LivingArsenalInventoryGuardSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private LivingArsenalInventoryGuardSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String guard = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingArsenalInventoryGuard.java");
		String conjuration = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/ConjurationManip.java");
		String events = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffWeaponFormEvents.java");
		String tool = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingToolItem.java");
		String staff = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffItem.java");
		String crossbow = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingCrossbowItem.java");
		String docs = read("docs/HEMOMANCY_REFERENCE.md");

		assertContains("guard class exists", guard, "public final class LivingArsenalInventoryGuard");
		assertContains("guard identifies living arsenal items", guard, "isLivingArsenalItem(ItemStack stack)");
		assertContains("guard sanitizes player inventory", guard, "sanitizePlayerInventory(Player player)");
		assertContains("guard restores non-player slots", guard, "restoreLivingArsenalFromNonPlayerSlots");
		assertContains("guard recovers staff on conjure", guard, "summonOrRecoverStaff");
		assertContains("guard knows living staff", guard, "ItemInit.living_staff");
		assertContains("guard knows living blade", guard, "ItemInit.living_blade");
		assertContains("guard knows living axe", guard, "ItemInit.living_axe");
		assertContains("guard knows living spear", guard, "ItemInit.living_spear");
		assertContains("guard knows living claws", guard, "ItemInit.living_baghnakh");
		assertContains("guard knows living crossbow", guard, "ItemInit.living_crossbow");
		assertContains("guard knows living torch", guard, "ItemInit.living_torch");
		assertContains("guard knows living flail", guard, "ItemInit.living_flail");

		assertContains("conjure staff uses guard", conjuration,
				"LivingArsenalInventoryGuard.summonOrRecoverStaff");
		assertContains("conjure staff branch remains scoped", conjuration,
				"item.get() == ItemInit.living_staff.get()");
		assertContains("container close guarded", events, "PlayerContainerEvent.Close");
		assertContains("player tick guarded", events, "PlayerTickEvent.Post");
		assertContains("container slots cleaned", events,
				"LivingArsenalInventoryGuard.restoreLivingArsenalFromNonPlayerSlots");
		assertContains("player inventory cleaned", events,
				"LivingArsenalInventoryGuard.sanitizePlayerInventory");

		assertContains("living tools reject container items", tool, "canFitInsideContainerItems()");
		assertContains("living staff rejects container items", staff, "canFitInsideContainerItems()");
		assertContains("living crossbow rejects container items", crossbow, "canFitInsideContainerItems()");
		assertContains("docs mention arsenal guard", docs, "Living arsenal guard");
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

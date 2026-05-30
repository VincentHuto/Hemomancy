package com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LivingStaffBondSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private LivingStaffBondSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String pendingCraft = read("src/main/java/com/vincenthuto/hemomancy/common/event/PendingBloodCraftManager.java");
		String helper = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/livingstaff/LivingStaffBondHelper.java");
		String manipInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java");
		String treeInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationTreeInit.java");

		assertContains("living staff craft calls bond helper", pendingCraft,
				"LivingStaffBondHelper.unlockFromBloodStructureCraft(crafter, result)");
		assertContains("bond helper unlocks capability", helper, "progress.unlockLivingStaffBond()");
		assertContains("bond helper grants conjure staff", helper, "ManipulationInit.conjure_staff");
		assertContains("bond helper auto equips granted manipulation", helper,
				"KnownManipulationGrantHelper.learnAndEquipIfPossible");
		assertContains("conjure staff registered", manipInit, "MANIPS.register(\"conjure_staff\"");
		assertContains("conjure staff summons living staff", manipInit, "ItemInit.living_staff");
		assertContains("conjure staff in manipulation tree", treeInit, "register(\"conjure_staff\"");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}
